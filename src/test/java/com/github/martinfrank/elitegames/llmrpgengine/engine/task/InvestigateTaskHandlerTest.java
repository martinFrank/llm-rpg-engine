package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies that INVESTIGATE always answers: it describes the location or person its targetId
 * resolves to, and falls back to the place the player is standing in when nothing resolves – an
 * investigation must never end in a silent turn. A look at a person stays within what is
 * perceivable about them.
 */
class InvestigateTaskHandlerTest {

    private static final String WIRTSHAUS = "603696b5-e1be-4f85-a0e1-1209147b8a3f";
    private static final String KALGERIA = "4bdd45a1-33d0-4ea4-91af-86a53e53dc61";
    private static final String KALGERIA_NAME = "Kalgeria Mondläufer";
    private static final String MARKTPLATZ = "0a5df08a-2094-4fbf-a94f-ce6fd74ddfee";
    /** "the key at the marketplace has not been found yet" – holds until the investigation succeeds. */
    private static final UUID KEY_NOT_FOUND = UUID.fromString("df939c07-4445-45a2-a086-99d406ee14e7");
    /**
     * What the find has to be recognizable by in the Narrator's input. Matched case-insensitively:
     * whether the adventure authors the discovery itself or the engine derives it from the event's
     * item flags decides the wording, and the test is about the find reaching the Narrator at all.
     */
    private static final String KEY = "schlüssel";

    private final Buchenhain adventure = new Buchenhain();
    private final NarratorAgent narratorAgent = mock(NarratorAgent.class);
    private final InvestigateTaskHandler handler = new InvestigateTaskHandler(narratorAgent);

    /** A handler whose skill checks all succeed / all fail, so the outcome is not a coin flip. */
    private InvestigateTaskHandler handlerThatAlwaysSucceeds() {
        return new InvestigateTaskHandler(narratorAgent, () -> 0.0);
    }

    private InvestigateTaskHandler handlerThatAlwaysFails() {
        return new InvestigateTaskHandler(narratorAgent, () -> 1.0);
    }

    private Session startedSession() {
        Session session = new Session(adventure, new Player("Thorsten"));
        session.start();
        when(narratorAgent.narrate(any(NarratorContext.class))).thenReturn("Du siehst dich um.");
        return session;
    }

    /** A session standing in the inn, where the innkeeper is present at any time of day. */
    private Session sessionAtTheInn() {
        Session session = startedSession();
        session.setCurrentLocation(session.getLocation(UUID.fromString(WIRTSHAUS)));
        return session;
    }

    /** A session in chapter 2, the chapter that hides the key at the marketplace. */
    private Session sessionAtTheMarketplace() {
        Session session = startedSession();
        session.moveToNextChapter();
        session.setCurrentLocation(session.getLocation(UUID.fromString(MARKTPLATZ)));
        return session;
    }

    private Verdict investigateTheMarketplace() {
        return new Verdict("Der Spieler durchsucht den Marktplatz.", TaskType.INVESTIGATE,
                "Marktplatz", MARKTPLATZ);
    }

    /** What the handler asked the Narrator to describe. */
    private NarratorContext narratedContext() {
        ArgumentCaptor<NarratorContext> context = ArgumentCaptor.forClass(NarratorContext.class);
        verify(narratorAgent).narrate(context.capture());
        return context.getValue();
    }

    /** What the handler asked the Narrator to describe on the last of several turns. */
    private NarratorContext lastNarratedContext() {
        ArgumentCaptor<NarratorContext> context = ArgumentCaptor.forClass(NarratorContext.class);
        verify(narratorAgent, atLeastOnce()).narrate(context.capture());
        return context.getValue();
    }

    private String narratedLocation() {
        return narratedContext().location();
    }

    private String narratedPersons() {
        return narratedContext().persons();
    }

    @Test
    void describesTheLocationResolvedById() {
        Session session = startedSession();

        handler.execute(new Verdict("Der Spieler sieht sich im Wirtshaus um.", TaskType.INVESTIGATE,
                "Wirtshaus zum kleinen Adler", WIRTSHAUS), session);

        assertThat(narratedLocation()).contains("Wirtshaus zum kleinen Adler");
        assertThat(session.chatHistory.getLatestEntries(1).getFirst().statement())
                .isEqualTo("Du siehst dich um.");
    }

    @Test
    void unknownTargetIdDescribesTheCurrentLocation() {
        Session session = startedSession();

        // "gibt es hier einen schmied?" – the player asks about the here and now, and the verdict
        // agent has nothing to resolve the question against.
        handler.execute(new Verdict("Der Spieler fragt nach dem Schmied im Dorf.",
                TaskType.INVESTIGATE, "", Verdict.UNKNOWN), session);

        assertThat(narratedLocation()).contains("Buchenhain Dorfplatz");
    }

    @Test
    void targetIdOfAnUnknownPlaceDescribesTheCurrentLocation() {
        Session session = startedSession();

        handler.execute(new Verdict("Der Spieler untersucht etwas.", TaskType.INVESTIGATE,
                "Mondbasis", "00000000-0000-0000-0000-000000000000"), session);

        assertThat(narratedLocation()).contains("Buchenhain Dorfplatz");
    }

    @Test
    void describesThePersonResolvedById() {
        Session session = sessionAtTheInn();

        handler.execute(new Verdict("Der Spieler betrachtet die Wirtin.", TaskType.INVESTIGATE,
                KALGERIA_NAME, KALGERIA), session);

        assertThat(narratedPersons())
                .contains(KALGERIA_NAME)
                .contains("dicke, freundliche Frau mit roten Wangen");
        assertThat(session.chatHistory.getLatestEntries(1).getFirst().statement())
                .isEqualTo("Du siehst dich um.");
    }

    @Test
    void aLookAtSomeoneStaysWithinWhatIsPerceivable() {
        Session session = sessionAtTheInn();

        handler.execute(new Verdict("Der Spieler betrachtet die Wirtin.", TaskType.INVESTIGATE,
                KALGERIA_NAME, KALGERIA), session);

        // Her function in the plot, her history and her inner life are not things a look reveals.
        assertThat(narratedPersons())
                .doesNotContain("Nebencharakter")
                .doesNotContain("Baronstadt")
                .doesNotContain("lacht viel");
    }

    @Test
    void describesOnlyTheInspectedPerson() {
        Session session = sessionAtTheInn();
        // At night the village elder and the smith are in the inn as well.
        session.setCurrentTime(GameTime.AT_NIGHT);

        handler.execute(new Verdict("Der Spieler betrachtet die Wirtin.", TaskType.INVESTIGATE,
                KALGERIA_NAME, KALGERIA), session);

        assertThat(narratedPersons())
                .contains(KALGERIA_NAME)
                .doesNotContain("Ulf Stetten")
                .doesNotContain("Rangolf Klingbeil");
    }

    @Test
    void aSuccessfulSkillCheckFiresTheInvestigationsEvent() {
        Session session = sessionAtTheMarketplace();
        assertThat(session.evaluate(adventure.getCondition(KEY_NOT_FOUND))).isTrue();

        handlerThatAlwaysSucceeds().execute(investigateTheMarketplace(), session);

        // The trigger's event raises the item flag, which is exactly what "key not found" negates.
        assertThat(session.evaluate(adventure.getCondition(KEY_NOT_FOUND))).isFalse();
        // ... and the Narrator is told about the find, or the player never learns of it.
        assertThat(narratedContext().interestingDetails()).containsIgnoringCase(KEY);
    }

    @Test
    void aFailedSkillCheckChangesNothingAndGivesNothingAway() {
        Session session = sessionAtTheMarketplace();

        handlerThatAlwaysFails().execute(investigateTheMarketplace(), session);

        assertThat(session.evaluate(adventure.getCondition(KEY_NOT_FOUND))).isTrue();
        // A description hinting at the key would hand the player the discovery they just failed at.
        assertThat(narratedContext().interestingDetails()).doesNotContainIgnoringCase(KEY);
        // The turn still answers: looking around is never silent.
        assertThat(session.chatHistory.getLatestEntries(1).getFirst().statement())
                .isEqualTo("Du siehst dich um.");
    }

    @Test
    void aFailedInvestigationCanBeRepeated() {
        Session session = sessionAtTheMarketplace();

        handlerThatAlwaysFails().execute(investigateTheMarketplace(), session);
        handlerThatAlwaysSucceeds().execute(investigateTheMarketplace(), session);

        assertThat(session.evaluate(adventure.getCondition(KEY_NOT_FOUND))).isFalse();
        assertThat(lastNarratedContext().interestingDetails()).containsIgnoringCase(KEY);
    }

    @Test
    void whatWasFoundOnceIsNotFoundAgain() {
        Session session = sessionAtTheMarketplace();
        InvestigateTaskHandler alwaysSucceeds = handlerThatAlwaysSucceeds();

        alwaysSucceeds.execute(investigateTheMarketplace(), session);
        alwaysSucceeds.execute(investigateTheMarketplace(), session);

        // The second look describes the marketplace as it is now – without finding the key twice.
        assertThat(lastNarratedContext().interestingDetails()).doesNotContainIgnoringCase(KEY);
    }

    @Test
    void anInvestigationIsOnlyMadeWhereTheChapterHidesSomething() {
        Session session = sessionAtTheMarketplace();

        // The key lies at the marketplace; searching the inn must not turn it up.
        handlerThatAlwaysSucceeds().execute(new Verdict("Der Spieler durchsucht das Wirtshaus.",
                TaskType.INVESTIGATE, "Wirtshaus zum kleinen Adler", WIRTSHAUS), session);

        assertThat(session.evaluate(adventure.getCondition(KEY_NOT_FOUND))).isTrue();
        assertThat(narratedContext().interestingDetails()).doesNotContainIgnoringCase(KEY);
    }

    @Test
    void theKeyIsFoundInEveryChapterThatHidesIt() {
        // The marketplace hides the key from chapter 1 on, so the very first look can turn it up.
        Session session = startedSession();

        handlerThatAlwaysSucceeds().execute(investigateTheMarketplace(), session);

        assertThat(session.evaluate(adventure.getCondition(KEY_NOT_FOUND))).isFalse();
        assertThat(narratedContext().interestingDetails()).containsIgnoringCase(KEY);
    }

    /**
     * A discovery carried over into the next chapter stays made: the chapter scripts the same
     * investigation again, but its condition no longer holds once the key has been found.
     */
    @Test
    void aFindCarriesOverIntoTheNextChapter() {
        Session session = startedSession();
        InvestigateTaskHandler alwaysSucceeds = handlerThatAlwaysSucceeds();
        alwaysSucceeds.execute(investigateTheMarketplace(), session);

        session.moveToNextChapter();
        alwaysSucceeds.execute(investigateTheMarketplace(), session);

        assertThat(lastNarratedContext().interestingDetails()).doesNotContainIgnoringCase(KEY);
    }

    @Test
    void investigationLeavesThePlayerWhereTheyAre() {
        Session session = startedSession();

        handler.execute(new Verdict("Der Spieler sieht sich im Wirtshaus um.", TaskType.INVESTIGATE,
                "Wirtshaus zum kleinen Adler", WIRTSHAUS), session);

        // Looking at another place is not walking there.
        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
    }
}

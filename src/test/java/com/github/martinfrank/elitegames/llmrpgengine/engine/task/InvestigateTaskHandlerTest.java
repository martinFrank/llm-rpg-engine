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

    private final NarratorAgent narratorAgent = mock(NarratorAgent.class);
    private final InvestigateTaskHandler handler = new InvestigateTaskHandler(narratorAgent);

    private Session startedSession() {
        Session session = new Session(new Buchenhain(), new Player("Thorsten"));
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

    /** What the handler asked the Narrator to describe. */
    private NarratorContext narratedContext() {
        ArgumentCaptor<NarratorContext> context = ArgumentCaptor.forClass(NarratorContext.class);
        verify(narratorAgent).narrate(context.capture());
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
    void investigationLeavesThePlayerWhereTheyAre() {
        Session session = startedSession();

        handler.execute(new Verdict("Der Spieler sieht sich im Wirtshaus um.", TaskType.INVESTIGATE,
                "Wirtshaus zum kleinen Adler", WIRTSHAUS), session);

        // Looking at another place is not walking there.
        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
    }
}

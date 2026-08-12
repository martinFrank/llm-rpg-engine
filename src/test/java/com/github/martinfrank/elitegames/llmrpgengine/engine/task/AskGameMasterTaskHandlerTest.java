package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;
import com.github.martinfrank.elitegames.llmrpgengine.agent.GameMasterFacet;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies the split this handler rests on: <em>what</em> is true comes from the session, and only
 * <em>how</em> it is told comes from the Narrator.
 * <p>
 * The facts are therefore asserted on what the handler hands the agent (the AUSKUNFT it puts into
 * the context), not on the narration that comes back – the latter is the model's wording and says
 * nothing about whether the answer was true.
 */
class AskGameMasterTaskHandlerTest {

    private static final Id WIRTSHAUS = Id.of("location.wirtshaus-zum-adler");
    private static final Id AUFTRAG_DES_ORTSVORSTEHERS = Id.of("flag.auftrag-erhalten");
    private static final Id WISSEN_UEBER_DIE_BEDROHUNG = Id.of("flag.kennt-bedrohung");

    private static final String NARRATION = "Ihr steht auf dem Dorfplatz, und die Sonne neigt sich.";

    private final NarratorAgent narratorAgent = mock(NarratorAgent.class);
    private final AskGameMasterTaskHandler handler = new AskGameMasterTaskHandler(narratorAgent);

    private Session startedSession() {
        when(narratorAgent.narrate(any(NarratorContext.class))).thenReturn(NARRATION);
        Session session = new Session(new Buchenhain().build(), new Player("Thorsten"));
        session.start();
        return session;
    }

    private void ask(GameMasterFacet facet, Session session) {
        handler.execute(new Verdict("Der Spieler fragt den Spielleiter.", TaskType.ASK_GAME_MASTER,
                "", Verdict.UNKNOWN, "", Verdict.UNKNOWN, facet), session);
    }

    /** The binding facts the handler worked out and handed to the Narrator to put into words. */
    private String factsHandedToTheNarrator(GameMasterFacet facet, Session session) {
        ask(facet, session);
        ArgumentCaptor<NarratorContext> context = ArgumentCaptor.forClass(NarratorContext.class);
        verify(narratorAgent).narrate(context.capture());
        return context.getValue().interestingDetails();
    }

    /** What the player ends up reading. */
    private String answer(GameMasterFacet facet, Session session) {
        ask(facet, session);
        return session.chatHistory.getLatestEntries(1).getFirst().statement();
    }

    @Test
    void theNarratorPutsTheAnswerIntoWords() {
        Session session = startedSession();

        assertThat(answer(GameMasterFacet.WHAT_TIME_IS_IT, session)).isEqualTo(NARRATION);
    }

    @Test
    void theNarratorIsToldWhatTheQuestionWas() {
        Session session = startedSession();

        ask(GameMasterFacet.WHERE_CAN_I_GO, session);

        ArgumentCaptor<NarratorContext> context = ArgumentCaptor.forClass(NarratorContext.class);
        verify(narratorAgent).narrate(context.capture());
        assertThat(context.getValue().purpose()).contains("Wege");
    }

    @Test
    void namesTheCurrentLocation() {
        Session session = startedSession();

        assertThat(factsHandedToTheNarrator(GameMasterFacet.WHERE_AM_I, session))
                .contains("Buchenhain Dorfplatz");
    }

    @Test
    void listsOnlyTheWaysTheChapterActuallyOpens() {
        Session session = startedSession();

        String facts = factsHandedToTheNarrator(GameMasterFacet.WHERE_CAN_I_GO, session);

        assertThat(facts).contains("Haus des Dorfvorstehers", "Die Dorf Schmiede",
                "Wirtshaus zum kleinen Adler", "Der Dorfladen");
        // The Dorfplatz lists the Blumental as a destination, but chapter 1 does not carry that
        // location: naming it would offer the player a way that GO_TO then refuses to walk.
        assertThat(facts).doesNotContain("Blumental");
    }

    @Test
    void listsOnlyTheWaysThatAreOpenAtThisTimeOfDay() {
        Session session = startedSession();
        session.setCurrentTime(GameTime.AT_NIGHT);

        String facts = factsHandedToTheNarrator(GameMasterFacet.WHERE_CAN_I_GO, session);

        // At night only the inn is open; the smithy, the shop and the headman's house are not.
        assertThat(facts).contains("Wirtshaus zum kleinen Adler");
        assertThat(facts).doesNotContain("Die Dorf Schmiede", "Der Dorfladen", "Haus des Dorfvorstehers");
    }

    @Test
    void saysSoWhenNobodyIsAround() {
        Session session = startedSession();

        // Nobody is placed at the Dorfplatz in chapter 1.
        assertThat(factsHandedToTheNarrator(GameMasterFacet.WHO_IS_HERE, session))
                .contains("Außer euch ist niemand hier.");
    }

    @Test
    void namesThePersonsPresentRightNow() {
        Session session = startedSession();
        session.setCurrentLocation(session.getLocation(WIRTSHAUS));

        assertThat(factsHandedToTheNarrator(GameMasterFacet.WHO_IS_HERE, session))
                .contains("Kalgeria Mondläufer");
    }

    @Test
    void namesTheTimeOfDayInsteadOfAnHour() {
        Session session = startedSession();

        assertThat(factsHandedToTheNarrator(GameMasterFacet.WHAT_TIME_IS_IT, session))
                .contains("Es ist Nachmittag.");
    }

    @Test
    void quotesTheBriefingWhenNothingIsKnownYet() {
        Session session = startedSession();

        String facts = factsHandedToTheNarrator(GameMasterFacet.WHAT_DO_I_KNOW, session);

        assertThat(facts).contains("Ulf Stetten");
        assertThat(facts).contains("Herausgefunden habt ihr seither noch nichts.");
    }

    @Test
    void listsWhatThePlayerHasFoundOut() {
        Session session = startedSession();
        session.sessionFlags.raiseFlagValue(AUFTRAG_DES_ORTSVORSTEHERS);
        session.sessionFlags.raiseFlagValue(WISSEN_UEBER_DIE_BEDROHUNG);

        String facts = factsHandedToTheNarrator(GameMasterFacet.WHAT_DO_I_KNOW, session);

        assertThat(facts).contains("Auftrag des Ortsvorstehers", "wissen über die Bedrohung im Dorf");
        assertThat(facts).doesNotContain("Herausgefunden habt ihr seither noch nichts.");
    }

    @Test
    void doesNotRevealKnowledgeThePlayerHasNotFoundYet() {
        Session session = startedSession();
        session.sessionFlags.raiseFlagValue(AUFTRAG_DES_ORTSVORSTEHERS);

        String facts = factsHandedToTheNarrator(GameMasterFacet.WHAT_DO_I_KNOW, session);

        assertThat(facts).contains("Auftrag des Ortsvorstehers");
        assertThat(facts).doesNotContain("wissen über die Bedrohung im Dorf",
                "das Horn der Silena wurde geklaut");
    }

    /**
     * The chapter's summary is written for the verdict agent and describes how the chapter is meant
     * to go – putting it in front of the Narrator would let the plot leak into the answer.
     */
    @Test
    void neverQuotesTheChaptersSummary() {
        Session session = startedSession();

        assertThat(factsHandedToTheNarrator(GameMasterFacet.WHAT_DO_I_KNOW, session))
                .doesNotContain("die Helden sollen vom dorf-vorsteher");
    }

    @Test
    void answersAnUnspecifiedQuestionWithAnOverview() {
        Session session = startedSession();

        String facts = factsHandedToTheNarrator(GameMasterFacet.UNSPECIFIED, session);

        assertThat(facts).contains("Buchenhain Dorfplatz", "Es ist Nachmittag.",
                "Außer euch ist niemand hier.", "Von hier aus könnt ihr gehen:");
    }

    @Test
    void logsTheAnswerAsMetaSoItStaysOutOfTheStoryContext() {
        Session session = startedSession();

        ask(GameMasterFacet.WHAT_TIME_IS_IT, session);

        assertThat(session.chatHistory.getLatestEntries(1).getFirst().kind())
                .isEqualTo(ChatEntry.Kind.META);
        assertThat(session.chatHistory.getLatestStoryEntries(50))
                .noneMatch(entry -> entry.statement().equals(NARRATION));
    }

    @Test
    void changesNothingAboutTheGame() {
        Session session = startedSession();

        for (GameMasterFacet facet : GameMasterFacet.values()) {
            ask(facet, session);
        }

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
        assertThat(session.getCurrentTime()).isEqualTo(GameTime.AFTERNOON);
        assertThat(session.getCurrentChapter().name()).isEqualTo("Probleme in Buchenwald");
        assertThat(session.getKnownKnowledge()).isEmpty();
    }

    /**
     * Guardrail: a local model that fails mid-reply must not turn a question about the time of day
     * into a technical error. The plainly worded facts are still a correct answer.
     */
    @Test
    void answersPlainlyWhenTheNarratorFails() {
        Session session = startedSession();
        when(narratorAgent.narrate(any(NarratorContext.class)))
                .thenThrow(new RuntimeException("Unexpected end-of-input"));

        assertThat(answer(GameMasterFacet.WHAT_TIME_IS_IT, session))
                .startsWith("Es ist Nachmittag.");
    }

    @Test
    void answersPlainlyWhenTheNarratorStaysSilent() {
        Session session = startedSession();
        when(narratorAgent.narrate(any(NarratorContext.class))).thenReturn("  ");

        assertThat(answer(GameMasterFacet.WHERE_AM_I, session))
                .contains("Buchenhain Dorfplatz");
    }

    /** The plain fallback is assembled line by line, so its structure must survive being logged. */
    @Test
    void keepsTheEnumerationOnSeparateLinesWhenAnsweringPlainly() {
        Session session = startedSession();
        when(narratorAgent.narrate(any(NarratorContext.class))).thenReturn(null);

        assertThat(answer(GameMasterFacet.WHERE_CAN_I_GO, session).lines()).hasSizeGreaterThan(1);
    }
}

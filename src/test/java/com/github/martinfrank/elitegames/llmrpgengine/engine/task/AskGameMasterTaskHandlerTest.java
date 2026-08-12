package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;
import com.github.martinfrank.elitegames.llmrpgengine.agent.GameMasterFacet;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.ChatEntry;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that a question to the game master is answered from the session – truthfully, without a
 * model, and without changing anything about the game.
 */
class AskGameMasterTaskHandlerTest {

    private static final Id WIRTSHAUS = Id.of("location.wirtshaus-zum-adler");
    private static final Id AUFTRAG_DES_ORTSVORSTEHERS = Id.of("flag.auftrag-erhalten");
    private static final Id WISSEN_UEBER_DIE_BEDROHUNG = Id.of("flag.kennt-bedrohung");

    private final AskGameMasterTaskHandler handler = new AskGameMasterTaskHandler();

    private Session startedSession() {
        Session session = new Session(new Buchenhain().build(), new Player("Thorsten"));
        session.start();
        return session;
    }

    /** The answer the handler produced, i.e. the last line of the log. */
    private String ask(GameMasterFacet facet, Session session) {
        handler.execute(new Verdict("Der Spieler fragt den Spielleiter.", TaskType.ASK_GAME_MASTER,
                "", Verdict.UNKNOWN, "", Verdict.UNKNOWN, facet), session);
        return session.chatHistory.getLatestEntries(1).getFirst().statement();
    }

    @Test
    void namesTheCurrentLocation() {
        Session session = startedSession();

        assertThat(ask(GameMasterFacet.WHERE_AM_I, session)).contains("Buchenhain Dorfplatz");
    }

    @Test
    void listsOnlyTheWaysTheChapterActuallyOpens() {
        Session session = startedSession();

        String answer = ask(GameMasterFacet.WHERE_CAN_I_GO, session);

        assertThat(answer).contains("Haus des Dorfvorstehers", "Die Dorf Schmiede",
                "Wirtshaus zum kleinen Adler", "Der Dorfladen");
        // The Dorfplatz lists the Blumental as a destination, but chapter 1 does not carry that
        // location: naming it would offer the player a way that GO_TO then refuses to walk.
        assertThat(answer).doesNotContain("Blumental");
    }

    @Test
    void listsOnlyTheWaysThatAreOpenAtThisTimeOfDay() {
        Session session = startedSession();
        session.setCurrentTime(GameTime.AT_NIGHT);

        String answer = ask(GameMasterFacet.WHERE_CAN_I_GO, session);

        // At night only the inn is open; the smithy, the shop and the headman's house are not.
        assertThat(answer).contains("Wirtshaus zum kleinen Adler");
        assertThat(answer).doesNotContain("Die Dorf Schmiede", "Der Dorfladen", "Haus des Dorfvorstehers");
    }

    @Test
    void saysSoWhenNobodyIsAround() {
        Session session = startedSession();

        // Nobody is placed at the Dorfplatz in chapter 1.
        assertThat(ask(GameMasterFacet.WHO_IS_HERE, session)).isEqualTo("Außer euch ist niemand hier.");
    }

    @Test
    void namesThePersonsPresentRightNow() {
        Session session = startedSession();
        session.setCurrentLocation(session.getLocation(WIRTSHAUS));

        assertThat(ask(GameMasterFacet.WHO_IS_HERE, session)).contains("Kalgeria Mondläufer");
    }

    @Test
    void namesTheTimeOfDayInsteadOfAnHour() {
        Session session = startedSession();

        String answer = ask(GameMasterFacet.WHAT_TIME_IS_IT, session);

        assertThat(answer).startsWith("Es ist Nachmittag.");
    }

    @Test
    void quotesTheBriefingWhenNothingIsKnownYet() {
        Session session = startedSession();

        String answer = ask(GameMasterFacet.WHAT_DO_I_KNOW, session);

        assertThat(answer).contains("Ulf Stetten");
        assertThat(answer).contains("Herausgefunden habt ihr seither noch nichts.");
    }

    @Test
    void listsWhatThePlayerHasFoundOut() {
        Session session = startedSession();
        session.sessionFlags.raiseFlagValue(AUFTRAG_DES_ORTSVORSTEHERS);
        session.sessionFlags.raiseFlagValue(WISSEN_UEBER_DIE_BEDROHUNG);

        String answer = ask(GameMasterFacet.WHAT_DO_I_KNOW, session);

        assertThat(answer).contains("Auftrag des Ortsvorstehers", "wissen über die Bedrohung im Dorf");
        assertThat(answer).doesNotContain("Herausgefunden habt ihr seither noch nichts.");
    }

    @Test
    void doesNotRevealKnowledgeThePlayerHasNotFoundYet() {
        Session session = startedSession();
        session.sessionFlags.raiseFlagValue(AUFTRAG_DES_ORTSVORSTEHERS);

        String answer = ask(GameMasterFacet.WHAT_DO_I_KNOW, session);

        assertThat(answer).contains("Auftrag des Ortsvorstehers");
        assertThat(answer).doesNotContain("wissen über die Bedrohung im Dorf",
                "das Horn der Silena wurde geklaut");
    }

    /**
     * The chapter's summary is written for the verdict agent and describes how the chapter is meant
     * to go – handing it to the player would give away the plot.
     */
    @Test
    void neverQuotesTheChaptersSummary() {
        Session session = startedSession();

        assertThat(ask(GameMasterFacet.WHAT_DO_I_KNOW, session))
                .doesNotContain("die Helden sollen vom dorf-vorsteher");
    }

    @Test
    void answersAnUnspecifiedQuestionWithAnOverview() {
        Session session = startedSession();

        String answer = ask(GameMasterFacet.UNSPECIFIED, session);

        assertThat(answer).contains("Buchenhain Dorfplatz", "Es ist Nachmittag.",
                "Außer euch ist niemand hier.", "Von hier aus könnt ihr gehen:");
    }

    @Test
    void logsTheAnswerAsMetaSoItStaysOutOfTheStoryContext() {
        Session session = startedSession();

        ask(GameMasterFacet.WHAT_TIME_IS_IT, session);

        assertThat(session.chatHistory.getLatestEntries(1).getFirst().kind())
                .isEqualTo(ChatEntry.Kind.META);
        assertThat(session.chatHistory.getLatestStoryEntries(50))
                .noneMatch(entry -> entry.statement().startsWith("Es ist"));
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

    /** The answers are assembled line by line, so their structure must survive being logged. */
    @Test
    void keepsTheEnumerationOnSeparateLines() {
        Session session = startedSession();

        assertThat(ask(GameMasterFacet.WHERE_CAN_I_GO, session).lines()).hasSizeGreaterThan(1);
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.engine;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictContext;
import com.github.martinfrank.elitegames.llmrpgengine.engine.task.TaskHandler;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the engine's guardrail: the verdict is reconciled with the actual game state before a
 * handler ever sees it, so a TALK that has no one to talk to still produces a turn.
 */
class GameEngineTest {

    private static final String DORFPLATZ = "0a5df08a-2094-4fbf-a94f-ce6fd74ddfee";
    private static final String WIRTSHAUS = "603696b5-e1be-4f85-a0e1-1209147b8a3f";
    private static final String KALGERIA = "4bdd45a1-33d0-4ea4-91af-86a53e53dc61";
    private static final String DIALOG_UEBER_MONSTER = "7975bb9c-72f0-4038-a5f7-591241275826";

    /** Records the verdict its task was dispatched with, instead of playing it out. */
    private static final class RecordingHandler implements TaskHandler {
        private final TaskType type;
        private Verdict received;

        private RecordingHandler(TaskType type) {
            this.type = type;
        }

        @Override
        public TaskType type() {
            return type;
        }

        @Override
        public void execute(Verdict verdict, Session session) {
            this.received = verdict;
        }
    }

    private final VerdictAgent verdictAgent = mock(VerdictAgent.class);
    private final RecordingHandler talk = new RecordingHandler(TaskType.TALK);
    private final RecordingHandler investigate = new RecordingHandler(TaskType.INVESTIGATE);
    private final RecordingHandler unknown = new RecordingHandler(TaskType.UNKNOWN);
    private final GameEngine engine = new GameEngine(verdictAgent, mock(NarratorAgent.class),
            List.of(talk, investigate, unknown));

    private Session startedSession() {
        Session session = new Session(new Buchenhain(), new Player("Thorsten"));
        session.start();
        return session;
    }

    private void stubVerdict(Verdict verdict) {
        when(verdictAgent.evaluate(any(VerdictContext.class), anyString())).thenReturn(verdict);
    }

    @Test
    void talkWithoutAnAddresseeInvestigatesTheCurrentLocation() {
        Session session = startedSession();
        // What the verdict agent produced for "gibt es hier einen schmied?": a question, so TALK,
        // but nobody is being addressed – there is not even a target.
        stubVerdict(new Verdict("Der Spieler fragt nach dem Schmied im Dorf.",
                TaskType.TALK, "", Verdict.UNKNOWN));

        engine.handleUserInput("gibt es hier einen schmied?", session);

        assertThat(talk.received).isNull();
        assertThat(investigate.received).isNotNull()
                .satisfies(verdict -> {
                    assertThat(verdict.task()).isEqualTo(TaskType.INVESTIGATE);
                    assertThat(verdict.targetId()).isEqualTo(DORFPLATZ);
                    assertThat(verdict.target()).isEqualTo("Buchenhain Dorfplatz");
                });
    }

    @Test
    void talkAgainstALocationInvestigatesThatLocation() {
        Session session = startedSession();
        stubVerdict(new Verdict("Der Spieler redet mit dem Wirtshaus.",
                TaskType.TALK, "Wirtshaus zum kleinen Adler", WIRTSHAUS));

        engine.handleUserInput("rede mit dem wirtshaus", session);

        assertThat(talk.received).isNull();
        assertThat(investigate.received.task()).isEqualTo(TaskType.INVESTIGATE);
        assertThat(investigate.received.targetId()).isEqualTo(WIRTSHAUS);
    }

    @Test
    void talkToAPresentPersonIsPassedThroughUntouched() {
        Session session = startedSession();
        session.setCurrentLocation(session.getLocation(UUID.fromString(WIRTSHAUS)));
        stubVerdict(new Verdict("Der Spieler fragt die Wirtin nach den Monstern.", TaskType.TALK,
                "Kalgeria Mondläufer", KALGERIA, "Die Monster", DIALOG_UEBER_MONSTER));

        engine.handleUserInput("frag die wirtin nach den monstern", session);

        assertThat(investigate.received).isNull();
        assertThat(talk.received).isNotNull()
                .satisfies(verdict -> {
                    assertThat(verdict.targetId()).isEqualTo(KALGERIA);
                    // The guardrail must not strip the dialog it resolved.
                    assertThat(verdict.dialogId()).isEqualTo(DIALOG_UEBER_MONSTER);
                });
    }

    @Test
    void unmappedInputStillReachesAHandler() {
        Session session = startedSession();
        stubVerdict(new Verdict("Der Spieler will fliegen.", TaskType.UNKNOWN, "", Verdict.UNKNOWN));

        engine.handleUserInput("ich fliege zum mond", session);

        // UNKNOWN is answered, not silently dropped.
        assertThat(unknown.received).isNotNull();
    }

    @Test
    void recordsThePlayersInput() {
        Session session = startedSession();
        stubVerdict(new Verdict("Der Spieler sieht sich um.", TaskType.INVESTIGATE,
                "Buchenhain Dorfplatz", DORFPLATZ));

        engine.handleUserInput("sieh dich um", session);

        assertThat(session.chatHistory.getLatestEntries(1).getFirst().statement())
                .isEqualTo("sieh dich um");
    }
}

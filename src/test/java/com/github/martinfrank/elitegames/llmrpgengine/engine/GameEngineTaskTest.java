package com.github.martinfrank.elitegames.llmrpgengine.engine;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.TwoLocationTestAdventure;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictContext;
import com.github.martinfrank.elitegames.llmrpgengine.engine.task.GeheZuTaskHandler;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies the task dispatch: a GEHEZU verdict must move the player to the target
 * location. The LLM is mocked, so no Ollama is required.
 */
class GameEngineTaskTest {

    private final VerdictAgent verdictAgent = mock(VerdictAgent.class);
    private final NarratorAgent narratorAgent = mock(NarratorAgent.class);
    private final GameEngine engine =
            new GameEngine(verdictAgent, narratorAgent, List.of(new GeheZuTaskHandler()));

    private Session startedSession() {
        Session session = new Session(new TwoLocationTestAdventure(), new Player("Thorsten"));
        session.start();
        return session;
    }

    @Test
    void geheZuMovesPlayerToTargetLocation() {
        when(verdictAgent.evaluate(any(VerdictContext.class), any()))
                .thenReturn(new Verdict("Der Spieler will zum Rathaus.", TaskType.GEHEZU, "Rathaus"));

        Session session = startedSession();
        assertThat(session.getCurrentLocation().name()).isEqualTo("Dorfplatz");

        engine.handleUserInput("geh zum rathaus", session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Rathaus");
    }

    @Test
    void geheZuIsCaseInsensitive() {
        when(verdictAgent.evaluate(any(VerdictContext.class), any()))
                .thenReturn(new Verdict("Zum Rathaus.", TaskType.GEHEZU, "rATHAUS"));

        Session session = startedSession();
        engine.handleUserInput("ab ins rathaus", session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Rathaus");
    }

    @Test
    void unknownTargetLeavesLocationUnchanged() {
        when(verdictAgent.evaluate(any(VerdictContext.class), any()))
                .thenReturn(new Verdict("Zu einem nicht existenten Ort.", TaskType.GEHEZU, "Mondbasis"));

        Session session = startedSession();
        engine.handleUserInput("geh zur mondbasis", session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Dorfplatz");
    }

    @Test
    void unknownTaskLeavesLocationUnchanged() {
        when(verdictAgent.evaluate(any(VerdictContext.class), any()))
                .thenReturn(new Verdict("Unklar.", TaskType.UNBEKANNT, ""));

        Session session = startedSession();
        engine.handleUserInput("hmpf", session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Dorfplatz");
    }
}

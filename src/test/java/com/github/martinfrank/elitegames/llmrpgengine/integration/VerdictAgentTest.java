package com.github.martinfrank.elitegames.llmrpgengine.integration;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.agent.GameMasterFacet;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictContext;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Full integration test that talks to a REAL Ollama instance — no mocks.
 * <p>
 * Requires a running Ollama server (see application.yml / OLLAMA_BASE_URL) and is
 * therefore gated behind the {@code OLLAMA_IT=true} environment variable so it does
 * not break the normal build. Run it explicitly, e.g.:
 * {@code OLLAMA_IT=true mvn -Dtest=IntegrationTest test}
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "OLLAMA_IT", matches = "true")
class VerdictAgentTest {

    // Injected by Spring, fully wired: GameEngine -> Verdict/NarratorAgent -> real Ollama ChatClients.
    @Autowired
    private GameEngine engine;

    /** Directly, because the classification itself is what is under test here. */
    @Autowired
    private VerdictAgent verdictAgent;

    @Test
    void testWalkToInput() {
        Adventure adventure = new Buchenhain();
        Player player = new Player("Thorsten");
        Session session = new Session(adventure, player);

        session.start();
        engine.handleUserInput("wir gehen zum Haus des Bürgermeisters", session);
    }

    @Test
    void testInvestigateInput() {
        Adventure adventure = new Buchenhain();
        Player player = new Player("Thorsten");
        Session session = new Session(adventure, player);
        session.start();

        engine.handleUserInput("sehen uns den markt genauer an", session);
    }

    /**
     * A question the player asks the game master, not a person: nobody is on the village square,
     * so this must not become a TALK. Expected verdict: INVESTIGATE of the current location.
     */
    @Test
    void testQuestionAboutTheSurroundings() {
        Adventure adventure = new Buchenhain();
        Player player = new Player("Thorsten");
        Session session = new Session(adventure, player);
        session.start();

        engine.handleUserInput("gibt es hier einen schmied?", session);
    }

    /**
     * The classification of a question about the state of the game, which is the one part of
     * ASK_GAME_MASTER a unit test cannot cover: whether the model picks the task and the facet is
     * decided by {@code prompts/verdict-system.st} alone. Everything downstream is deterministic.
     */
    @ParameterizedTest
    @CsvSource({
            "wo bin ich?,                       WHERE_AM_I",
            "wohin kann ich von hier gehen?,    WHERE_CAN_I_GO",
            "wer ist hier?,                     WHO_IS_HERE",
            "wie spät ist es gerade?,           WHAT_TIME_IS_IT",
            "was war eigentlich meine aufgabe?, WHAT_DO_I_KNOW",
    })
    void testQuestionAboutTheStateOfTheGame(String input, GameMasterFacet expectedFacet) {
        Adventure adventure = new Buchenhain();
        Session session = new Session(adventure, new Player("Thorsten"));
        session.start();

        Verdict verdict = verdictAgent.evaluate(VerdictContext.generate(session), input);

        assertThat(verdict.task()).isEqualTo(TaskType.ASK_GAME_MASTER);
        assertThat(verdict.facetOrUnspecified()).isEqualTo(expectedFacet);
    }
}

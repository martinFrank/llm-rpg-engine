package com.github.martinfrank.elitegames.llmrpgengine.integration;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


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
}

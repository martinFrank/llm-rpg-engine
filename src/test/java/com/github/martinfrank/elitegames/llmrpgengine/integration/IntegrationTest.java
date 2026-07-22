package com.github.martinfrank.elitegames.llmrpgengine.integration;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.BaseFlag;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

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
class IntegrationTest {

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

    @Test
    void testInvestigateTavernInput() {
        Adventure buchenhain = new Buchenhain();
        Player player = new Player("Thorsten");
        Session session = new Session(buchenhain, player);
        session.start();

        //zeit auf abends setzen
        session.setFlag(BaseFlag.GAME_TIME_FLAG.id(), GameTime.IN_THE_EVENING);

        //zum gasthaus gehen
        Location gasthaus = buchenhain.getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"));
        session.chatHistory.player("wir gehen zum Wirtshaus");
        session.chatHistory.narrator(gasthaus.description());
        session.setCurrentLocation(gasthaus);

        engine.handleUserInput("was ist hier aktuell los?", session);
    }

//    @Test
//    void testTalkToMajornInput() {
//        Adventure buchenhain = new Buchenhain();
//        Player player = new Player("Thorsten");
//        Session session = new Session(buchenhain, player);
//        session.start();
//
//        //zeit auf abends setzen
//        session.setFlag(BaseFlag.GAME_TIME_FLAG.getId(), GameTime.IN_THE_EVENING);
//
//        //zum gasthaus gehen
//        Location gasthaus = buchenhain.getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"));
//        session.chatHistory.player("wir gehen zum Wirtshaus");
//        session.chatHistory.narrator(gasthaus.description());
//        session.setCurrentLocation(gasthaus);
//
//        engine.handleUserInput("ich frage den Ortsvorsteher, welches Thema er mit mir besprechen wollte?", session);
//    }


}

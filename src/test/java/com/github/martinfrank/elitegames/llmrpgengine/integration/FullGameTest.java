package com.github.martinfrank.elitegames.llmrpgengine.integration;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
class FullGameTest {

    // Injected by Spring, fully wired: GameEngine -> Verdict/NarratorAgent -> real Ollama ChatClients.
    @Autowired
    private GameEngine engine;

    @Test
    void testFullGame(){
        Adventure adventure = new Buchenhain();
        Player player = new Player("Thorsten");
        Session session = new Session(adventure, player);

        session.start();
        engine.handleUserInput("wir sehen uns erstmal auf dem marktplatz um", session);
        engine.handleUserInput("gehen erstmal zum gasthaus, mal schauen was da los ist", session);
        session.setCurrentTime(GameTime.IN_THE_EVENING);
        Location currentLocation = session.getCurrentLocation();
        List<Person> persons = session.getCurrentPersons(currentLocation);
//        System.out.println("Current location: " + currentLocation);
//        System.out.println("Current persons: " + persons);
        engine.handleUserInput("wer ist denn alles so im Gasthaus?", session);
//        engine.handleUserInput("wir gehen jetzt erstmal zum Dorfvorstehen, ich bin gespannt, was der von uns wollte", session);
        engine.handleUserInput("Ich frage die Wirtin, wie es gerade im Dorf so läuft", session);
        engine.handleUserInput("Ich frage den Ulf, wieso er mit uns sprechen wollte", session);

        session.chatHistory.prettyPrint(System.out);

    }

}

package com.github.martinfrank.elitegames.llmrpgengine.integration;

import com.github.martinfrank.elitegames.llmrpgengine.LlmRpgEngineApplication;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Scanner;

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

    /**
     * The same game as {@link #testFullGame()}, but the input comes from the console instead of a
     * scripted list: every line is handed to the engine as the player's turn until {@code exit}
     * (or end of input) ends the session.
     * <p>
     * Needs a running Ollama, like the test. Unlike the test it wires up Spring itself, so it can
     * be started straight from the IDE ("run main"); {@code OLLAMA_IT} is not consulted.
     */
    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(LlmRpgEngineApplication.class)
                .web(WebApplicationType.NONE)
                .run(args)) {

            GameEngine engine = context.getBean(GameEngine.class);
            Session session = new Session(new Buchenhain(), new Player("Thorsten"));

            session.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\n> ");
                System.out.flush();
                if (!scanner.hasNextLine()) {
                    break;
                }
                String userInput = scanner.nextLine().strip();
                if (userInput.isEmpty()) {
                    continue;
                }
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }
                if ("history".equalsIgnoreCase(userInput)) {
                    session.chatHistory.prettyPrint(System.out);
                    continue;
                }
                engine.handleUserInput(userInput, session);
            }

            System.out.println();
            session.chatHistory.prettyPrint(System.out);
        }
    }

}

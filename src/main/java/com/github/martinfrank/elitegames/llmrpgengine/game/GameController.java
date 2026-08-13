package com.github.martinfrank.elitegames.llmrpgengine.game;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureStore;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * What the game page talks to: start a game, play a turn, read the state.
 * <p>
 * The running game lives in the browser's http session, so opening the page in a second browser
 * starts a second game instead of joining the first. That is the honest mapping of a single-player
 * adventure onto a web page, and it costs one attribute rather than a session registry with a
 * lifecycle nobody would maintain.
 * <p>
 * A turn is a call to a local LLM and takes seconds, not milliseconds. Two of them at once on the
 * same {@link Session} would interleave their writes to the log and the flags, so a turn holds the
 * session while it is played - the page disables its input for the same reason, this is the part
 * that also holds for a second tab.
 */
@RestController
@RequestMapping("/api/game")
public class GameController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    /** Where the running game is kept. Qualified, because an http session is shared ground. */
    private static final String SESSION_KEY = GameController.class.getName() + ".session";

    private static final String DEFAULT_PLAYER = "Spieler";

    private final GameEngine engine;
    private final AdventureStore store;

    public GameController(GameEngine engine, AdventureStore store) {
        this.engine = engine;
        this.store = store;
    }

    /**
     * Starts a new game and throws away whatever was running.
     * <p>
     * The adventure is taken from {@link AdventureStore} at this moment rather than at startup, so
     * a game started after a save in the editor plays what was saved.
     */
    @PostMapping("/start")
    public GameView start(@RequestBody(required = false) StartRequest request, HttpSession http) {
        String name = request == null || request.player() == null || request.player().isBlank()
                ? DEFAULT_PLAYER
                : request.player().strip();
        Session session = new Session(store.adventure(), new Player(name));
        session.start();
        http.setAttribute(SESSION_KEY, session);
        LOGGER.info("new game '{}' for '{}' (http session {})",
                session.getMetadata().title(), name, http.getId());
        return GameView.of(session, null);
    }

    /** The game as it stands, or 204 if none is running - which is the page's start screen. */
    @GetMapping("/state")
    public ResponseEntity<GameView> state(HttpSession http) {
        Session session = session(http);
        return session == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(GameView.of(session, null));
    }

    /**
     * Plays one turn: the player's input goes to the engine, and what the session looks like
     * afterwards comes back.
     * <p>
     * A turn that breaks is reported as part of the game rather than as a failed request. The
     * engine drives three agents against a local model over the network, and the ways that can go
     * wrong - the server being down, a reply that will not parse - are not the player's fault and
     * must not cost them the game they are in: the session is untouched by the failure, so the next
     * input is played as if nothing had happened.
     */
    @PostMapping("/input")
    public ResponseEntity<GameView> input(@RequestBody InputRequest request, HttpSession http) {
        Session session = session(http);
        if (session == null) {
            return ResponseEntity.notFound().build();
        }
        String text = request.text() == null ? "" : request.text().strip();
        if (text.isEmpty()) {
            return ResponseEntity.ok(GameView.of(session, null));
        }

        String error = null;
        synchronized (session) {
            try {
                engine.handleUserInput(text, session);
            } catch (RuntimeException failed) {
                LOGGER.warn("turn '{}' could not be played", text, failed);
                error = "Der Spielleiter ist ins Stolpern geraten (" + failed.getClass().getSimpleName()
                        + "). Der Zug wurde nicht zu Ende gespielt - versuch es noch einmal oder"
                        + " anders. Was passiert ist, steht im Log des Servers.";
            }
            return ResponseEntity.ok(GameView.of(session, error));
        }
    }

    /** Ends the running game, so the next call to {@code /state} shows the start screen again. */
    @PostMapping("/quit")
    public ResponseEntity<Void> quit(HttpSession http) {
        http.removeAttribute(SESSION_KEY);
        return ResponseEntity.noContent().build();
    }

    private static Session session(HttpSession http) {
        return (Session) http.getAttribute(SESSION_KEY);
    }

    /** @param player the name the game is played under; blank means {@value #DEFAULT_PLAYER} */
    public record StartRequest(String player) {
    }

    /** @param text what the player typed - one turn of the game */
    public record InputRequest(String text) {
    }
}

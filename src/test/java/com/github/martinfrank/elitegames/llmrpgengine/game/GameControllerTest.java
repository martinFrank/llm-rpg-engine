package com.github.martinfrank.elitegames.llmrpgengine.game;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.format.AdventureStore;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * What the game page can rely on: a game lives in the http session, a turn comes back as the whole
 * state, and a turn that breaks is a line in the log rather than a failed request.
 */
class GameControllerTest {

    private final GameEngine engine = mock(GameEngine.class);
    private final AdventureStore store = mock(AdventureStore.class);
    private final GameController controller = new GameController(engine, store);
    private final HttpSession http = httpSession();

    GameControllerTest() {
        when(store.adventure()).thenReturn(new Buchenhain().build());
    }

    /** An http session that is nothing but its attributes – all the controller uses of one. */
    private static HttpSession httpSession() {
        Map<String, Object> attributes = new HashMap<>();
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn("test-session");
        when(session.getAttribute(anyString())).thenAnswer(call -> attributes.get(call.getArgument(0)));
        doAnswer(call -> attributes.put(call.getArgument(0), call.getArgument(1)))
                .when(session).setAttribute(anyString(), any());
        doAnswer(call -> attributes.remove(call.getArgument(0)))
                .when(session).removeAttribute(anyString());
        return session;
    }

    /** Plays the turn by writing the given line into the log, instead of asking a model. */
    private void engineAnswers(String narration) {
        doAnswer(call -> {
            call.<Session>getArgument(1).chatHistory.narrator(narration);
            return null;
        }).when(engine).handleUserInput(anyString(), any(Session.class));
    }

    @Test
    void withoutAGameThereIsNoState() {
        assertThat(controller.state(http).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void startingAGameOpensTheAdventureWhereItBegins() {
        GameView view = controller.start(new GameController.StartRequest("Thorsten"), http);

        assertThat(view.title()).isEqualTo("Abenteuer in Buchenwald");
        assertThat(view.chapter()).isEqualTo("Probleme in Buchenwald");
        assertThat(view.location()).isEqualTo("Buchenhain Dorfplatz");
        assertThat(view.time()).isEqualTo("Nachmittag");
        // The way out of the first place is what the page offers the player to click.
        assertThat(view.destinations()).extracting(GameView.Named::name)
                .contains("Die Dorf Schmiede");
        // Title, author and the chapter's intro: the game has already been begun for them.
        assertThat(view.log()).hasSize(3);
        assertThat(view.error()).isNull();
    }

    @Test
    void theStartedGameIsTheOneTheNextRequestSees() {
        controller.start(null, http);

        ResponseEntity<GameView> state = controller.state(http);

        assertThat(state.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(state.getBody().location()).isEqualTo("Buchenhain Dorfplatz");
    }

    @Test
    void aTurnComesBackAsTheStateAfterIt() {
        controller.start(null, http);
        engineAnswers("Ihr seht euch auf dem Platz um.");

        GameView view = controller.input(new GameController.InputRequest("sieh dich um"), http).getBody();

        assertThat(view.log()).last()
                .satisfies(line -> assertThat(line.statement()).isEqualTo("Ihr seht euch auf dem Platz um."));
        assertThat(view.error()).isNull();
    }

    @Test
    void anEmptyTurnIsNotPlayedAtAll() {
        controller.start(null, http);

        GameView view = controller.input(new GameController.InputRequest("   "), http).getBody();

        assertThat(view.log()).hasSize(3);
    }

    /**
     * The engine drives three agents against a model over the network. When that fails the player
     * must keep the game they are in: the failure is reported, and the next turn is played.
     */
    @Test
    void aBrokenTurnIsReportedWithoutLosingTheGame() {
        controller.start(null, http);
        doThrow(new IllegalStateException("Ollama antwortet nicht"))
                .when(engine).handleUserInput(anyString(), any(Session.class));

        GameView failed = controller.input(new GameController.InputRequest("sieh dich um"), http).getBody();

        assertThat(failed.error()).contains("IllegalStateException");
        assertThat(failed.location()).isEqualTo("Buchenhain Dorfplatz");

        engineAnswers("Ihr seht euch auf dem Platz um.");
        GameView next = controller.input(new GameController.InputRequest("sieh dich um"), http).getBody();

        assertThat(next.error()).isNull();
        assertThat(next.log()).last()
                .satisfies(line -> assertThat(line.statement()).isEqualTo("Ihr seht euch auf dem Platz um."));
    }

    @Test
    void aTurnWithoutAGameIsNotFound() {
        assertThat(controller.input(new GameController.InputRequest("sieh dich um"), http).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void quittingLeavesNoGameBehind() {
        controller.start(null, http);

        controller.quit(http);

        assertThat(controller.state(http).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}

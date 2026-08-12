package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Verifies that GO_TO resolves its destination via the verdict's targetId (an id from
 * the available-locations list) and leaves the location unchanged when nothing resolves.
 */
class GoToTaskHandlerTest {

    private static final String DORFPLATZ = "location.dorfplatz";
    private static final String HAUS_DES_DORFVORSTEHERS = "location.haus-des-dorfvorstehers";

    /** A move narrates the arrival, so the handler needs an agent – stubbed, no LLM in a unit test. */
    private final NarratorAgent narratorAgent = mock(NarratorAgent.class);
    private final GoToTaskHandler handler = new GoToTaskHandler(narratorAgent);

    private void stubNarration(String narration) {
        when(narratorAgent.narrate(any(NarratorContext.class))).thenReturn(narration);
    }

    private Session startedSession() {
        Session session = new Session(new Buchenhain().build(), new Player("Thorsten"));
        session.start();
        return session;
    }

    @Test
    void movesPlayerToLocationResolvedById() {
        Session session = startedSession();
        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
        stubNarration("Ihr betretet das Haus des Dorfvorstehers.");

        handler.execute(new Verdict("Zum Vorsteher.", TaskType.GO_TO,
                "Haus des Dorfvorstehers", HAUS_DES_DORFVORSTEHERS), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Haus des Dorfvorstehers");
    }

    @Test
    void narratesTheArrival() {
        Session session = startedSession();
        stubNarration("Ihr betretet das Haus des Dorfvorstehers.");

        handler.execute(new Verdict("Zum Vorsteher.", TaskType.GO_TO,
                "Haus des Dorfvorstehers", HAUS_DES_DORFVORSTEHERS), session);

        assertThat(session.chatHistory.getLatestEntries(1).getFirst().statement())
                .isEqualTo("Ihr betretet das Haus des Dorfvorstehers.");
    }

    @Test
    void unknownTargetIdLeavesLocationUnchanged() {
        Session session = startedSession();

        handler.execute(new Verdict("Zum Mond.", TaskType.GO_TO, "Mondbasis", Verdict.UNKNOWN), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
        verifyNoInteractions(narratorAgent);
    }

    @Test
    void staysWhenTargetIdIsNotAKnownLocation() {
        Session session = startedSession();

        // A well-formed but unknown id must not move the player.
        handler.execute(new Verdict("Irgendwohin.", TaskType.GO_TO,
                "Nirgendwo", "location.mondbasis"), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
        verifyNoInteractions(narratorAgent);
    }

    @Test
    void dorfplatzIdResolvesToStartLocation() {
        Session session = startedSession();
        stubNarration("Ihr steht wieder auf dem Dorfplatz.");

        handler.execute(new Verdict("Zurück zum Platz.", TaskType.GO_TO,
                "Buchenhain Dorfplatz", DORFPLATZ), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
    }
}

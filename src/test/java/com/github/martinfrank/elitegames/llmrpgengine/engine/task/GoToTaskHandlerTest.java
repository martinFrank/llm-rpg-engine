package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that GO_TO resolves its destination via the verdict's targetId (a UUID from
 * the available-locations list) and leaves the location unchanged when nothing resolves.
 */
class GoToTaskHandlerTest {

    private static final String DORFPLATZ = "0a5df08a-2094-4fbf-a94f-ce6fd74ddfee";
    private static final String HAUS_DES_DORFVORSTEHERS = "b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2";

    private final GoToTaskHandler handler = new GoToTaskHandler();

    private Session startedSession() {
        Session session = new Session(new Buchenhain(), new Player("Thorsten"));
        session.start();
        return session;
    }

    @Test
    void movesPlayerToLocationResolvedById() {
        Session session = startedSession();
        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");

        handler.execute(new Verdict("Zum Vorsteher.", TaskType.GO_TO,
                "Haus des Dorfvorstehers", HAUS_DES_DORFVORSTEHERS), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Haus des Dorfvorstehers");
    }

    @Test
    void unknownTargetIdLeavesLocationUnchanged() {
        Session session = startedSession();

        handler.execute(new Verdict("Zum Mond.", TaskType.GO_TO, "Mondbasis", Verdict.UNKNOWN), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
    }

    @Test
    void staysWhenTargetIdIsNotAKnownLocation() {
        Session session = startedSession();

        // A syntactically valid but unknown UUID must not move the player.
        handler.execute(new Verdict("Irgendwohin.", TaskType.GO_TO,
                "Nirgendwo", "00000000-0000-0000-0000-000000000000"), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
    }

    @Test
    void dorfplatzIdResolvesToStartLocation() {
        Session session = startedSession();

        handler.execute(new Verdict("Zurück zum Platz.", TaskType.GO_TO,
                "Buchenhain Dorfplatz", DORFPLATZ), session);

        assertThat(session.getCurrentLocation().name()).isEqualTo("Buchenhain Dorfplatz");
    }
}

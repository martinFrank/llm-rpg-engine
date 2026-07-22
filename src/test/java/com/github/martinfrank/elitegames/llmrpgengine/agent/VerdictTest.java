package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link Verdict#targetUuid()} robustly turns the agent's targetId string
 * into a UUID, tolerating the "unbekannt" fallback and malformed LLM output.
 */
class VerdictTest {

    private Verdict withTargetId(String targetId) {
        return new Verdict("egal", TaskType.GEHEZU, "egal", targetId);
    }

    @Test
    void parsesValidUuid() {
        UUID id = UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2");

        assertThat(withTargetId(id.toString()).targetUuid()).contains(id);
    }

    @Test
    void trimsSurroundingWhitespace() {
        UUID id = UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2");

        assertThat(withTargetId("  " + id + "  ").targetUuid()).contains(id);
    }

    @Test
    void unknownYieldsEmpty() {
        assertThat(withTargetId("unbekannt").targetUuid()).isEmpty();
        assertThat(withTargetId("UNBEKANNT").targetUuid()).isEmpty();
    }

    @Test
    void blankOrNullYieldsEmpty() {
        assertThat(withTargetId("").targetUuid()).isEmpty();
        assertThat(withTargetId("   ").targetUuid()).isEmpty();
        assertThat(withTargetId(null).targetUuid()).isEmpty();
    }

    @Test
    void malformedIdYieldsEmpty() {
        assertThat(withTargetId("Haus des Dorfvorstehers").targetUuid()).isEmpty();
    }
}

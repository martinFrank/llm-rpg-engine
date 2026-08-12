package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.junit.jupiter.api.Test;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link Verdict#resolvedTargetId()} robustly turns the agent's targetId string
 * into an {@link Id}, tolerating the "unbekannt" fallback and malformed LLM output.
 */
class VerdictTest {

    private Verdict withTargetId(String targetId) {
        return new Verdict("egal", TaskType.GO_TO, "egal", targetId);
    }

    @Test
    void parsesValidId() {
        Id id = Id.of("location.haus-des-dorfvorstehers");

        assertThat(withTargetId(id.toString()).resolvedTargetId()).contains(id);
    }

    @Test
    void trimsSurroundingWhitespace() {
        Id id = Id.of("location.haus-des-dorfvorstehers");

        assertThat(withTargetId("  " + id + "  ").resolvedTargetId()).contains(id);
    }

    @Test
    void unknownYieldsEmpty() {
        assertThat(withTargetId("unbekannt").resolvedTargetId()).isEmpty();
        assertThat(withTargetId("UNBEKANNT").resolvedTargetId()).isEmpty();
    }

    @Test
    void blankOrNullYieldsEmpty() {
        assertThat(withTargetId("").resolvedTargetId()).isEmpty();
        assertThat(withTargetId("   ").resolvedTargetId()).isEmpty();
        assertThat(withTargetId(null).resolvedTargetId()).isEmpty();
    }

    @Test
    void malformedIdYieldsEmpty() {
        assertThat(withTargetId("Haus des Dorfvorstehers").resolvedTargetId()).isEmpty();
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies how a verdict reports whether the agent named an id at all. The ids themselves are not
 * parsed here – resolving them is the guardrail's job – so this only distinguishes "an id was
 * reported" from the "unbekannt" fallback and malformed LLM output.
 */
class VerdictTest {

    private static final String ID = "b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2";

    private Verdict withTargetId(String targetId) {
        return new Verdict("egal", TaskType.GO_TO, "egal", targetId);
    }

    private Verdict withDialogId(String dialogId) {
        return new Verdict("egal", TaskType.TALK, "egal", ID, "egal", dialogId);
    }

    @Test
    void reportedTargetIdIsRecognized() {
        assertThat(withTargetId(ID).hasTargetId()).isTrue();
        assertThat(withTargetId("  " + ID + "  ").hasTargetId()).isTrue();
    }

    @Test
    void mangledTargetIdCountsAsReported() {
        // Not a parsable UUID, but the agent clearly meant one - the guardrail gets to try.
        assertThat(withTargetId(ID.substring(0, ID.length() - 1)).hasTargetId()).isTrue();
    }

    @Test
    void unknownTargetIdIsNotReported() {
        assertThat(withTargetId("unbekannt").hasTargetId()).isFalse();
        assertThat(withTargetId("UNBEKANNT").hasTargetId()).isFalse();
        assertThat(withTargetId("  unbekannt ").hasTargetId()).isFalse();
    }

    @Test
    void blankOrNullTargetIdIsNotReported() {
        assertThat(withTargetId("").hasTargetId()).isFalse();
        assertThat(withTargetId("   ").hasTargetId()).isFalse();
        assertThat(withTargetId(null).hasTargetId()).isFalse();
    }

    @Test
    void malformedTargetIdCountsAsReported() {
        // A name instead of an id is still "the agent said something" - the guardrail rejects it.
        assertThat(withTargetId("Haus des Dorfvorstehers").hasTargetId()).isTrue();
    }

    @Test
    void dialogIdIsReportedIndependently() {
        UUID dialog = UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a");

        assertThat(withDialogId(dialog.toString()).hasDialogId()).isTrue();
        assertThat(withDialogId("unbekannt").hasDialogId()).isFalse();
        assertThat(withDialogId(null).hasDialogId()).isFalse();
    }

    @Test
    void convenienceConstructorReportsNoDialog() {
        assertThat(withTargetId(ID).hasDialogId()).isFalse();
    }
}

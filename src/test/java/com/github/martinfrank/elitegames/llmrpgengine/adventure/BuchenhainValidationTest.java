package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Holds the real adventure to the rules. This is the test that turns a typo in an id, a place
 * nobody can reach or a person placed outside their chapter into a red build instead of into a
 * turn that quietly does nothing.
 */
class BuchenhainValidationTest {

    @Test
    void buchenhainIsValid() {
        ValidationResult validation = AdventureValidator.validate(new Buchenhain().build());

        assertThat(validation.errors()).isEmpty();
    }

    /**
     * The warnings are not asserted one by one on purpose: they describe content that is still
     * being written (chapter 2 has knowledge nothing grants yet), and pinning them down would make
     * this test fail every time the adventure grows. What must hold is that they stay warnings.
     */
    @Test
    void theOpenEndsOfTheAdventureDoNotBlockPlaying() {
        ValidationResult validation = AdventureValidator.validate(new Buchenhain().build());

        assertThat(validation.hasErrors()).isFalse();
        assertThat(validation.warnings()).isNotEmpty();
    }
}

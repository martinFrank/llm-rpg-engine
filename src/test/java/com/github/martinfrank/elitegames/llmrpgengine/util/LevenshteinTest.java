package com.github.martinfrank.elitegames.llmrpgengine.util;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the id guardrail: an id an agent got slightly wrong still resolves to the candidate it
 * was meant to be, while invented, ambiguous, or missing ids resolve to nothing.
 */
class LevenshteinTest {

    private record Candidate(UUID id) implements Identifiable {
    }

    private static final Candidate ANNA = new Candidate(UUID.fromString("409b408c-4b7a-4bcc-9a37-527d02bcdf7a"));
    private static final Candidate BERT = new Candidate(UUID.fromString("c92c0884-5af2-45c5-8927-03ae61f4c711"));
    private static final List<Candidate> CANDIDATES = List.of(ANNA, BERT);

    @Test
    void exactIdResolves() {
        assertThat(Levenshtein.findClosest(ANNA.id().toString(), CANDIDATES)).isEqualTo(ANNA);
    }

    @Test
    void idWithOneWrongCharacterResolves() {
        String mangled = "409b408c-4b7a-4bcc-9a37-527d02bcdf7b";
        assertThat(Levenshtein.findClosest(mangled, CANDIDATES)).isEqualTo(ANNA);
    }

    @Test
    void idWithMissingCharacterResolves() {
        // Not even a parsable UUID any more - which is why the guardrail works on the raw string.
        String mangled = "409b408c-4b7a-4bcc-9a37-527d02bcdf7";
        assertThat(Levenshtein.findClosest(mangled, CANDIDATES)).isEqualTo(ANNA);
    }

    @Test
    void upperCaseIdResolves() {
        assertThat(Levenshtein.findClosest(ANNA.id().toString().toUpperCase(), CANDIDATES)).isEqualTo(ANNA);
    }

    @Test
    void surroundingWhitespaceIsIgnored() {
        assertThat(Levenshtein.findClosest("  " + BERT.id() + "\n", CANDIDATES)).isEqualTo(BERT);
    }

    @Test
    void tooDistantIdResolvesToNothing() {
        String mangled = "409b408c-4b7a-4bcc-9a37-527d02bcd000";
        assertThat(Levenshtein.findClosest(mangled, CANDIDATES)).isNull();
    }

    @Test
    void inventedIdResolvesToNothing() {
        assertThat(Levenshtein.findClosest("00000000-0000-0000-0000-000000000000", CANDIDATES)).isNull();
        assertThat(Levenshtein.findClosest("unbekannt", CANDIDATES)).isNull();
    }

    @Test
    void missingIdResolvesToNothing() {
        assertThat(Levenshtein.findClosest(null, CANDIDATES)).isNull();
        assertThat(Levenshtein.findClosest("   ", CANDIDATES)).isNull();
        assertThat(Levenshtein.findClosest(ANNA.id().toString(), List.<Candidate>of())).isNull();
    }

    @Test
    void ambiguousMatchResolvesToNothing() {
        Candidate a = new Candidate(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
        Candidate b = new Candidate(UUID.fromString("00000000-0000-0000-0000-00000000000b"));
        // Equally close (distance 1) to both -> no match, rather than an arbitrary one.
        assertThat(Levenshtein.findClosest("00000000-0000-0000-0000-00000000000c", List.of(a, b))).isNull();
    }

    @Test
    void distanceCountsSingleCharacterEdits() {
        assertThat(Levenshtein.distance("kitten", "kitten")).isZero();
        assertThat(Levenshtein.distance("kitten", "sitten")).isEqualTo(1);
        assertThat(Levenshtein.distance("kitten", "sitting")).isEqualTo(3);
        assertThat(Levenshtein.distance("", "abc")).isEqualTo(3);
    }
}

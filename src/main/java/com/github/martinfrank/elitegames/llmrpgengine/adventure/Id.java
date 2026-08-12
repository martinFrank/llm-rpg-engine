package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The identity of everything an adventure is built from – a place, a figure, a flag, a trigger.
 * <p>
 * An id is written by the author, not generated: {@code person.ulf-stetten} says who is meant,
 * a UUID does not. That matters twice over. In the adventure source a reference is readable on
 * its own, so it no longer needs a comment next to it that can drift away from the id it explains.
 * And in the prompts (see
 * {@link com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictContext VerdictContext}) the
 * agent has to copy ids verbatim: a short, meaningful one is copied far more reliably than 36
 * random characters.
 * <p>
 * The format is {@code namespace.slug}, lowercase ASCII with hyphens – no umlauts, because the
 * agents reproduce plain ASCII more reliably. The namespace names the kind of thing the id
 * belongs to ({@code location}, {@code person}, {@code item}, {@code flag}, {@code condition},
 * {@code trigger}, {@code dialog}, {@code investigation}, {@code chapter}), which makes a
 * reference pointing at the wrong kind of thing visible at a glance.
 */
public record Id(String value) implements Comparable<Id> {

    private static final Pattern FORMAT = Pattern.compile("[a-z]+\\.[a-z0-9][a-z0-9-]*");

    public Id {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "invalid id: '" + value + "' - expected namespace.slug, e.g. person.ulf-stetten");
        }
    }

    public static Id of(String value) {
        return new Id(value);
    }

    /**
     * Reads an id an agent reported back. Anything that is not a well-formed id – a name the agent
     * wrote instead of an id, a blank, {@code null} – yields an empty result rather than an
     * exception: unusable model output is a normal operating condition the caller handles by
     * falling back, never a programming error.
     */
    public static Optional<Id> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String stripped = raw.strip();
        if (!FORMAT.matcher(stripped).matches()) {
            return Optional.empty();
        }
        return Optional.of(new Id(stripped));
    }

    /** The part before the dot, i.e. the kind of thing this id belongs to. */
    public String namespace() {
        return value.substring(0, value.indexOf('.'));
    }

    @Override
    public int compareTo(Id other) {
        return value.compareTo(other.value);
    }

    /** The bare id, so an id drops into a prompt or a log line without ceremony. */
    @Override
    public String toString() {
        return value;
    }
}

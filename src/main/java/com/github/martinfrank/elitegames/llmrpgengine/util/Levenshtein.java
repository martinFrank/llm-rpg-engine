package com.github.martinfrank.elitegames.llmrpgengine.util;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Levenshtein (edit) distance: the minimum number of single-character insertions, deletions,
 * or substitutions to turn one string into another.
 * <p>
 * Used to recover LLM-mangled ids: an id that is off by one or two characters can still be
 * resolved to the intended one, because ids (UUIDs) are far apart from each other.
 * {@link #findClosest(String, List)} is the single entry point every guardrail uses for that.
 */
public final class Levenshtein {

    private static final Logger LOGGER = LoggerFactory.getLogger(Levenshtein.class);

    /**
     * How far a reported id may be (in edit distance) from a real id and still be accepted as
     * that id. Ids (UUIDs) are 36 characters and far apart from each other, so a small threshold
     * recovers LLM typos without risking a wrong match.
     */
    public static final int MAX_ID_DISTANCE = 3;

    private Levenshtein() {
    }

    public static int distance(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[b.length()];
    }

    /**
     * Guardrail for ids reported by an agent: the candidate whose id is closest to
     * {@code reportedId} within {@link #MAX_ID_DISTANCE}, or {@code null} when none is close
     * enough. See {@link #findClosest(String, List, int)}.
     */
    public static <T extends Identifiable> T findClosest(String reportedId, List<T> candidates) {
        return findClosest(reportedId, candidates, MAX_ID_DISTANCE);
    }

    /**
     * Guardrail for ids reported by an agent: instead of rigorously discarding an id that does
     * not match any candidate exactly, the closest candidate by edit distance wins, as long as
     * it is within {@code maxDistance}. This recovers ids the model got slightly wrong (a mangled
     * UUID) while still rejecting invented ones, which are far from every candidate.
     * <p>
     * The comparison is case-insensitive, because {@link java.util.UUID#toString()} is lower case
     * while a model may report the same id in upper case.
     *
     * @param reportedId  the id as reported, may be {@code null}, blank, or not a valid UUID
     * @param candidates  the ids that are legal here – this scoping is the actual guardrail, so
     *                    pass only what the agent was offered in its context (e.g. the dialogs of
     *                    <em>this</em> person), never the whole adventure
     * @param maxDistance the largest edit distance still accepted as a match
     * @return the matching candidate, or {@code null} if none is close enough or the closest
     * distance is shared by several candidates (an ambiguous match is no match)
     */
    public static <T extends Identifiable> T findClosest(String reportedId, List<T> candidates, int maxDistance) {
        if (reportedId == null || reportedId.isBlank() || candidates.isEmpty()) {
            return null;
        }
        String needle = reportedId.strip().toLowerCase(Locale.ROOT);
        T best = null;
        int bestDistance = Integer.MAX_VALUE;
        boolean ambiguous = false;
        for (T candidate : candidates) {
            int distance = distance(needle, candidate.id().toString());
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
                ambiguous = false;
            } else if (distance == bestDistance) {
                ambiguous = true;
            }
        }
        if (bestDistance > maxDistance) {
            return null;
        }
        if (ambiguous) {
            LOGGER.info("Guardrail: reported id '{}' is equally close (distance {}) to several candidates -> no match",
                    reportedId, bestDistance);
            return null;
        }
        return best;
    }
}

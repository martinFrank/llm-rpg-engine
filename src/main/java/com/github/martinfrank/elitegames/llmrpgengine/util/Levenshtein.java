package com.github.martinfrank.elitegames.llmrpgengine.util;

/**
 * Levenshtein (edit) distance: the minimum number of single-character insertions, deletions,
 * or substitutions to turn one string into another.
 * <p>
 * Used to recover LLM-mangled ids: an id that is off by one or two characters can still be
 * resolved to the intended one, as long as the ids of an adventure are far enough apart from
 * each other for the nearest one to be unambiguous.
 */
public final class Levenshtein {

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
}

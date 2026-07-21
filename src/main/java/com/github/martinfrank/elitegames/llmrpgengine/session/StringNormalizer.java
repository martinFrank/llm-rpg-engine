package com.github.martinfrank.elitegames.llmrpgengine.session;

public class StringNormalizer {

    /**
     * Collapses wrapped text lines of a paragraph into a single line (line breaks
     * become spaces), while keeping blank lines that separate paragraphs. Leading
     * and trailing blank lines are dropped.
     */
    public static String normalize(String statement) {
        if (statement == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        StringBuilder paragraph = new StringBuilder();
        for (String line : statement.split("\n", -1)) {
            if (line.isBlank()) {
                flush(paragraph, result);
                result.append('\n');
            } else {
                if (!paragraph.isEmpty()) {
                    paragraph.append(' ');
                }
                paragraph.append(line.strip());
            }
        }
        flush(paragraph, result);
        return result.toString().strip();
    }

    private static void flush(StringBuilder paragraph, StringBuilder result) {
        if (!paragraph.isEmpty()) {
            result.append(paragraph).append('\n');
            paragraph.setLength(0);
        }
    }
}

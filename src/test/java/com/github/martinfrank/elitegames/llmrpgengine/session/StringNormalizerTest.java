package com.github.martinfrank.elitegames.llmrpgengine.session;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringNormalizerTest {

    @Test
    void collapsesWrappedLinesOfASingleParagraph() {
        String input = """
                Der Ort besteht nur aus ein paar wenigen Häusern, die von den
                Bauern bewohnt werden. Einer der Bauern hat auch einen kleinen
                Laden und einen Schmied gibt es auch.
                """;

        assertThat(StringNormalizer.normalize(input)).isEqualTo(
                "Der Ort besteht nur aus ein paar wenigen Häusern, die von den "
                        + "Bauern bewohnt werden. Einer der Bauern hat auch einen kleinen "
                        + "Laden und einen Schmied gibt es auch.");
    }

    @Test
    void keepsBlankLineBetweenParagraphs() {
        String input = """
                Erster Absatz Zeile eins
                Erster Absatz Zeile zwei

                Zweiter Absatz Zeile eins
                Zweiter Absatz Zeile zwei""";

        assertThat(StringNormalizer.normalize(input)).isEqualTo(
                "Erster Absatz Zeile eins Erster Absatz Zeile zwei\n\n"
                        + "Zweiter Absatz Zeile eins Zweiter Absatz Zeile zwei");
    }

    @Test
    void preservesMultipleBlankLines() {
        assertThat(StringNormalizer.normalize("A\n\n\nB")).isEqualTo("A\n\n\nB");
    }

    @Test
    void dropsLeadingAndTrailingBlankLines() {
        assertThat(StringNormalizer.normalize("\n\nHallo\nWelt\n\n")).isEqualTo("Hallo Welt");
    }

    @Test
    void handlesNull() {
        assertThat(StringNormalizer.normalize(null)).isNull();
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** That an author never has to type an id, and is told when a name would produce a bad one. */
class IdSuggesterTest {

    private final Adventure adventure = new Buchenhain().build();

    @Test
    void aNameBecomesAnId() {
        assertEquals("person.gunver-eichblatt",
                IdSuggester.suggest(adventure, "person", "Gunver Eichblatt").id());
    }

    @Test
    void umlautsAreSpelledOutRatherThanStripped() {
        assertEquals("kalgeria-mondlaeufer", IdSuggester.slugOf("Kalgeria Mondläufer"));
        assertEquals("eisenschluessel", IdSuggester.slugOf("Eisenschlüssel"));
        assertEquals("gruessgott", IdSuggester.slugOf("Grüßgott"));
    }

    @Test
    void punctuationAndRunsOfSpacesBecomeSingleHyphens() {
        assertEquals("das-horn-der-silena", IdSuggester.slugOf("  Das Horn   der Silena! "));
        assertEquals("wirtshaus-zum-adler", IdSuggester.slugOf("Wirtshaus \"zum Adler\""));
    }

    @Test
    void aNameWithNothingUsableInItYieldsNoId() {
        assertNull(IdSuggester.suggest(adventure, "person", "???").id());
        assertNull(IdSuggester.suggest(adventure, "person", "").id());
    }

    @Test
    void anIdThatAlreadyExistsIsReportedAsTaken() {
        IdSuggester.Suggestion suggestion = IdSuggester.suggest(adventure, "person", "Ulf Stetten");

        assertEquals("person.ulf-stetten", suggestion.id());
        assertTrue(suggestion.taken());
        assertFalse(suggestion.usable());
    }

    /**
     * The case the distance rule exists for. A name one character off an existing one is not
     * quietly numbered - numbering would produce exactly the near-collision being avoided.
     */
    @Test
    void aNameTooCloseToAnExistingIdNamesTheIdInTheWay() {
        IdSuggester.Suggestion suggestion = IdSuggester.suggest(adventure, "person", "Ulf Stetteo");

        assertFalse(suggestion.taken());
        assertFalse(suggestion.usable());
        assertTrue(suggestion.tooClose().contains("person.ulf-stetten"), suggestion.tooClose().toString());
    }

    @Test
    void aFreshNameIsUsable() {
        assertTrue(IdSuggester.suggest(adventure, "location", "Tiefe Mine").usable());
    }

    @Test
    void theGenericDialogCountsAsOccupied() {
        assertFalse(IdSuggester.suggest(adventure, "dialog", "Small Talk").usable());
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That saving cannot leave the author with a file that will not open.
 * <p>
 * This is the property the whole store is built around, so it is what the tests are about: a
 * rejected save must change nothing – not the file, not the adventure in memory – and an accepted
 * one must be on disk before the editor is told it worked.
 */
class AdventureStoreTest {

    @TempDir
    Path directory;

    private Path file;
    private ModelSchema schema;
    private AdventureDocument writer;

    @BeforeEach
    void seedAFileToWorkOn() {
        schema = new ModelSchema();
        writer = new AdventureDocument(schema);
        file = directory.resolve("buchenhain.yaml");
        AdventureYaml.toFile(file, AdventureYaml.fromClasspath(Buchenhain.RESOURCE));
    }

    private AdventureStore storeOnFile() {
        return new AdventureStore(file, Buchenhain.RESOURCE, schema, writer);
    }

    @Test
    void anAcceptedSaveReachesTheFileAndTheAdventure() {
        AdventureStore store = storeOnFile();
        Map<String, Object> document = store.document();
        firstOf(document, "persons").put("name", "Ulf von Stetten");

        AdventureStore.SaveResult result = store.save(document);

        assertTrue(result.saved(), () -> "not saved: " + result.errors());
        assertEquals("Ulf von Stetten", store.adventure().getPersons().getFirst().name());
        assertEquals("Ulf von Stetten", firstOf(AdventureYaml.fromFile(file), "persons").get("name"));
    }

    @Test
    void warningsDoNotBlockASave() {
        AdventureStore store = storeOnFile();

        AdventureStore.SaveResult result = store.save(store.document());

        assertTrue(result.saved());
        assertFalse(result.warnings().isEmpty(), "Buchenhain has unfinished content to warn about");
    }

    @Test
    void aBrokenReferenceIsRefusedAndChangesNothing() {
        AdventureStore store = storeOnFile();
        String before = AdventureYaml.write(AdventureYaml.fromFile(file));
        Map<String, Object> document = store.document();
        firstChapterPersonCondition(document).put("person", "person.gibt-es-nicht");

        AdventureStore.SaveResult result = store.save(document);

        assertFalse(result.saved());
        assertTrue(result.errors().toString().contains("person.gibt-es-nicht"), result.errors().toString());
        assertEquals(before, AdventureYaml.write(AdventureYaml.fromFile(file)), "the file was touched");
        assertEquals("Ulf Stetten", store.adventure().getPersons().getFirst().name());
    }

    /**
     * A validator error, as opposed to a structural one, comes back as the individual complaints
     * rather than one blob - the author fixes them one at a time.
     * <p>
     * The change here is structurally sound on purpose: a new item whose id is one character from
     * an existing one. Everything resolves, and only the distance rule objects.
     */
    @Test
    @SuppressWarnings("unchecked")
    void aValidatorErrorIsReportedItemByItem() {
        AdventureStore store = storeOnFile();
        Map<String, Object> document = store.document();
        ((List<Object>) document.get("items")).add(Map.of(
                "id", "item.eisenschluesse",
                "name", "ein zweiter Schlüssel",
                "description", "einer zu viel"));

        AdventureStore.SaveResult result = store.save(document);

        assertFalse(result.saved());
        assertTrue(result.errors().stream().anyMatch(error -> error.contains("apart")),
                result.errors().toString());
        assertFalse(result.errors().stream().anyMatch(error -> error.contains("error(s):")),
                "expected the individual errors, not the joined message: " + result.errors());
    }

    @Test
    void aReadOnlyStoreRefusesToSaveAndSaysWhy() {
        AdventureStore store = new AdventureStore(null, Buchenhain.RESOURCE, schema, writer);

        AdventureStore.SaveResult result = store.save(store.document());

        assertFalse(store.writable());
        assertFalse(result.saved());
        assertTrue(result.errors().toString().contains("rpg.adventure.file"), result.errors().toString());
    }

    @Test
    void reloadingThrowsAwayWhatWasNotSaved() {
        AdventureStore store = storeOnFile();
        Map<String, Object> document = store.document();
        firstOf(document, "persons").put("name", "Nie gespeichert");
        assertNotEquals("Nie gespeichert", store.adventure().getPersons().getFirst().name());

        store.reload();

        assertEquals("Ulf Stetten", store.adventure().getPersons().getFirst().name());
    }

    /** A file that is configured but not there yet: read the packaged one, create it on first save. */
    @Test
    void aConfiguredFileThatDoesNotExistYetIsCreatedBySaving() {
        Path fresh = directory.resolve("neu.yaml");
        AdventureStore store = new AdventureStore(fresh, Buchenhain.RESOURCE, schema, writer);

        assertTrue(store.writable());
        assertTrue(store.save(store.document()).saved());
        assertEquals(store.document(), AdventureYaml.fromFile(fresh));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstOf(Map<String, Object> document, String section) {
        return (Map<String, Object>) ((List<Object>) document.get(section)).getFirst();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> firstChapterPersonCondition(Map<String, Object> document) {
        Map<String, Object> chapter = firstOf(document, "chapters");
        return (Map<String, Object>) ((List<Object>) chapter.get("personConditions")).getFirst();
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureDefinitionException;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That nothing is lost between the file and the adventure.
 * <p>
 * Since the reference adventure <em>is</em> a file now, the test that matters is the whole trip:
 * read it, build it, write it out again, and compare. Anything the reader drops, mistypes or
 * resolves to the wrong thing shows up as a difference in that comparison – there is no
 * hand-written Java version left to check against.
 */
class DocumentAdventureTest {

    private final ModelSchema schema = new ModelSchema();
    private final AdventureDocument writer = new AdventureDocument(schema);

    /**
     * The shipped file, read and written again. This is what guards the reference adventure
     * against a regression in either half of the format: whatever the reader drops or the writer
     * spells differently shows up as a difference against the file on disk.
     */
    @Test
    void theShippedFileSurvivesBeingReadAndWrittenAgain() {
        Map<String, Object> onDisk = AdventureYaml.fromClasspath(Buchenhain.RESOURCE);

        Map<String, Object> afterTheTrip = writer.write(new DocumentAdventure(onDisk, schema).build());

        assertEquals(onDisk, afterTheTrip);
    }

    /** And the text of the file too, so a re-export is a no-op rather than a diff. */
    @Test
    void writingTheFileAgainProducesTheSameText() {
        Map<String, Object> onDisk = AdventureYaml.fromClasspath(Buchenhain.RESOURCE);

        String rewritten = AdventureYaml.write(writer.write(new DocumentAdventure(onDisk, schema).build()));

        assertEquals(onDisk, AdventureYaml.parse(rewritten));
    }

    @Test
    void theWholeAdventureSurvivesTheTripThroughYaml() {
        Map<String, Object> expected = writer.write(new Buchenhain().build());

        Adventure reloaded = new DocumentAdventure(AdventureYaml.parse(AdventureYaml.write(expected)), schema).build();

        assertEquals(expected, writer.write(reloaded));
    }

    /**
     * The engine's own conditions have to come back as the very constants, not as copies that
     * happen to carry the same id - a chapter comparing them by identity would otherwise break.
     */
    @Test
    void builtinConditionsResolveToTheEngineConstants() {
        Adventure reloaded = load(writer.write(new Buchenhain().build()));

        Condition dayTime = reloaded.getChapters().getFirst().locationConditions().stream()
                .filter(entry -> entry.condition().id().equals(Condition.DAY_TIME.id()))
                .findFirst().orElseThrow().condition();

        assertSame(Condition.DAY_TIME, dayTime);
    }

    @Test
    void aReferenceThatNamesNothingSaysWhereItIs() {
        Map<String, Object> document = writer.write(new Buchenhain().build());
        firstChapterPersonCondition(document).put("person", "person.gibt-es-nicht");

        AdventureDefinitionException thrown =
                assertThrows(AdventureDefinitionException.class, () -> load(document));

        assertTrue(thrown.getMessage().contains("person.gibt-es-nicht"), thrown.getMessage());
    }

    @Test
    void aPolymorphicEntryWithoutATypeSaysWhatIsMissing() {
        Map<String, Object> document = writer.write(new Buchenhain().build());
        firstOf(document, "flags").remove("type");

        AdventureDefinitionException thrown =
                assertThrows(AdventureDefinitionException.class, () -> load(document));

        assertTrue(thrown.getMessage().contains("type"), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("KnowledgeFlag"), thrown.getMessage());
    }

    /**
     * A half-written adventure has to load. It is the normal state of one being written, and an
     * author who has not got to the items yet should not have to write an empty list to say so.
     */
    @Test
    void aDocumentThatLeavesSectionsOutStillLoads() {
        Adventure barely = load(Map.of(
                "metadata", Map.of("title", "Kaum ein Abenteuer", "author", "Test"),
                "plotSummary", "noch nichts",
                "locations", List.of(Map.of(
                        "id", "location.hier", "name", "Hier", "description", "nichts weiter",
                        "destinationIds", List.of(), "triggerIds", List.of())),
                "chapters", List.of(Map.of(
                        "id", "chapter.der-anfang", "name", "Der Anfang", "summary", "noch nichts",
                        "intro", Map.of("intro", "Ihr steht da.",
                                "startLocation", "location.hier", "startTime", "MORNING"),
                        "locationConditions", List.of(Map.of(
                                "location", "location.hier", "condition", "condition.immer-wahr")),
                        "personConditions", List.of(),
                        "dialogConditions", List.of(),
                        "investigateConditions", List.of(),
                        "chapterFinishedCondition", "condition.immer-wahr"))));

        assertEquals(List.of(), barely.getPersons());
        assertEquals(List.of(), barely.getItems());
        assertEquals(List.of(), barely.getFlags());
        assertEquals(1, barely.getLocations().size());
        assertEquals(1, barely.getChapters().size());
    }

    private Adventure load(Map<String, Object> document) {
        return new DocumentAdventure(document, schema).build();
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

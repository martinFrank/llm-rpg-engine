package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That the document is complete enough to be the file the editor writes.
 * <p>
 * The load side does not exist yet, so the property worth pinning down now is the one it will
 * depend on: everything the document refers to is also <em>in</em> the document. If a reference
 * could point at something only the Java definition knows, the file would not be a full account
 * of the adventure and reading it back would quietly lose something.
 */
class AdventureDocumentTest {

    private static Adventure adventure;
    private static ModelSchema schema;
    private static Map<String, Object> document;

    @BeforeAll
    static void buildDocument() {
        adventure = new Buchenhain().build();
        schema = new ModelSchema();
        document = new AdventureDocument(schema).write(adventure);
    }

    @Test
    void everyEntityOfTheAdventureIsWrittenExactlyOnce() {
        for (ModelSchema.Section section : ModelSchema.SECTIONS) {
            List<? extends Identifiable> expected = section.read().apply(adventure);
            List<?> written = (List<?>) document.get(section.name());

            assertEquals(expected.size(), written.size(),
                    "section '" + section.name() + "' has a different number of entries than the adventure");
            List<String> writtenIds = written.stream().map(entry -> id(entry)).toList();
            List<String> expectedIds = expected.stream().map(entry -> entry.id().value()).toList();
            assertEquals(expectedIds, writtenIds, "section '" + section.name() + "' wrote different ids");
        }
    }

    @Test
    void everyReferenceInTheDocumentResolvesWithinIt() {
        Set<String> known = definedIds();
        List<String> dangling = new ArrayList<>();
        collectReferences(document, known, dangling);

        assertTrue(dangling.isEmpty(),
                "the document refers to ids it does not define: " + dangling);
    }

    @Test
    void polymorphicSectionsSayWhichVariantEachEntryIs() {
        for (ModelSchema.Section section : ModelSchema.SECTIONS) {
            if (!section.polymorphic()) {
                continue;
            }
            for (Object entry : (List<?>) document.get(section.name())) {
                Object type = ((Map<?, ?>) entry).get("type");
                assertTrue(type instanceof String variant && !variant.isBlank(),
                        "entry " + id(entry) + " of polymorphic section '" + section.name()
                                + "' does not say which type it is - reading it back would be a guess");
            }
        }
    }

    @Test
    void theSchemaDescribesEveryTypeTheDocumentUses() {
        Map<String, ModelSchema.Type> types = schema.types();
        assertFalse(types.isEmpty());
        types.forEach((name, type) ->
                assertTrue(type != null, "type '" + name + "' was reserved but never described"));
    }

    /** Every id the document defines: the entities of each section plus the engine's own conditions. */
    private Set<String> definedIds() {
        Set<String> ids = new LinkedHashSet<>();
        for (ModelSchema.Section section : ModelSchema.SECTIONS) {
            ((List<?>) document.get(section.name())).forEach(entry -> ids.add(id(entry)));
        }
        ((List<?>) document.get("builtinConditions")).forEach(entry -> ids.add(id(entry)));
        return ids;
    }

    /**
     * Walks the document and reports every string that looks like an id but names nothing. Going
     * by shape rather than by schema is the point: it also catches an id that ended up in a plain
     * text field, which is exactly the copy-paste the ids were introduced to make visible.
     */
    private void collectReferences(Object node, Set<String> known, List<String> dangling) {
        switch (node) {
            case Map<?, ?> map -> map.forEach((key, value) -> {
                if (!"id".equals(key)) {
                    collectReferences(value, known, dangling);
                }
            });
            case List<?> list -> list.forEach(element -> collectReferences(element, known, dangling));
            case String text -> {
                if (looksLikeAnId(text) && !known.contains(text)) {
                    dangling.add(text);
                }
            }
            case null, default -> {
            }
        }
    }

    private static boolean looksLikeAnId(String text) {
        return ModelSchema.SECTIONS.stream()
                .anyMatch(section -> text.startsWith(section.namespace() + "."))
                && !text.contains(" ")
                && !text.contains("\n");
    }

    private static String id(Object entry) {
        return (String) ((Map<?, ?>) entry).get("id");
    }
}

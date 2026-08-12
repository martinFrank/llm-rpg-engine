package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A built adventure as a plain tree of maps, lists and strings – the shape that both JSON (for
 * the editor) and YAML (for the file on disk) are written from, so the two can never drift apart.
 *
 * <h2>The one rule</h2>
 * Anything with an id is written as its id, except in the section that defines it. That single
 * rule replaces a decision per reference: a {@code PersonCondition} holding a {@code Person}
 * becomes {@code person: person.ulf-stetten}, and a record without an id – an {@code Intro}, a
 * {@code SkillCheck} – is written out where it stands. Both hold for types that do not exist yet.
 *
 * <h2>Built-in conditions</h2>
 * A chapter may refer to {@link Condition#ALWAYS_TRUE} and friends, which belong to the engine
 * and appear in no adventure's condition list. They are written into their own part of the
 * document so that a reader – the editor's dropdowns above all – can resolve every reference it
 * meets, and so that they are visibly not the author's to edit.
 */
public final class AdventureDocument {

    private final ModelSchema schema;

    public AdventureDocument(ModelSchema schema) {
        this.schema = schema;
    }

    public Map<String, Object> write(Adventure adventure) {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("metadata", object(adventure.getMetadata(), false));
        document.put("plotSummary", adventure.getPlotSummary());
        for (ModelSchema.Section section : ModelSchema.SECTIONS) {
            List<Object> entries = new ArrayList<>();
            for (Identifiable entry : section.read().apply(adventure)) {
                entries.add(object(entry, section.polymorphic()));
            }
            document.put(section.name(), entries);
        }
        document.put("builtinConditions", builtinConditions());
        return document;
    }

    /** The conditions the engine declares as constants; see {@link Builtins}. */
    private List<Object> builtinConditions() {
        List<Object> builtins = new ArrayList<>();
        Builtins.conditions().forEach((id, condition) -> {
            Map<String, Object> written = object(condition, true);
            written.put("constant", Builtins.constantName(id));
            builtins.add(written);
        });
        return builtins;
    }

    private Map<String, Object> object(Object value, boolean withType) {
        Map<String, Object> written = new LinkedHashMap<>();
        if (withType) {
            written.put("type", value.getClass().getSimpleName());
        }
        for (ModelSchema.Field field : schema.type(value.getClass()).fields()) {
            written.put(field.name(), value(field, schema.read(value, field.name())));
        }
        return written;
    }

    private Object value(ModelSchema.Field field, Object raw) {
        if (raw == null) {
            return null;
        }
        if (!field.list()) {
            return single(field, raw);
        }
        List<Object> written = new ArrayList<>();
        for (Object element : (List<?>) raw) {
            written.add(single(field, element));
        }
        return written;
    }

    private Object single(ModelSchema.Field field, Object raw) {
        if (raw == null) {
            return null;
        }
        return switch (field.kind()) {
            case ID -> ((Id) raw).value();
            case REF -> reference(raw);
            case ENUM -> raw.toString();
            case EMBEDDED -> object(raw, hasVariants(field.type()));
            case TEXT, NUMBER, BOOLEAN -> raw;
        };
    }

    /** A reference is either the object itself or – where the model holds ids to break a cycle – an id. */
    private static String reference(Object raw) {
        if (raw instanceof Id id) {
            return id.value();
        }
        return ((Identifiable) raw).id().value();
    }

    private boolean hasVariants(String typeName) {
        ModelSchema.Type type = schema.types().get(typeName);
        return type != null && !type.variants().isEmpty();
    }
}

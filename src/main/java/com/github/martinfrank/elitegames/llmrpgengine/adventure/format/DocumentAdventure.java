package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureDefinitionException;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.BaseAdventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Chapter;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Investigation;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Item;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Metadata;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Trigger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * An adventure read from a document – the other half of {@link AdventureDocument}.
 * <p>
 * It is a {@link BaseAdventure} on purpose rather than its own {@code Adventure}. That inherits
 * the whole of the Java path: the same registry, the same "did you mean" on a mistyped id, the
 * same validator run at the end of {@code build()}. A file therefore fails the same way a
 * hand-written adventure does, and there is no second set of rules to keep in step.
 *
 * <h2>Why no second pass</h2>
 * {@code build()} fills the sections in the order of {@link ModelSchema#SECTIONS}, which is the
 * dependency order. By the time an entry is read, everything it refers to by object is already
 * registered; the two places that would look forward hold an {@link Id} instead and are checked
 * by the validator. So reading a document is a single walk, and a reference that points nowhere
 * says so where it stands.
 */
public class DocumentAdventure extends BaseAdventure {

    private final Map<String, Object> document;
    private final ModelSchema schema;

    public DocumentAdventure(Map<String, Object> document) {
        this(document, new ModelSchema());
    }

    public DocumentAdventure(Map<String, Object> document, ModelSchema schema) {
        this.document = document;
        this.schema = schema;
    }

    @Override
    public String getPlotSummary() {
        return (String) document.get("plotSummary");
    }

    @Override
    public Metadata getMetadata() {
        Object metadata = document.get("metadata");
        if (metadata == null) {
            return new Metadata("(ohne Titel)", "(ohne Autor)");
        }
        return (Metadata) instantiate(Metadata.class, asMap(metadata, "metadata"), "metadata");
    }

    @Override protected List<Location> defineLocations() { return section("locations"); }
    @Override protected List<Person> definePersons() { return section("persons"); }
    @Override protected List<Item> defineItems() { return section("items"); }
    @Override protected List<Flag<?>> defineFlags() { return section("flags"); }
    @Override protected List<Trigger> defineTriggers() { return section("triggers"); }
    @Override protected List<Dialog> defineDialogs() { return section("dialogs"); }
    @Override protected List<Investigation> defineInvestigations() { return section("investigations"); }
    @Override protected List<Condition> defineConditions() { return section("conditions"); }
    @Override protected List<Chapter> defineChapters() { return section("chapters"); }

    /* ---------------------------------------------------------------- reading */

    @SuppressWarnings("unchecked")
    private <T> List<T> section(String name) {
        ModelSchema.Section section = ModelSchema.SECTIONS.stream()
                .filter(candidate -> candidate.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no section '" + name + "'"));

        Object raw = document.get(name);
        if (raw == null) {
            return List.of();       // a section left out is an empty one, not a broken document
        }
        List<T> entries = new ArrayList<>();
        List<?> nodes = asList(raw, name);
        for (int i = 0; i < nodes.size(); i++) {
            Map<String, Object> node = asMap(nodes.get(i), name + "[" + i + "]");
            String where = name + "[" + i + "]" + (node.get("id") == null ? "" : " (" + node.get("id") + ")");
            entries.add((T) instantiate(typeOf(section, node, where), node, where));
        }
        return entries;
    }

    /** Which record an entry is: the section's type, or for a polymorphic section what it says it is. */
    private Class<?> typeOf(ModelSchema.Section section, Map<String, Object> node, String where) {
        if (!section.polymorphic()) {
            return section.type();
        }
        Object variant = node.get("type");
        if (variant == null) {
            throw new AdventureDefinitionException(where + ": '" + section.name()
                    + "' holds several kinds of thing, so the entry must say which one it is"
                    + " - add a 'type', one of " + schema.type(section.type()).variants());
        }
        return schema.classOf(String.valueOf(variant));
    }

    private Object instantiate(Class<?> type, Map<String, Object> node, String where) {
        List<ModelSchema.Field> fields = schema.type(type).fields();
        RecordComponent[] components = type.getRecordComponents();
        Object[] arguments = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            ModelSchema.Field field = fields.get(i);
            arguments[i] = convert(field, node.get(field.name()),
                    components[i].getType(), where + "." + field.name());
        }
        return construct(type, components, arguments, where);
    }

    private Object convert(ModelSchema.Field field, Object raw, Class<?> target, String where) {
        if (raw == null) {
            if (target.isPrimitive()) {
                throw new AdventureDefinitionException(
                        where + " is missing, and it has no sensible empty value");
            }
            return null;
        }
        if (field.list()) {
            List<Object> converted = new ArrayList<>();
            List<?> elements = asList(raw, where);
            for (int i = 0; i < elements.size(); i++) {
                converted.add(single(field, elements.get(i), null, where + "[" + i + "]"));
            }
            return List.copyOf(converted);
        }
        return single(field, raw, target, where);
    }

    private Object single(ModelSchema.Field field, Object raw, Class<?> target, String where) {
        return switch (field.kind()) {
            case ID -> id(raw, where);
            case REF -> field.byId()
                    ? id(raw, where)
                    : resolve(field.type(), id(raw, where), where);
            case ENUM -> enumValue(field, raw, where);
            case EMBEDDED -> instantiate(embeddedType(field, raw), asMap(raw, where), where);
            case TEXT -> String.valueOf(raw);
            case BOOLEAN -> asBoolean(raw, where);
            case NUMBER -> number(raw, target, where);
        };
    }

    /** An embedded record may itself be polymorphic, in which case the node says what it is. */
    private Class<?> embeddedType(ModelSchema.Field field, Object raw) {
        Object variant = raw instanceof Map<?, ?> map ? map.get("type") : null;
        return variant == null ? schema.classOf(field.type()) : schema.classOf(String.valueOf(variant));
    }

    /**
     * The object an id names. Engine constants come first: they belong to no adventure, so the
     * registry does not know them, but a chapter refers to them like anything else.
     */
    private Identifiable resolve(String typeName, Id id, String where) {
        Condition builtin = Builtins.conditions().get(id);
        if (builtin != null && isConditionTarget(typeName)) {
            return builtin;
        }
        if (Identifiable.class.getSimpleName().equals(typeName)) {
            return resolveAnywhere(id, where);
        }
        return schema.sectionFor(typeName).resolve().apply(this, id);
    }

    private boolean isConditionTarget(String typeName) {
        return Condition.class.getSimpleName().equals(typeName)
                || Identifiable.class.getSimpleName().equals(typeName);
    }

    /**
     * For the one reference the model leaves open – what a chapter lets the player investigate is
     * a place or a person – so every section is tried. Whether the thing found actually belongs
     * there is a rule of the chapter, and the validator owns it.
     */
    private Identifiable resolveAnywhere(Id id, String where) {
        for (ModelSchema.Section section : ModelSchema.SECTIONS) {
            try {
                return section.resolve().apply(this, id);
            } catch (RuntimeException ignored) {
                // not this kind of thing, or its section is not filled yet - try the next
            }
        }
        throw new AdventureDefinitionException(where + ": '" + id + "' is nothing this adventure defines");
    }

    /* ---------------------------------------------------------------- coercion */

    private static Id id(Object raw, String where) {
        try {
            return Id.of(String.valueOf(raw));
        } catch (IllegalArgumentException e) {
            throw new AdventureDefinitionException(where + ": " + e.getMessage());
        }
    }

    private Object enumValue(ModelSchema.Field field, Object raw, String where) {
        String name = String.valueOf(raw);
        for (Object constant : schema.classOf(field.type()).getEnumConstants()) {
            if (constant.toString().equals(name)) {
                return constant;
            }
        }
        throw new AdventureDefinitionException(where + ": '" + name + "' is not one of "
                + field.options());
    }

    private static boolean asBoolean(Object raw, String where) {
        if (raw instanceof Boolean value) {
            return value;
        }
        throw new AdventureDefinitionException(where + ": expected true or false, found '" + raw + "'");
    }

    private static Object number(Object raw, Class<?> target, String where) {
        if (!(raw instanceof Number value)) {
            throw new AdventureDefinitionException(where + ": expected a number, found '" + raw + "'");
        }
        if (target == double.class || target == Double.class) return value.doubleValue();
        if (target == float.class || target == Float.class) return value.floatValue();
        if (target == long.class || target == Long.class) return value.longValue();
        if (target == int.class || target == Integer.class) return value.intValue();
        if (target == short.class || target == Short.class) return value.shortValue();
        if (target == byte.class || target == Byte.class) return value.byteValue();
        return value;
    }

    private static Object construct(Class<?> type, RecordComponent[] components, Object[] arguments,
                                    String where) {
        Class<?>[] parameters = Arrays.stream(components).map(RecordComponent::getType)
                .toArray(Class<?>[]::new);
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameters);
            constructor.setAccessible(true);
            return constructor.newInstance(arguments);
        } catch (InvocationTargetException e) {
            throw new AdventureDefinitionException(where + ": " + type.getSimpleName()
                    + " rejected it - " + e.getTargetException().getMessage());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot build a " + type.getSimpleName() + " at " + where, e);
        }
    }

    /* ---------------------------------------------------------------- shapes */

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object raw, String where) {
        if (raw instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new AdventureDefinitionException(where + ": expected a block of fields, found '" + raw + "'");
    }

    private static List<?> asList(Object raw, String where) {
        if (raw instanceof List<?> list) {
            return list;
        }
        throw new AdventureDefinitionException(where + ": expected a list, found '" + raw + "'");
    }
}

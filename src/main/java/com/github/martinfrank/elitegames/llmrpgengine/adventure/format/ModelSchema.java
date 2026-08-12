package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureDefinitionException;
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
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Ref;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Trigger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The model, described by reading it rather than by restating it.
 * <p>
 * This is the single reason the editor survives a change to the adventure model. Every type it
 * edits is a {@code record}, so the fields, their types and the references between them can be
 * recovered with reflection. A new component on {@link Person} therefore reaches the schema, the
 * document writer and the form in the browser without any of the three being touched.
 *
 * <h2>What reflection cannot see</h2>
 * Two things, and both are declared rather than guessed. A bare {@link Id} does not say what it
 * names, which {@link Ref} supplies. And an interface does not list its implementations, which a
 * classpath scan supplies – so a seventh {@link Condition} appears in the editor by existing,
 * with nothing to register.
 * <p>
 * The one list kept by hand is {@link #SECTIONS}, because a new <em>kind</em> of thing is a
 * decision about the model – it needs a registry, a namespace and a place in the validator – and
 * not something that should quietly appear because a class was added.
 */
public final class ModelSchema {

    /** Where implementations of a model interface are looked for. */
    private static final String MODEL_PACKAGE = "com.github.martinfrank.elitegames.llmrpgengine.adventure";

    /** What a field holds, as far as an editor needs to care. */
    public enum Kind {
        /** The entity's own id. */
        ID,
        TEXT,
        NUMBER,
        BOOLEAN,
        /** A fixed set of values; see {@link Field#options()}. */
        ENUM,
        /** Names another entity by id; see {@link Field#type()}. */
        REF,
        /** A record that has no id of its own and is written out in place. */
        EMBEDDED
    }

    /**
     * One component of a record. {@code list} is kept apart from {@code kind} so that a list of
     * references and a single reference are the same case with one flag between them.
     * <p>
     * {@code byId} tells the two shapes of a reference apart: the model usually holds the object,
     * but in the places that would close a cycle it holds an {@link Id}. Both are written as an
     * id, so only a reader needs to care.
     */
    public record Field(String name, Kind kind, boolean list, boolean byId, String type,
                        List<String> options) {
    }

    /**
     * One record (or interface) of the model. An {@code entity} has an id and lives in a section
     * of the document; anything else is written inside its owner. An interface contributes no
     * fields of its own – its {@code variants} do.
     */
    public record Type(String name, boolean entity, List<Field> fields, List<String> variants) {
    }

    /**
     * One top-level list of the document: how to read it off a built adventure, and how to look
     * one entry up by id. The order of {@link #SECTIONS} is the dependency order – see
     * {@code BaseAdventure.build()} – which is what lets a reader fill the sections in turn and
     * find every object reference already registered.
     */
    public record Section(String name, String namespace, Class<?> type,
                          Function<Adventure, List<? extends Identifiable>> read,
                          BiFunction<Adventure, Id, ? extends Identifiable> resolve) {

        /** Whether entries carry a {@code type} discriminator, i.e. whether the section is polymorphic. */
        public boolean polymorphic() {
            return type.isInterface();
        }
    }

    public static final List<Section> SECTIONS = List.of(
            new Section("locations", "location", Location.class,
                    Adventure::getLocations, Adventure::getLocation),
            new Section("persons", "person", Person.class,
                    Adventure::getPersons, Adventure::getPerson),
            new Section("items", "item", Item.class,
                    Adventure::getItems, Adventure::getItem),
            new Section("flags", "flag", Flag.class,
                    Adventure::getFlags, Adventure::getFlag),
            new Section("triggers", "trigger", Trigger.class,
                    Adventure::getTriggers, Adventure::getTrigger),
            new Section("dialogs", "dialog", Dialog.class,
                    Adventure::getDialogs, Adventure::getDialog),
            new Section("investigations", "investigation", Investigation.class,
                    Adventure::getInvestigations, Adventure::getInvestigation),
            new Section("conditions", "condition", Condition.class,
                    Adventure::getConditions, Adventure::getCondition),
            new Section("chapters", "chapter", Chapter.class,
                    Adventure::getChapters, (adventure, id) -> adventure.getChapters().stream()
                    .filter(chapter -> chapter.id().equals(id)).findFirst()
                    .orElseThrow(() -> new AdventureDefinitionException("unknown chapter '" + id + "'"))));

    /**
     * Types that hang off the document itself rather than off a section, so nothing references
     * them and the walk would never reach them.
     */
    private static final List<Class<?>> DOCUMENT_TYPES = List.of(Metadata.class);

    private final Map<String, Type> types = new LinkedHashMap<>();
    private final Map<String, Class<?>> classes = new LinkedHashMap<>();
    private final Map<Class<?>, Map<String, Method>> accessors = new ConcurrentHashMap<>();

    public ModelSchema() {
        SECTIONS.forEach(section -> describe(section.type()));
        DOCUMENT_TYPES.forEach(this::describe);
    }

    /** Every type reachable from the sections, keyed by simple class name. */
    public Map<String, Type> types() {
        return Map.copyOf(types);
    }

    /** The class a document names by its simple name, e.g. the {@code type} of a polymorphic entry. */
    public Class<?> classOf(String name) {
        Class<?> clazz = classes.get(name);
        if (clazz == null) {
            throw new AdventureDefinitionException("unknown type '" + name + "'"
                    + " - the document names something the model does not have"
                    + " (known: " + String.join(", ", classes.keySet()) + ")");
        }
        return clazz;
    }

    /** The section a reference of the given target type is looked up in. */
    public Section sectionFor(String typeName) {
        Class<?> target = classOf(typeName);
        return SECTIONS.stream()
                .filter(section -> section.type() == target)
                .findFirst()
                .orElseThrow(() -> new AdventureDefinitionException(
                        "'" + typeName + "' is referenced by id but is not a section of the document"
                                + " - add it to ModelSchema.SECTIONS"));
    }

    public Type type(Class<?> clazz) {
        Type described = types.get(clazz.getSimpleName());
        if (described == null) {
            throw new IllegalStateException("no schema for " + clazz.getName()
                    + " - it is not reachable from ModelSchema.SECTIONS");
        }
        return described;
    }

    /** Reads one component off a record instance, by the name the schema knows it under. */
    public Object read(Object owner, String field) {
        Method accessor = accessors
                .computeIfAbsent(owner.getClass(), ModelSchema::accessorsOf)
                .get(field);
        if (accessor == null) {
            throw new IllegalStateException(owner.getClass().getSimpleName() + " has no component '" + field + "'");
        }
        try {
            return accessor.invoke(owner);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot read " + owner.getClass().getSimpleName() + "." + field, e);
        }
    }

    private void describe(Class<?> clazz) {
        String name = clazz.getSimpleName();
        if (types.containsKey(name)) {
            return;
        }
        types.put(name, null);                  // reserved, so a cyclic reference stops here
        classes.put(name, clazz);

        List<Field> fields = List.of();
        List<String> variants = List.of();
        if (clazz.isInterface()) {
            List<Class<?>> implementations = implementationsOf(clazz);
            implementations.forEach(this::describe);
            variants = implementations.stream().map(Class::getSimpleName).toList();
        } else if (clazz.isRecord()) {
            fields = Arrays.stream(clazz.getRecordComponents()).map(this::describe).toList();
        }
        types.put(name, new Type(name, Identifiable.class.isAssignableFrom(clazz), fields, variants));
    }

    private Field describe(RecordComponent component) {
        boolean list = List.class.isAssignableFrom(component.getType());
        Class<?> declared = list
                ? rawOf(elementTypeOf(component))
                : component.getType();

        // A bare id carries no target in its type - that is what @Ref is for.
        Ref ref = component.getAnnotation(Ref.class);
        boolean byId = ref != null && declared == Id.class;
        Class<?> element = ref != null ? ref.value() : declared;

        String name = component.getName();
        if (element == Id.class) {
            return new Field(name, Kind.ID, list, false, null, List.of());
        }
        if (Identifiable.class.isAssignableFrom(element)) {
            // Identifiable itself is the "any entity" case (InvestigateCondition.subject); it has
            // no fields to describe, and which entities actually fit is the validator's business.
            if (element != Identifiable.class) {
                describe(element);
            }
            return new Field(name, Kind.REF, list, byId, element.getSimpleName(), List.of());
        }
        if (element.isEnum()) {
            classes.putIfAbsent(element.getSimpleName(), element);
            List<String> options = Arrays.stream(element.getEnumConstants()).map(Object::toString).toList();
            return new Field(name, Kind.ENUM, list, false, element.getSimpleName(), options);
        }
        if (element == String.class) {
            return new Field(name, Kind.TEXT, list, false, null, List.of());
        }
        if (element == boolean.class || element == Boolean.class) {
            return new Field(name, Kind.BOOLEAN, list, false, null, List.of());
        }
        if (element.isPrimitive() || Number.class.isAssignableFrom(element)) {
            return new Field(name, Kind.NUMBER, list, false, null, List.of());
        }
        if (element.isRecord()) {
            describe(element);
            return new Field(name, Kind.EMBEDDED, list, false, element.getSimpleName(), List.of());
        }
        throw new IllegalStateException("no schema rule for " + element.getName()
                + " (component '" + name + "' of " + component.getDeclaringRecord().getSimpleName() + ")"
                + " - teach ModelSchema about it, or model it as a record");
    }

    private static java.lang.reflect.Type elementTypeOf(RecordComponent component) {
        if (component.getGenericType() instanceof ParameterizedType parameterized) {
            return parameterized.getActualTypeArguments()[0];
        }
        return Object.class;
    }

    /** The class behind a generic type – {@code Flag<?>}, {@code R extends Identifiable} and friends. */
    private static Class<?> rawOf(java.lang.reflect.Type type) {
        return switch (type) {
            case Class<?> clazz -> clazz;
            case ParameterizedType parameterized -> rawOf(parameterized.getRawType());
            case WildcardType wildcard -> rawOf(wildcard.getUpperBounds()[0]);
            case TypeVariable<?> variable -> rawOf(variable.getBounds()[0]);
            default -> Object.class;
        };
    }

    private static List<Class<?>> implementationsOf(Class<?> interfaceType) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(interfaceType));
        List<Class<?>> found = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(MODEL_PACKAGE)) {
            Class<?> candidate = load(definition.getBeanClassName());
            if (!candidate.isInterface() && !Modifier.isAbstract(candidate.getModifiers())) {
                found.add(candidate);
            }
        }
        found.sort(Comparator.comparing(Class::getSimpleName));
        return List.copyOf(found);
    }

    private static Class<?> load(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("scanned but cannot load " + className, e);
        }
    }

    private static Map<String, Method> accessorsOf(Class<?> clazz) {
        Map<String, Method> byName = new LinkedHashMap<>();
        RecordComponent[] components = clazz.getRecordComponents();
        if (components != null) {
            for (RecordComponent component : components) {
                byName.put(component.getName(), component.getAccessor());
            }
        }
        return Map.copyOf(byName);
    }
}

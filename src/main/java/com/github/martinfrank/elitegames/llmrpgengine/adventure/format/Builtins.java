package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The conditions the engine brings along – {@link Condition#ALWAYS_TRUE} and the two times of
 * day – which belong to no adventure and appear in no adventure's condition list.
 * <p>
 * They matter to both halves of the file format, which is why they are gathered in one place. On
 * the way out they have to be written down, or the document would refer to ids it does not
 * define. On the way back in they have to resolve to the very same constants, or a loaded
 * adventure would hold conditions that merely look like the engine's.
 * <p>
 * Read off the interface rather than listed here, so a fourth constant is picked up by existing.
 */
final class Builtins {

    private static final Map<Id, Condition> CONDITIONS;
    private static final Map<Id, String> CONSTANT_NAMES;

    static {
        Map<Id, Condition> conditions = new LinkedHashMap<>();
        Map<Id, String> names = new LinkedHashMap<>();
        for (Field field : Condition.class.getFields()) {
            if (isConstant(field)) {
                Condition condition = read(field);
                conditions.put(condition.id(), condition);
                names.put(condition.id(), field.getName());
            }
        }
        CONDITIONS = Collections.unmodifiableMap(conditions);
        CONSTANT_NAMES = Collections.unmodifiableMap(names);
    }

    private Builtins() {
    }

    /** The engine's conditions by id, in declaration order. */
    static Map<Id, Condition> conditions() {
        return CONDITIONS;
    }

    /** The field name a constant was declared under, for the document to name it by. */
    static String constantName(Id id) {
        return CONSTANT_NAMES.get(id);
    }

    private static boolean isConstant(Field field) {
        return Modifier.isStatic(field.getModifiers())
                && Condition.class.isAssignableFrom(field.getType());
    }

    private static Condition read(Field field) {
        try {
            return (Condition) field.get(null);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot read Condition." + field.getName(), e);
        }
    }
}

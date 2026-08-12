package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * What a bare {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.Id Id} points at.
 * <p>
 * Every other reference in the model carries its target in its type – a
 * {@code PersonCondition} holds a {@code Person}, an {@code Investigation} holds a
 * {@code Trigger} – so the editor can work out what to offer for it without being told. The
 * exception is the handful of places that hold an {@code Id} rather than the object, because
 * resolving it there would close a cycle in the build order. There the type says nothing, and
 * this annotation supplies what it cannot.
 * <p>
 * It exists so that a new reference of that shape costs one line rather than a case in the
 * editor. If you ever find yourself writing a second annotation to describe a field, that is the
 * sign the model wants the change, not the editor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD})
public @interface Ref {

    /** The kind of thing the annotated id (or list of ids) names. */
    Class<?> value();
}

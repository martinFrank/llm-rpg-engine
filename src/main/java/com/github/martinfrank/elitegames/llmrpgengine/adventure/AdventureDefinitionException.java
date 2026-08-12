package com.github.martinfrank.elitegames.llmrpgengine.adventure;

/**
 * The adventure itself is wrong – a reference points at nothing, an id is used twice, something
 * is asked for before it was defined.
 * <p>
 * Deliberately unchecked and deliberately loud: this is an authoring mistake, and the only useful
 * moment to learn about it is while writing the adventure. Returning {@code null} instead (as the
 * lookups used to) turns a typo into a crash somewhere else entirely, or worse, into a location
 * that silently never opens and a trigger that silently never fires.
 */
public class AdventureDefinitionException extends RuntimeException {

    public AdventureDefinitionException(String message) {
        super(message);
    }
}

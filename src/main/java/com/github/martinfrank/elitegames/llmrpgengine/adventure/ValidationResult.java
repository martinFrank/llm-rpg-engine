package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

/**
 * What {@link AdventureValidator} found. Errors and warnings are handed back rather than thrown
 * one at a time, so an author sees everything that is wrong in one go – and so an editor can show
 * the list instead of catching an exception per problem.
 *
 * @param errors   the adventure is broken and must not be played
 * @param warnings the adventure works, but something in it has no effect
 */
public record ValidationResult(List<String> errors, List<String> warnings) {

    public ValidationResult {
        errors = List.copyOf(errors);
        warnings = List.copyOf(warnings);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /** Fails with every error at once, because fixing them one build at a time is the slow way. */
    public void throwOnError() {
        if (hasErrors()) {
            throw new AdventureDefinitionException(
                    "the adventure has " + errors.size() + " error(s):\n - " + String.join("\n - ", errors));
        }
    }
}

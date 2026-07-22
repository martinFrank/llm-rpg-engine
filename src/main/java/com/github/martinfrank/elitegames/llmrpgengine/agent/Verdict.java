package com.github.martinfrank.elitegames.llmrpgengine.agent;

import java.util.Optional;
import java.util.UUID;

/**
 * The result of the {@link VerdictAgent}: its understanding of what the player's
 * input actually means, mapped onto one scripted {@link TaskType} that the engine
 * can execute against the session.
 *
 * @param interpretation short, plain description of what the player wants to do
 * @param task           the scripted task to run
 * @param target         the task's parameter as understood in words, e.g. the destination
 *                       name for {@link TaskType#GEHEZU}; empty when the task needs none
 * @param targetId       the id of the resolved location or person, picked from the available
 *                       lists in the context, or {@value #UNKNOWN} when nothing matched
 */
public record Verdict(
        String interpretation,
        TaskType task,
        String target,
        String targetId
) {

    /** Value the agent uses for {@link #targetId()} when the target could not be resolved. */
    public static final String UNKNOWN = "unbekannt";

    /**
     * The resolved target as a UUID, or empty if it is {@value #UNKNOWN}, blank, or not a
     * valid UUID. Handlers use this to look the target up in the session/adventure.
     */
    public Optional<UUID> targetUuid() {
        if (targetId == null || targetId.isBlank() || UNKNOWN.equalsIgnoreCase(targetId.strip())) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(targetId.strip()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

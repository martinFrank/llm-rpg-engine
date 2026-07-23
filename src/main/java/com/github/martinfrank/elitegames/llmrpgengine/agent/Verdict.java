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
 *                       name for {@link TaskType#GO_TO}; empty when the task needs none
 * @param targetId       the id of the resolved location or person, picked from the available
 *                       lists in the context, or {@value #UNKNOWN} when nothing matched
 * @param dialogTopic    for {@link TaskType#TALK}: the topic of the dialog the player's
 *                       input matches, as understood in words; empty for other tasks or
 *                       when no dialog matched (small talk / gossip)
 * @param dialogId       for {@link TaskType#TALK}: the id of the matched dialog, picked from
 *                       the dialog-topics list, or {@value #UNKNOWN} when no dialog matched
 *                       and the player only makes small talk (gossip)
 */
public record Verdict(
        String interpretation,
        TaskType task,
        String target,
        String targetId,
        String dialogTopic,
        String dialogId
) {

    /** Value the agent uses for {@link #targetId()}/{@link #dialogId()} when nothing could be resolved. */
    public static final String UNKNOWN = "unbekannt";

    /**
     * Convenience constructor for tasks that carry no dialog (everything but {@link TaskType#TALK}),
     * defaulting the dialog fields to "no dialog matched".
     */
    public Verdict(String interpretation, TaskType task, String target, String targetId) {
        this(interpretation, task, target, targetId, "", UNKNOWN);
    }

    /**
     * The resolved target as a UUID, or empty if it is {@value #UNKNOWN}, blank, or not a
     * valid UUID. Handlers use this to look the target up in the session/adventure.
     */
    public Optional<UUID> targetUuid() {
        return toUuid(targetId);
    }

    /**
     * The matched dialog as a UUID, or empty if it is {@value #UNKNOWN}, blank, or not a
     * valid UUID. Empty means the player only makes small talk (gossip) with no scripted dialog.
     */
    public Optional<UUID> dialogUuid() {
        return toUuid(dialogId);
    }

    private static Optional<UUID> toUuid(String value) {
        if (value == null || value.isBlank() || UNKNOWN.equalsIgnoreCase(value.strip())) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.strip()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

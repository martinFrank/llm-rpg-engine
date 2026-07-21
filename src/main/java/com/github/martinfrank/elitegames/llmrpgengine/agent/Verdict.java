package com.github.martinfrank.elitegames.llmrpgengine.agent;

/**
 * The result of the {@link VerdictAgent}: its understanding of what the player's
 * input actually means, mapped onto one scripted {@link TaskType} that the engine
 * can execute against the session.
 *
 * @param interpretation short, plain description of what the player wants to do
 * @param task           the scripted task to run
 * @param target         the task's parameter, e.g. the destination name for {@link TaskType#GEHEZU};
 *                       empty when the task needs none
 */
public record Verdict(
        String interpretation,
        TaskType task,
        String target
) {
}

package com.github.martinfrank.elitegames.llmrpgengine.agent;

/**
 * The result of the {@link VerdictAgent}: its understanding of what the player's
 * input actually means, mapped onto one scripted {@link TaskType} that the engine
 * can execute against the session.
 * <p>
 * The ids stay raw strings on purpose: they come from an agent and may be mangled, so parsing
 * them to a {@link java.util.UUID} here would drop exactly the ids the guardrail can still
 * recover. Handlers resolve them against the candidates that are legal at that point – see
 * {@code Session.resolvePerson/resolveLocation} and
 * {@link com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein#findClosest(String, java.util.List)}.
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
     * Whether the agent reported a target id at all, i.e. it is neither blank nor
     * {@value #UNKNOWN}. A reported id may still be mangled, so handlers resolve it through the
     * guardrail ({@code session.resolvePerson/resolveLocation}) rather than parsing it directly.
     */
    public boolean hasTargetId() {
        return isReported(targetId);
    }

    /**
     * Whether the agent reported a dialog id at all, i.e. it is neither blank nor
     * {@value #UNKNOWN}. {@code false} means the player only makes small talk (gossip).
     */
    public boolean hasDialogId() {
        return isReported(dialogId);
    }

    private static boolean isReported(String value) {
        return value != null && !value.isBlank() && !UNKNOWN.equalsIgnoreCase(value.strip());
    }
}

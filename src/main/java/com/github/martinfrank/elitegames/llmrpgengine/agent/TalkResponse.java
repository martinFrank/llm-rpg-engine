package com.github.martinfrank.elitegames.llmrpgengine.agent;

import java.util.List;

/**
 * The result of the {@link TalkAgent}: the person's in-character reply plus the
 * knowledge triggers the conversation actually touched.
 * <p>
 * Detecting the triggers is the agent's job; <em>applying</em> them (setting flags,
 * granting knowledge) is done elsewhere.
 *
 * @param reply             the person's spoken reply, in character
 * @param triggeredTriggers the triggerCondition topics offered in the context that this exchange
 *                          addressed, each with the id needed to resolve it; empty when none
 */
public record TalkResponse(String reply, List<TriggeredTrigger> triggeredTriggers) {

    /**
     * A knowledge triggerCondition the {@link TalkAgent} judged to have been addressed in the
     * conversation.
     *
     * @param trigger   the triggerCondition topic as understood (human-readable)
     * @param triggerId the id of the triggerCondition, taken verbatim from the triggerCondition list in the context
     */
    public record TriggeredTrigger(String trigger, String triggerId) {
    }
}

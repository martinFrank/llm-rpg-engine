package com.github.martinfrank.elitegames.llmrpgengine.adventure.trigger;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;

import java.util.List;
import java.util.UUID;

public record KnowledgeTrigger<R>(UUID id, String triggerCondition, List<Condition<R>> conditions, TriggeredEvent triggeredEvent) implements Identifiable, Trigger<R>{

    @Override
    public boolean isTriggered(List<Condition<R>> flags) {
        return false;
    }

}

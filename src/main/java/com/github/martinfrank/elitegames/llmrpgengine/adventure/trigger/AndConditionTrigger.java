package com.github.martinfrank.elitegames.llmrpgengine.adventure.trigger;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;

import java.util.List;
import java.util.UUID;

public record AndConditionTrigger<R>(UUID id, String triggerCondition, List<Condition<R>> conditions, TriggeredEvent<R> triggeredEvent) implements Identifiable, Trigger<R>{

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isTriggered(List<Condition<R>> conditions, Session session) {
        for (Condition<R> condition: conditions){
            List flags = condition.consideredFlags();
            List sessionFlags = session.sessionFlags.getFlags(flags);
            if( ! condition.evaluate(sessionFlags) ){
                return false;
            }
        }
        return true;
    }

}

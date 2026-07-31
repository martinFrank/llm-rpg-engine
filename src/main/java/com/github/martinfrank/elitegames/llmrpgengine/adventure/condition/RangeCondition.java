package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record RangeCondition(UUID id, String description, List<Flag> consideredFlags, List values) implements Condition {

    @Override
    public boolean evaluate(List flags) {
        if (values == null || flags == null || flags.size() != 1) {
            return false;
        }
        for(Object r : values) {
            if (((Flag<Object, ?>)flags.getFirst()).value().equals(r)){
                return true;
            }
        }
        return false;
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record RangeCondition<R>(UUID id, String description, List<Flag<R>> consideredFlags, List<R> values) implements Condition<R> {

    @Override
    public boolean evaluate(List<Flag<R>> flags) {
        if (values == null || flags == null || flags.size() != 1) {
            return false;
        }
        for(R r : values) {
            if (flags.getFirst().value().equals(r)){
                return true;
            }
        }
        return false;
    }
}

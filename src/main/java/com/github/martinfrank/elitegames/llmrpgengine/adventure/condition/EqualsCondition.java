package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record EqualsCondition<R>(UUID id, String description, List consideredFlags, R value) implements Condition {

    @Override
    public boolean evaluate(List flags) {
        if (value == null || flags == null || flags.size() != 1) {
            return false;
        }
        return value.equals(((Flag<Boolean, ?>)flags.getFirst()).value());
    }
}

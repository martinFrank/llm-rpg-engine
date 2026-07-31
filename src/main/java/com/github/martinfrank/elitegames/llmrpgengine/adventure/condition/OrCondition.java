package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record OrCondition(UUID id, String description, List<Flag> consideredFlags) implements Condition {

    @Override
    public boolean evaluate(List<Flag> flags) {
        if (flags == null || flags.size() != 2) {
            return false;
        }
        return ((Flag<Boolean,?>)flags.getFirst()).value() && ((Flag<Boolean,?>)flags.getLast()).value();
    }

}

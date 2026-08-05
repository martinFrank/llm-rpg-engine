package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record OrCondition(UUID id, String description, List<Flag<?>> consideredFlags) implements Condition {

    @Override
    public boolean evaluate(List<Flag<?>> flags) {
        if (flags == null || flags.isEmpty()) {
            return false;
        }
        for (Flag<?> flag : flags) {
            if (flag.isRaised()) {
                return true;
            }
        }
        return false;
    }

}

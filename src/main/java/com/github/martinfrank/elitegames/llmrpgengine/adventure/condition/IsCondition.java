package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record IsCondition(UUID id, String description, List<Flag<Boolean>> consideredFlags) implements Condition<Boolean> {

    @Override
    public boolean evaluate(List<Flag<Boolean>> flags) {
        if (flags == null || flags.size() != 1) {
            return false;
        }
        return flags.getFirst().value();
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record TrueCondition(UUID id) implements Condition<Boolean> {
    @Override
    public boolean evaluate(List<Flag<Boolean>> flags) {
        return true;
    }

    @Override
    public List<Flag<Boolean>> consideredFlags() {
        return List.of();
    }
}

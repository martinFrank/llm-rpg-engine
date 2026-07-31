package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record TrueCondition(UUID id) implements Condition {

    @Override
    public boolean evaluate(List<Flag> flags) {
        return true;
    }

    @Override
    public List<Flag> consideredFlags() {
        return List.of();
    }
}

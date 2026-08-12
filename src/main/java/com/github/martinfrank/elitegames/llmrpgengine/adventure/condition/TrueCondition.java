package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

public record TrueCondition(Id id) implements Condition {

    @Override
    public boolean evaluate(List<Flag<?>> flags) {
        return true;
    }

    @Override
    public List<Flag<?>> consideredFlags() {
        return List.of();
    }
}

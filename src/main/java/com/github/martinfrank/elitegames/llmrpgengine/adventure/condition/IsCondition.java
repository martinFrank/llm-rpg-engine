package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

public record IsCondition(Id id, String description, List<Flag<?>> consideredFlags) implements Condition {

    @Override
    public boolean evaluate(List<Flag<?>> flags) {
        if (flags == null || flags.size() != 1) {
            return false;
        }
        return flags.getFirst().isRaised();
    }
}

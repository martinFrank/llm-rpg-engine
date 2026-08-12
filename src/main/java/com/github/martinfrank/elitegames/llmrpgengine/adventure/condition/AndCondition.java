package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

public record AndCondition(Id id, String description, List<Flag<?>> consideredFlags) implements Condition {

    @Override
    public boolean evaluate(List<Flag<?>> list) {

        if (list == null || list.isEmpty()) {
            return false;
        }

        for (Flag<?> flag : list) {
            if (!flag.isRaised()) {
                return false;
            }
        }
        return true;
    }
}

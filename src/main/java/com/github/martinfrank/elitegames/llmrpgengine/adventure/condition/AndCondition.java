package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;

import java.util.List;
import java.util.UUID;

public record AndCondition(UUID id, String description, List<Flag<?>> consideredFlags) implements Condition {

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

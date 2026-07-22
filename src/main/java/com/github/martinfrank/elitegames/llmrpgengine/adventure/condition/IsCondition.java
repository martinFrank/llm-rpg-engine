package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public class IsCondition extends BaseCondition<Boolean> {

    public IsCondition(UUID uuid, String description, List<Flag<Boolean>> consideredFlags) {
        super(uuid, description, consideredFlags);
    }

    @Override
    public boolean evaluate(List<Flag<Boolean>> flags) {
        if (flags == null || flags.size() != 1) {
            return false;
        }
        return flags.getFirst().getValue();
    }
}

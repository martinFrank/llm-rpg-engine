package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public class AndCondition extends BaseCondition<Boolean> {

    public AndCondition(UUID id, String description, List<Flag<Boolean>> consideredFlags) {
        super(id, description, consideredFlags);
    }

    @Override
    public boolean evaluate(List<Flag<Boolean>> flags) {
        if (flags == null || flags.size() != 2) {
            return false;
        }
        return flags.getFirst().getValue() && flags.getLast().getValue();
    }

}

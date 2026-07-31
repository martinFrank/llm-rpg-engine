package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public record AndCondition (UUID id, String description, List<Flag> consideredFlags) implements Condition {
    @Override
    public boolean evaluate(List list) {

            if (list == null || list.size() != 2) {
            return false;
        }
        return ((Flag<Boolean, ?>) list.getFirst()).value() && ((Flag<Boolean, ?>) list.getLast()).value();

    }

//    @Override
//    public boolean evaluate(List<Flag> flags) {
//        if (flags == null || flags.size() != 2) {
//            return false;
//        }
//        return flags.getFirst().value() && flags.getLast().value();
//    }

}

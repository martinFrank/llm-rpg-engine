package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;

import java.util.List;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

public record GameTimeCondition(Id id, List<Flag<?>> consideredFlags, List<GameTime> times) implements Condition {

    @Override
    public boolean evaluate(List<Flag<?>> flags) {
        if (flags == null || flags.size() != 1) {
            return false;
        }

        GameTime time = (GameTime) flags.getFirst().content();
        return  times.contains(time);
    }
}

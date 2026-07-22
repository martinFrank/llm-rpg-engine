package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public class RangeCondition<R> extends BaseCondition<R> {

    private final List<R> values;

    public RangeCondition(UUID uuid, String description, List<Flag<R>> consideredFlags, List<R> values) {
        super(uuid, description, consideredFlags);
        this.values = values;
    }

    @Override
    public boolean evaluate(List<Flag<R>> flags) {
        if (values == null || flags == null || flags.size() != 1) {
            return false;
        }
        for(R r : values) {
            if (flags.getFirst().getValue().equals(r)){
                return true;
            }
        }
        return false;
    }
}

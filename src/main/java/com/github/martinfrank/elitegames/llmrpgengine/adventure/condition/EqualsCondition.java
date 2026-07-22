package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public class EqualsCondition<R> extends BaseCondition<R> {

    private final R value;

    public EqualsCondition(UUID uuid, String description, List<Flag<R>> flagsToConsider, R value) {
        super(uuid, description, flagsToConsider);
        this.value = value;
    }

    @Override
    public boolean evaluate(List<Flag<R>> flags) {
        if (value == null || flags == null || flags.size() != 1) {
            return false;
        }
        return value.equals(flags.getFirst().getValue());
    }
}

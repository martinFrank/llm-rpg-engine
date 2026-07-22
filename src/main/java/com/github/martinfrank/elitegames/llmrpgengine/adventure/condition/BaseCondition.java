package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

abstract class BaseCondition <R> implements Condition<R> {

    private final UUID id;
    private final String description;
    private final List<Flag<R>> consideredFlags;

    public BaseCondition(UUID id, String description, List<Flag<R>> consideredFlags) {
        this.id = id;
        this.description = description;
        this.consideredFlags = consideredFlags;
    }

    @Override
    public List<Flag<R>> getConsideredFlags() {
        return consideredFlags;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

}

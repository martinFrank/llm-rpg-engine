package com.github.martinfrank.elitegames.llmrpgengine.adventure.condition;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.List;
import java.util.UUID;

public abstract class BaseCondition <R> implements Condition<R> {

    public static final Condition<Boolean> ALWAYS_TRUE_CONDITION = new BaseCondition<>(UUID.fromString("7b8e5213-c009-49f2-8488-6e051f88643f"), "always true", List.of()) {
        @Override
        public boolean evaluate(List<Flag<Boolean>> list) {
            return true;
        }
    };

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

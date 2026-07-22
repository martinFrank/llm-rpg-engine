package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import java.util.UUID;

public class BooleanFlag extends BaseFlag<Boolean> {

    public BooleanFlag(UUID id, String name, Boolean value) {
        super(id, name, value);
    }

}

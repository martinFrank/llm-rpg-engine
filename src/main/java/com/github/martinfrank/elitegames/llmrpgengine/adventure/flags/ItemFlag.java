package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Item;

import java.util.UUID;

public record ItemFlag (UUID id, String name, Item content) implements Flag<Item> {

    @Override
    public boolean isRaised() {
        return false;
    }
}

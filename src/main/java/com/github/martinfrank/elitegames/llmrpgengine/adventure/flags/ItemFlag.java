package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Item;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

public record ItemFlag (Id id, String name, Item content) implements Flag<Item> {

    @Override
    public boolean isRaised() {
        return false;
    }
}

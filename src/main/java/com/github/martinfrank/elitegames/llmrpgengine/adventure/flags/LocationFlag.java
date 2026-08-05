package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;

import java.util.UUID;

//zeigt an, ob die location betreten wurde
public record LocationFlag(UUID id, String name, Location content) implements Flag<Location> {

    @Override
    public boolean isRaised() {
        return false;
    }
}

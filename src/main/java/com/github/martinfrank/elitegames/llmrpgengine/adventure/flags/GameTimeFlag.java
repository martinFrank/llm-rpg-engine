package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;

import java.util.UUID;

public record GameTimeFlag (UUID id, String name, GameTime value) implements Flag<GameTime> {

//    public GameTimeFlag(UUID id, String name, GameTime value) {
//        super(id, name, value);
//    }

}

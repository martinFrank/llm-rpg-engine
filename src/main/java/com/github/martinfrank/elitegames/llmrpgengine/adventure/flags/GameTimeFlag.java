package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;

import java.util.UUID;

public class GameTimeFlag extends BaseFlag<GameTime>{

    public GameTimeFlag(UUID id, String name, GameTime value) {
        super(id, name, value);
    }

}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;

import java.util.UUID;

public class BaseFlag<R> implements Flag<R> {

    public static final Flag<GameTime> GAME_TIME_FLAG = new BaseFlag<GameTime>(
            UUID.fromString("ab9ee3e8-04df-493b-a408-dc93e738eaa3"), "game time flag", GameTime.AFTERNOON);

    private final UUID id;
    private final String description;
    private final R value;

    public BaseFlag(UUID id, String description, R value) {
        this.id = id;
        this.description = description;
        this.value = value;
    }

    @Override
    public R getValue() {
        return value;
    }

    public String getDescription(){
        return description;
    }

    @Override
    public UUID getId() {
        return id;
    }


}

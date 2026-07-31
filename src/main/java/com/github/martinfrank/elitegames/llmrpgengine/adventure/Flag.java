package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.GameTimeFlag;

import java.util.UUID;

public interface Flag<R, V> extends Identifiable{

    Flag<GameTime, String> GAME_TIME_FLAG = new GameTimeFlag(UUID.fromString("ab9ee3e8-04df-493b-a408-dc93e738eaa3"), "game time flag", "the current time", GameTime.AFTERNOON);

    R value();
    V data();

    /*
    Also nicht:
    - Flags
    - Knowledge
    - Visited
    - Quest

    sondern
    - Facts

    mit Typen.

    Beispielsweise
    Fact
    - type = KNOWLEDGE
    - id = dragon_location

    Fact
    - type = VISITED
    - id = castle

    Fact
    - type = QUEST
    - id = dragon
    - state = started
     */
}

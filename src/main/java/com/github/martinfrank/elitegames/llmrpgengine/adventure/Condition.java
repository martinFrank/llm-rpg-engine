package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.GameTimeCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.TrueCondition;

import java.util.List;
import java.util.UUID;

public interface Condition extends Identifiable {

    Condition ALWAYS_TRUE = new TrueCondition(UUID.fromString("7b8e5213-c009-49f2-8488-6e051f88643f"));
    Condition DAY_TIME = new GameTimeCondition(UUID.fromString("5d429e18-4b66-4226-af8e-2940d1f4037a"),
            List.of(), List.of(GameTime.MORNING, GameTime.HIGH_NOON, GameTime.AFTERNOON));
    Condition NIGHT_TIME = new GameTimeCondition(UUID.fromString("c10e1120-b895-496d-b095-18503b5a9a35"),
            List.of(),List.of(GameTime.IN_THE_EVENING, GameTime.AT_NIGHT, GameTime.MIDNIGHT));

    boolean evaluate(List<Flag<?>> flags);

    List<Flag<?>> consideredFlags();
}

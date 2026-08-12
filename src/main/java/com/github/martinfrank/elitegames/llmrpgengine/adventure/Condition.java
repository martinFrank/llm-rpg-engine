package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.GameTimeCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.TrueCondition;

import java.util.List;

public interface Condition extends Identifiable {

    Condition ALWAYS_TRUE = new TrueCondition(Id.of("condition.immer-wahr"));
    Condition DAY_TIME = new GameTimeCondition(Id.of("condition.tagsueber"),
            List.of(), List.of(GameTime.MORNING, GameTime.HIGH_NOON, GameTime.AFTERNOON));
    Condition NIGHT_TIME = new GameTimeCondition(Id.of("condition.nachts"),
            List.of(),List.of(GameTime.IN_THE_EVENING, GameTime.AT_NIGHT, GameTime.MIDNIGHT));

    boolean evaluate(List<Flag<?>> flags);

    List<Flag<?>> consideredFlags();
}

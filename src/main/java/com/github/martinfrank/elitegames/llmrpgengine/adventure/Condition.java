package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.condition.TrueCondition;

import java.util.List;
import java.util.UUID;

public interface Condition<R> extends Identifiable {

    Condition<Boolean> ALWAYS_TRUE_CONDITION = new TrueCondition(UUID.fromString("7b8e5213-c009-49f2-8488-6e051f88643f"));

    boolean evaluate(List<Flag<R>> flags);

    List<Flag<R>> consideredFlags();
}

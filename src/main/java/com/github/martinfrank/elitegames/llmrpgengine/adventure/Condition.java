package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

public interface Condition<R> extends Identifiable {

    boolean evaluate(List<Flag<R>> flags);

    List<Flag<R>> getConsideredFlags();
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

public interface Trigger<R> {

    boolean isTriggered(List<Condition<R>> flags);

    List<Condition<R>> conditions();

    TriggeredEvent triggeredEvent();

}

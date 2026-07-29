package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.session.Session;

import java.util.List;

public interface Trigger<R> extends Identifiable{

    boolean isTriggered(List<Condition<R>> flags, Session session);

    List<Condition<R>> conditions();

    String triggerCondition();

    TriggeredEvent<R> triggeredEvent();

}

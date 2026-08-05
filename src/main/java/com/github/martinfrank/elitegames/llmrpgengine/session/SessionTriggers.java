package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Trigger;

import java.util.ArrayList;
import java.util.List;

public class SessionTriggers {

    private final List<Trigger> executedTriggers = new ArrayList<>();

    //nur triggers die noch nicht ausgelöst wurden, damit sie nicht aus versehen 2x ausgelöst werden
    public List<Trigger> untriggered(List<Trigger> list) {
        List<Trigger> unexecutedTriggers = new ArrayList<>();
        for (Trigger trigger : list) {
            if (!executedTriggers.contains(trigger)) {
                executedTriggers.add(trigger);
                unexecutedTriggers.add(trigger);
            }
        }
        return unexecutedTriggers;
    }
}

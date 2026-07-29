package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.List;

public interface TriggeredEvent<R> extends Identifiable {

    String name();

    String description();

    //welche flags werden durch das Event geändert
    List<FlagChange<R>> flagChanges();

    //wo ist man nach dem Event
    Location newLocation();

    //Liste der Gegenstandswechsel (nehmen oder geben)
//    List<ItemChange> itemChanges();

}

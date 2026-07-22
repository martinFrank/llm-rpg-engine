package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public interface Adventure {

    String getPlotSummary();
    Intro getIntro();
    List<Chapter> getChapters();
    List<Person> getPersons();
    List<Item> getItems();
    List<Dialog> getDialogs();
    List<Location> getLocations();
    List<Condition<?>> getConditions();
    List<Flag<?>> getFlags();
    List<Knowledge> getKnowledges();

    Condition<?> getCondition(UUID id);
    Location getLocation(UUID id);
    Person getPerson(UUID id);
    Flag<?> getFlag(UUID id);
    Dialog getDialog(UUID id);
    Knowledge getKnowledge(UUID id);
}

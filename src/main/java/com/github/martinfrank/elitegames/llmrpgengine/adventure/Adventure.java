package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

public interface Adventure {

    String getPlotSummary();
    Metadata getMetadata();
    List<Chapter> getChapters();
    List<Person> getPersons();
    List<Item> getItems();
    List<Dialog> getDialogs();
    List<Location> getLocations();
    List<Condition> getConditions();
    List<Flag<?>> getFlags();
    List<Trigger> getTriggers();
    List<Investigation> getInvestigations();

    Condition getCondition(Id id);
    Location getLocation(Id id);
    Person getPerson(Id id);
    Item getItem(Id id);
    Flag<?> getFlag(Id id);
    Dialog getDialog(Id id);
    Trigger getTrigger(Id id);
    Investigation getInvestigation(Id id);

    /** Convenience for the authoring side, so an adventure can reference by literal id. */
    default Condition getCondition(String id) { return getCondition(Id.of(id)); }
    default Location getLocation(String id) { return getLocation(Id.of(id)); }
    default Person getPerson(String id) { return getPerson(Id.of(id)); }
    default Item getItem(String id) { return getItem(Id.of(id)); }
    default Flag<?> getFlag(String id) { return getFlag(Id.of(id)); }
    default Dialog getDialog(String id) { return getDialog(Id.of(id)); }
    default Trigger getTrigger(String id) { return getTrigger(Id.of(id)); }
    default Investigation getInvestigation(String id) { return getInvestigation(Id.of(id)); }
}

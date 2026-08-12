package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

/**
 * The smallest adventure that builds: everything empty. A test subclasses it and fills in only
 * the definitions its rule is about, so what the test is checking stays the only thing on screen.
 */
class TinyAdventure extends BaseAdventure {

    static Location location(String id, String name) {
        return new Location.Builder().id(id).name(name).description(name).build();
    }

    static Person person(String id, String name) {
        return new Person.Builder().id(id).name(name).description(name).build();
    }

    @Override public String getPlotSummary() { return "nichts passiert"; }
    @Override public Metadata getMetadata() { return new Metadata("Winzig", "test"); }
    @Override protected List<Location> defineLocations() { return List.of(); }
    @Override protected List<Person> definePersons() { return List.of(); }
    @Override protected List<Item> defineItems() { return List.of(); }
    @Override protected List<Flag<?>> defineFlags() { return List.of(); }
    @Override protected List<Trigger> defineTriggers() { return List.of(); }
    @Override protected List<Dialog> defineDialogs() { return List.of(); }
    @Override protected List<Investigation> defineInvestigations() { return List.of(); }
    @Override protected List<Condition> defineConditions() { return List.of(); }
    @Override protected List<Chapter> defineChapters() { return List.of(); }
}

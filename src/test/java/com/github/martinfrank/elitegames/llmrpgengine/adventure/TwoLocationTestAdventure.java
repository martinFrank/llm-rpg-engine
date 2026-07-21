package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

/**
 * Minimal adventure fixture with two locations, used to test scripted movement.
 */
public class TwoLocationTestAdventure implements Adventure {

    public static final Location DORFPLATZ = new Location(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "Dorfplatz", "Der Dorfplatz.");

    public static final Location RATHAUS = new Location(
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            "Rathaus", "Das Rathaus.");

    @Override
    public String getPlotSummary() {
        return "Testplot.";
    }

    @Override
    public Intro getIntro() {
        return new Intro("Testtitel", "Testautor", "Testintro.", DORFPLATZ, GameTime.AFTERNOON);
    }

    @Override
    public List<Chapter> getChapters() {
        return List.of(new Chapter.Builder()
                .name("Testkapitel")
                .summary("Ein Testkapitel.")
                .locations(getLocations())
                .timeTables(List.of())
                .build());
    }

    @Override
    public List<Person> getPersons() {
        return List.of();
    }

    @Override
    public List<Item> getItems() {
        return List.of();
    }

    @Override
    public List<Location> getLocations() {
        return List.of(DORFPLATZ, RATHAUS);
    }

    @Override
    public Location getLocation(UUID uuid) {
        return (Location) Identifiable.find(uuid, getLocations());
    }

    @Override
    public Person getPerson(UUID uuid) {
        return (Person) Identifiable.find(uuid, getPersons());
    }
}

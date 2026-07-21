package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Chapter implements Identifiable {

    private final UUID id;
    private final String name;
    private final String summary;
    private final List<Location> locations;
    private final List<PersonTimetable> timeTables;


    public Chapter(UUID id, String name, String summary, List<Location> locations,  List<PersonTimetable> timeTables) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.locations = locations;
        this.timeTables = timeTables;
    }

    public List<Person> getPersons(Location location, GameTime currentTime) {
        return timeTables.stream()
                .filter(tt -> tt.where().getId().equals(location.getId()) && tt.when().contains(currentTime))
                .map(PersonTimetable::who).distinct().collect(Collectors.toList());
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String name() {
        return name;
    }

    public String summary() {
        return summary;
    }

    public static class Builder {

        private UUID id = UUID.randomUUID();
        private String name;
        private String summary;
        private List<Location> locations;
        private List<PersonTimetable> timeTables;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Builder locations(List<Location> locations) {
            this.locations = locations;
            return this;
        }

        public Builder timeTables(List<PersonTimetable> timeTables) {
            this.timeTables = timeTables;
            return this;
        }

        public Chapter build() {
            return new Chapter(id, name, summary, locations, timeTables);
        }

    }
}

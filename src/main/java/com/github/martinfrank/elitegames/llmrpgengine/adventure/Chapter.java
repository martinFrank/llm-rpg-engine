package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Chapter implements Identifiable {

    private final UUID id;
    private final String name;
    private final String summary;
    private final List<LocationCondition> locationConditions;
    private final List<PersonCondition> personConditions;
    private final List<Item> items = new ArrayList<>();


    public Chapter(UUID id, String name, String summary, List<LocationCondition> locationConditions, List<PersonCondition> personConditions) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.locationConditions = locationConditions;
        this.personConditions = personConditions;
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

    public List<PersonCondition> personConditions() {
        return personConditions;
    }
    public List<LocationCondition> locationConditions() {
        return locationConditions;
    }

    public static class Builder {

        private UUID id = UUID.randomUUID();
        private String name;
        private String summary;
        private List<LocationCondition> locations;
        private List<PersonCondition> personConditions;

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

        public Builder locationConditions(List<LocationCondition> locations) {
            this.locations = locations;
            return this;
        }

        public Builder personConditions(List<PersonCondition> personConditions) {
            this.personConditions = personConditions;
            return this;
        }

        public Chapter build() {
            return new Chapter(id, name, summary, locations, personConditions);
        }

    }
}

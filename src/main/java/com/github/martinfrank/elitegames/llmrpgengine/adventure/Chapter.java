package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Chapter(
        UUID id,
        String name,
        String summary,
        List<LocationCondition> locationConditions,
        List<PersonCondition> personConditions,
        List <DialogCondition> dialogConditions) implements Identifiable {

    public static class Builder {

        private UUID id = UUID.randomUUID();
        private String name;
        private String summary;
        private List<LocationCondition> locations = new ArrayList<>();
        private List<PersonCondition> personConditions = new ArrayList<>();
        private List<DialogCondition> dialogConditions = new ArrayList<>();

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

        public Builder dialogConditions(List<DialogCondition> dialogConditions) {
            this.dialogConditions = dialogConditions;
            return this;
        }

        public Chapter build() {
            return new Chapter(id, name, summary, locations, personConditions, dialogConditions);
        }

    }
}

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
        Intro intro,
        List<LocationCondition> locationConditions,
        List<PersonCondition> personConditions,
        List<DialogCondition> dialogConditions,
        Condition chapterFinishedCondition) implements Identifiable {

    public static class Builder {

        private UUID id = UUID.randomUUID();
        private String name;
        private String summary;
        private Intro intro;
        private List<LocationCondition> locations = new ArrayList<>();
        private List<PersonCondition> personConditions = new ArrayList<>();
        private List<DialogCondition> dialogConditions = new ArrayList<>();
        private Condition chapterFinishedCondition = null; //FIXME

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

        public Builder intro(Intro intro) {
            this.intro = intro;
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

        public Builder chapterFinishedCondition(Condition chapterFinishedCondition) {
            this.chapterFinishedCondition = chapterFinishedCondition;
            return this;
        }

        public Chapter build() {
            if (chapterFinishedCondition == null) {
                throw new IllegalStateException("chapter finished condition cannot be null");
            }
            if (intro == null) {
                throw new IllegalStateException("intro cannot be null");
            }
            return new Chapter(
                    id,
                    name,
                    summary,
                    intro,
                    locations,
                    personConditions,
                    dialogConditions,
                    chapterFinishedCondition);
        }
    }
}

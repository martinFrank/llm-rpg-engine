package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.Objects;
import java.util.UUID;

public record Person (UUID id, String name, String description, String role, String appearance, String background, String personality) implements Identifiable {

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String name;
        private String description;
        private String role;
        private String appearance;
        private String background;
        private String personality;

        public Person build() {
            return new Person(id, name, description, role, appearance, background, personality);
        }
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder appearance(String appearance) {
            this.appearance = appearance;
            return this;
        }

        public Builder background(String background) {
            this.background = background;
            return this;
        }

        public Builder personality(String personality) {
            this.personality = personality;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

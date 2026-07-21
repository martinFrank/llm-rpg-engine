package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.Objects;
import java.util.UUID;

public record Location (UUID id, String name, String description) implements Identifiable{

//    private final UUID id;
//    private final String name;
//    private final String description;
//
//    public Location(UUID id, String name, String description) {
//        this.id = id;
//        this.name = name;
//        this.description = description;
//    }

    @Override
    public UUID getId() {
        return id;
    }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String name;
        private String description;
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
        public Location build() {
            return new Location(id, name, description);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

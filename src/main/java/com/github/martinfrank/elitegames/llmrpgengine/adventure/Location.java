package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Location (UUID id, String name, String description, List<UUID> destinationIds) implements Identifiable{


    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String name;
        private String description;
        private List<UUID> destinationIds = new ArrayList<>();

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
        public Builder destinations(List<UUID> destinationIds) {
            this.destinationIds = destinationIds;
            return this;
        }
        public Location build() {
            return new Location(id, name, description, destinationIds);
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

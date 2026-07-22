package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record Location (UUID id, String name, String description, List<FlagChange<Boolean>> visitedPlacesFlags) implements Identifiable{

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String name;
        private String description;
        private List<FlagChange<Boolean>> visitedPlacesFlags = new ArrayList<>();

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
        public Builder visitedPlacesFlags(List<FlagChange<Boolean>> visitedPlacesFlags) {
            this.visitedPlacesFlags = visitedPlacesFlags;
            return this;
        }
        public Location build() {
            return new Location(id, name, description, visitedPlacesFlags);
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

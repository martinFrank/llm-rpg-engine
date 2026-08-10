package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record Location (Id id, String name, String description, List<Id> destinationIds, List<Id> triggerIds) implements Identifiable{


    public static class Builder {
        private Id id = null;
        private String name;
        private String description;
        private List<Id> destinationIds = new ArrayList<>();
        private List<Id> triggerIds = new ArrayList<>();

        public Builder id(Id id) {
            this.id = id;
            return this;
        }
        public Builder id(String id) {
            return id(Id.of(id));
        }
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        public Builder destinations(List<Id> destinationIds) {
            this.destinationIds = destinationIds;
            return this;
        }
        public Builder destinations(String... destinationIds) {
            return destinations(Arrays.stream(destinationIds).map(Id::of).toList());
        }
        public Builder triggers(List<Id> triggerIds) {
            this.triggerIds = triggerIds;
            return this;
        }
        public Builder triggers(String... triggerIds) {
            return triggers(Arrays.stream(triggerIds).map(Id::of).toList());
        }
        public Location build() {
            if (id == null) {
                throw new IllegalStateException("location id cannot be null (name: " + name + ")");
            }
            return new Location(id, name, description, destinationIds, triggerIds);
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

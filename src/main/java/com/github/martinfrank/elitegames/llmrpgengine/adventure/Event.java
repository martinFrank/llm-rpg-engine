package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.ArrayList;
import java.util.List;

public record Event (
        List<Flag<?>> raisedFlags,
        String text, Location location,
        GameTime gameTime,
        List<Item> addedItems,
        List<Item> removedItems) {


    public static class Builder {
        private List<Flag<?>> raisedFlags = new ArrayList<>();
        private String text;
        private Location location;
        private GameTime gameTime;
        private List<Item> addedItems;
        private List<Item> removedItems;

        public Builder raisedFlags(List<Flag<?>> raisedFlags) {
            this.raisedFlags = raisedFlags;
            return this;
        }
        public Builder raisedFlag(Flag<?> flag) {
            this.raisedFlags = List.of(flag);
            return this;
        }
        public Builder text(String text) {
            this.text = text;
            return this;
        }
        public Builder location(Location location) {
            this.location = location;
            return this;
        }
        public Builder gameTime(GameTime gameTime) {
            this.gameTime = gameTime;
            return this;
        }
        public Builder addedItems(List<Item> addedItems) {
            this.addedItems = addedItems;
            return this;
        }
        public Builder removedItems(List<Item> removedItems) {
            this.removedItems = removedItems;
            return this;
        }
        public Event build() {
            return new Event(raisedFlags, text, location, gameTime, addedItems, removedItems);
        }
    }

}

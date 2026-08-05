package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.ArrayList;
import java.util.List;

public record Event (
        List<FlagChange<?,?>> flagChanges,
        String text, Location location,
        GameTime gameTime,
        List<Item> addedItems,
        List<Item> removedItems) {


    public static class Builder {
        private List<FlagChange<?,?>> flagChanges = new ArrayList<>();
        private String text;
        private Location location;
        private GameTime gameTime;
        private List<Item> addedItems;
        private List<Item> removedItems;

        public Builder flagChanges(List<FlagChange<?,?>> flagChanges) {
            this.flagChanges = flagChanges;
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
            return new Event(flagChanges, text, location, gameTime, addedItems, removedItems);
        }
    }

}

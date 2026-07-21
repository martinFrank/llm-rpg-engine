package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;

import java.util.List;
import java.util.Optional;

public class Session {

    private final Adventure adventure;
    private final Player player;
    public final ChatHistory chatHistory = new ChatHistory();

    private Location currentLocation;
    private Chapter currentChapter;
    private GameTime currentTime = GameTime.AFTERNOON;

    public Session(Adventure adventure, Player player) {
        this.adventure = adventure;
        this.player = player;
    }

    public void start() {
        chatHistory.narrator(adventure.getIntro().title());
        chatHistory.narrator(adventure.getIntro().author());
        chatHistory.narrator(adventure.getIntro().intro());
        currentLocation = adventure.getIntro().startLocation();
        currentTime = adventure.getIntro().startTime();
        currentChapter = adventure.getChapters().getFirst();
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public List<Person> getPersons(Location location, GameTime gameTime) {
        return currentChapter.getPersons(location, gameTime);
    }

    public GameTime getCurrentTime() {
        return currentTime;
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    /**
     * Looks up a location of the adventure by its name (case-insensitive).
     * Used by scripted tasks such as GEHEZU to resolve a destination.
     */
    public Optional<Location> findLocation(String name) {
        if (name == null) {
            return Optional.empty();
        }
        String needle = name.strip();
        return adventure.getLocations().stream()
                .filter(location -> location.name().equalsIgnoreCase(needle))
                .findFirst();
    }
}

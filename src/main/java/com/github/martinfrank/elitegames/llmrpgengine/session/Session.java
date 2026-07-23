package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Session {

    private final Adventure adventure;
    private final Player player;
    public final ChatHistory chatHistory = new ChatHistory();

    private Location currentLocation;
    private Chapter currentChapter;
    private GameTime currentTime = GameTime.AFTERNOON;
    private final SessionFlags sessionFlags = new SessionFlags();

    public Session(Adventure adventure, Player player) {
        this.adventure = adventure;
        this.player = player;
        sessionFlags.init(adventure.getFlags());
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


    public GameTime getCurrentTime() {
        return currentTime;
    }
    public void setCurrentTime(GameTime currentTime) {
        this.currentTime = currentTime;
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Person> getCurrentPersons(Location location) {
        List<Person> result = new ArrayList<>();
        for (PersonCondition personCondition: currentChapter.personConditions()){
            if (personCondition.location().id().equals(location.id())) {
                List flags = personCondition.condition().consideredFlags();
                List<Flag<?>> currentValues = sessionFlags.getFlags(flags);
                Condition condition = personCondition.condition();
                boolean evaluated = condition.evaluate(currentValues);
                if (evaluated) {
                    result.add(personCondition.person());
                }
            }
        }
        return result;
    }

    public void setFlag(UUID id, Object value) {
        sessionFlags.setFlagValue(id, value);
    }

    /** Looks up a location of the adventure by its id. Used to resolve a verdict's targetId. */
    public Optional<Location> getLocation(UUID id) {
        return Optional.ofNullable(adventure.getLocation(id));
    }

    /** Looks up a person of the adventure by its id. Used to resolve a verdict's targetId. */
    public Optional<Person> getPerson(UUID id) {
        return Optional.ofNullable(adventure.getPerson(id));
    }
}

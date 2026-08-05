package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Session {

    private static final Logger LOGGER = LoggerFactory.getLogger(Session.class);

    private final Adventure adventure;
    private final Player player;
    public final ChatHistory chatHistory = new ChatHistory();
    public final TalkHistory talkHistory = new TalkHistory();

    private Location currentLocation;
    private Chapter currentChapter;
    private GameTime currentTime = GameTime.AFTERNOON;
    public final SessionFlags sessionFlags = new SessionFlags();
    public final SessionTriggers sessionTriggers = new SessionTriggers();

    public Session(Adventure adventure, Player player) {
        this.adventure = adventure;
        this.player = player;
        sessionFlags.init(adventure.getFlags());
    }

    public void start() {
        chatHistory.narrator(adventure.getMetadata().title());
        chatHistory.narrator(adventure.getMetadata().author());
        currentChapter = adventure.getChapters().getFirst();
        chatHistory.narrator(currentChapter.intro().intro());
        currentLocation = currentChapter.intro().startLocation();
        setCurrentTime(currentChapter.intro().startTime());
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

    /**
     * Advances the clock. The game-time flag is kept in sync, because that flag – not this field –
     * is what the chapter conditions evaluate: it decides which locations are open and where the
     * persons currently are.
     */
    public void setCurrentTime(GameTime currentTime) {
        this.currentTime = currentTime;
        setFlag(Flag.GAME_TIME_FLAG.id(), currentTime);
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    @SuppressWarnings({"rawtypes"})
    public List<Person> getCurrentPersons(Location location) {
        List<Person> result = new ArrayList<>();
        for (PersonCondition personCondition: currentChapter.personConditions()){
            if (personCondition.location().id().equals(location.id())) {
                List flags = personCondition.condition().consideredFlags();
                List<Flag> currentValues = sessionFlags.getFlags(flags);
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


    @SuppressWarnings({"rawtypes"})
    public Location getLocation(UUID id) {
        for(LocationCondition locationCondition: currentChapter.locationConditions() ){
            if (locationCondition.location().id().equals(id)) {

                List flags = locationCondition.condition().consideredFlags();
                List<Flag> currentValues = sessionFlags.getFlags(flags);
                Condition condition = locationCondition.condition();
                boolean evaluated = condition.evaluate(currentValues);
                if (evaluated) {
                    return locationCondition.location();
                }
            }
        }
        return null;
    }

    public Person getPerson(UUID id) {
        List<Person> personsHere = getCurrentPersons(currentLocation);
        Optional<Person> desiredPerson = personsHere.stream()
                .filter(person -> person.id().equals(id))
                .findFirst();
        return desiredPerson.orElse(null);
    }

    /**
     * The dialogs the given person can currently talk about: their person-specific dialogs
     * (whose conditions evaluate to true in the current chapter) plus the common gossip dialogs.
     * This is the set a TALK verdict's dialog must belong to.
     */
    @SuppressWarnings({"rawtypes"})
    public List<Dialog> getAvailableDialogs(Person person) {
        List<Dialog> dialogs = new ArrayList<>();
        for (DialogCondition dialogCondition : currentChapter.dialogConditions()) {
            if (dialogCondition.person().id().equals(person.id())) {
                List flags = dialogCondition.condition().consideredFlags();
                List<Flag> currentValues = sessionFlags.getFlags(flags);
                Condition condition = dialogCondition.condition();
                if (condition.evaluate(currentValues)) {
                    dialogs.add(dialogCondition.dialog());
                }
            }
        }
        return dialogs;
    }

    public void moveToNextChapter() {
        //TODO diesen teil als methode ausgliedern, refactoring
        int nextChapterIndex = -1;
        for (int i = 0; i < adventure.getChapters().size(); i++){
            if (adventure.getChapters().get(i).id().equals(currentChapter.id())){
                nextChapterIndex = i + 1;
                break;
            }
        }
        if (nextChapterIndex == -1){
            LOGGER.warn("index of next chapter could not be identified!");
        }
        currentChapter = adventure.getChapters().get(nextChapterIndex);
        chatHistory.narrator(currentChapter.intro().intro());
        currentLocation = currentChapter.intro().startLocation();
        setCurrentTime(currentChapter.intro().startTime());
    }

    public List<Trigger> getTriggers(List<UUID> triggers) {
        return adventure.getTriggers().stream().filter( t -> triggers.contains(t.id())).toList();
    }
}

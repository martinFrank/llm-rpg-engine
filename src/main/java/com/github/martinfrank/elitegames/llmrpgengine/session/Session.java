package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
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

    public void setCurrentTime(GameTime currentTime) {
        this.currentTime = currentTime;
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    public List<Person> getCurrentPersons(Location location) {
        List<Person> result = new ArrayList<>();
        for (PersonCondition personCondition: currentChapter.personConditions()){
            if (personCondition.location().id().equals(location.id())) {
                boolean evaluated = evaluate(personCondition.condition());
                if (evaluated) {
                    result.add(personCondition.person());
                }
            }
        }
        return result;
    }

    public Location getLocation(UUID id) {
        for(LocationCondition locationCondition: currentChapter.locationConditions() ){
            if (locationCondition.location().id().equals(id)) {
                boolean evaluated = evaluate(locationCondition.condition());
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
     * The dialogs the given person can currently talk about: the generic dialogs
     * ({@link Dialog#GENERIC}, e.g. gossip), which are never filtered out because everybody can
     * always make small talk, plus their person-specific dialogs whose conditions evaluate to true
     * in the current chapter. This is the set a TALK verdict's dialog must belong to.
     */
    public List<Dialog> getAvailableDialogs(Person person) {
        List<Dialog> dialogs = new ArrayList<>(Dialog.GENERIC);
        for (DialogCondition dialogCondition : currentChapter.dialogConditions()) {
            if (dialogCondition.person().id().equals(person.id())) {
                if (evaluate(dialogCondition.condition())) {
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

    public List<Trigger> getTriggers() {
        return adventure.getTriggers();
    }


    public boolean evaluate(Condition condition) {
        return  sessionFlags.evaluate(condition, currentTime);
    }

    public void handleEvent(Event event) {
        LOGGER.debug("handle event: newLocation: {}, newTime: {}", event.location().name(), event.gameTime());
        List<Flag<?>> flags = event.raisedFlags();
        if (flags != null && !flags.isEmpty()) {
            for (Flag<?> flag : flags) {
                LOGGER.debug("raise flag {}", flag.name());
                sessionFlags.raiseFlagValue(flag.id());
            }
        }
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {

    private final Adventure adventure;
    private final Player player;
    public final ChatHistory chatHistory = new ChatHistory();
    public final TalkHistory talkHistory = new TalkHistory();

    private Location currentLocation;
    private Chapter currentChapter;
    private GameTime currentTime = GameTime.AFTERNOON;
    public final SessionFlags sessionFlags = new SessionFlags();

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
        setFlag(Flag.GAME_TIME_FLAG.id(), GameTime.IN_THE_EVENING);
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


    /**
     * The locations the player can currently reach: those of the current chapter whose conditions
     * evaluate to true. This is the set a location id from a verdict must belong to.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Location> getAvailableLocations() {
        List<Location> locations = new ArrayList<>();
        for (LocationCondition locationCondition : currentChapter.locationConditions()) {
            List flags = locationCondition.condition().consideredFlags();
            List<Flag<?>> currentValues = sessionFlags.getFlags(flags);
            Condition condition = locationCondition.condition();
            if (condition.evaluate(currentValues) && !locations.contains(locationCondition.location())) {
                locations.add(locationCondition.location());
            }
        }
        return locations;
    }

    public Location getLocation(UUID id) {
        return Identifiable.find(id, getAvailableLocations());
    }

    public Person getPerson(UUID id) {
        return Identifiable.find(id, getCurrentPersons(currentLocation));
    }

    /**
     * Guardrail: resolves a location id as reported by an agent – a slightly mangled id still
     * resolves to the location it was meant to be, an invented one to {@code null}.
     * Only currently reachable locations are candidates.
     */
    public Location resolveLocation(String reportedId) {
        return Levenshtein.findClosest(reportedId, getAvailableLocations());
    }

    /**
     * Guardrail: resolves a person id as reported by an agent – a slightly mangled id still
     * resolves to the person it was meant to be, an invented one to {@code null}.
     * Only persons present at the current location are candidates.
     */
    public Person resolvePerson(String reportedId) {
        return Levenshtein.findClosest(reportedId, getCurrentPersons(currentLocation));
    }

    public Dialog getDialog(UUID id) {
        return adventure.getDialog(id);
    }

    /**
     * The dialogs the given person can currently talk about: their person-specific dialogs
     * (whose conditions evaluate to true in the current chapter) plus the common gossip dialogs.
     * This is the set a TALK verdict's dialog must belong to.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Dialog> getAvailableDialogs(Person person) {
        List<Dialog> dialogs = new ArrayList<>();
        for (DialogCondition dialogCondition : currentChapter.dialogConditions()) {
            if (dialogCondition.person().id().equals(person.id())) {
                List flags = dialogCondition.condition().consideredFlags();
                List<Flag<?>> currentValues = sessionFlags.getFlags(flags);
                Condition condition = dialogCondition.condition();
                if (condition.evaluate(currentValues)) {
                    dialogs.add(dialogCondition.dialog());
                }
            }
        }
        return dialogs;
    }

    public void applyFlagChange(FlagChange<?> flagChange) {
        sessionFlags.setFlagValue(flagChange.flag().id(),  flagChange.newValue());
    }
}

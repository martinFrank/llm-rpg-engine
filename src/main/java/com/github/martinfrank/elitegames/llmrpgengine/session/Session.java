package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.*;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.InvestigateCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /** Which adventure is being played – its title and who wrote it. */
    public Metadata getMetadata() {
        return adventure.getMetadata();
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

    public Location getLocation(Id id) {
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

    /**
     * The places that can be reached from the given location right now.
     * <p>
     * Guardrail: every destination is resolved through {@link #getLocation(Id)}, so a place the
     * current chapter does not carry – or whose condition does not hold, e.g. a shop that is closed
     * at night – is silently left out instead of being offered as a way that then refuses to be
     * walked. This is what makes the list safe to show the player.
     */
    public List<Location> getReachableLocations(Location from) {
        List<Location> reachable = new ArrayList<>();
        for (Id destinationId : from.destinationIds()) {
            Location destination = getLocation(destinationId);
            if (destination != null) {
                reachable.add(destination);
            }
        }
        return reachable;
    }

    /**
     * Everything the player has found out so far, in the order the adventure declares it.
     * <p>
     * The knowledge has to be read back through {@link #sessionFlags}: an authored
     * {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.KnowledgeFlag} is only
     * a template and reports {@code isRaised() == false} forever, while whether the player actually
     * knows the thing is session state. Asking the adventure's flag directly yields an empty result
     * no matter how far the player has come.
     */
    public List<Knowledge> getKnownKnowledge() {
        List<Knowledge> known = new ArrayList<>();
        for (Flag<?> flag : sessionFlags.getFlags(adventure.getFlags())) {
            if (flag.isRaised() && flag.content() instanceof Knowledge knowledge) {
                known.add(knowledge);
            }
        }
        return known;
    }

    public Person getPerson(Id id) {
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

    /**
     * What a closer look at the given subject – a {@link Location} or a {@link Person} – can turn
     * up right now: the investigations the current chapter scripts for exactly this subject and
     * whose condition holds.
     * <p>
     * Guardrail: the subject is matched by id, so an investigation authored for another place or
     * figure can never be run here, and the condition is what stops a discovery from being made a
     * second time (typically "not found yet"). A condition without an investigation is skipped
     * rather than handed out as {@code null}.
     */
    public List<Investigation> getAvailableInvestigations(Identifiable subject) {
        List<Investigation> investigations = new ArrayList<>();
        for (InvestigateCondition<?> investigateCondition : currentChapter.investigateConditions()) {
            if (investigateCondition.investigation() == null) {
                LOGGER.warn("investigate condition for '{}' has no investigation - ignored", investigateCondition.subject());
                continue;
            }
            if (investigateCondition.subject().id().equals(subject.id()) && evaluate(investigateCondition.condition())) {
                investigations.add(investigateCondition.investigation());
            }
        }
        return investigations;
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

    /**
     * Applies what an event changes about the session. An event only carries what it actually
     * changes, so every part of it is optional – reading one unconditionally (as the log line once
     * did with the location) turns a perfectly normal flag-only event into a crash.
     */
    public void handleEvent(Event event) {
        LOGGER.debug("handle event: newLocation: {}, newTime: {}",
                event.location() == null ? "-" : event.location().name(), event.gameTime());
        List<Flag<?>> flags = event.raisedFlags();
        if (flags != null && !flags.isEmpty()) {
            for (Flag<?> flag : flags) {
                LOGGER.debug("raise flag {}", flag.name());
                sessionFlags.raiseFlagValue(flag.id());
            }
        }
    }
}

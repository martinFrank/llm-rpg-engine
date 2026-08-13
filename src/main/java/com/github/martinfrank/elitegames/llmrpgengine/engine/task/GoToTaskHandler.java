package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Event;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Trigger;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

/**
 * Moves the player to the location resolved from {@link Verdict#resolvedTargetId()}. If the
 * verdict carries no resolvable destination at all, the current location is left unchanged.
 * <p>
 * "Ich gehe zu Ulf Stetten" is a move as much as "ich gehe zum Haus des Dorfvorstehers" is, and
 * which of the two the verdict agent reports back – the person or the place they are at – is not
 * something the same sentence decides twice the same way. So the destination is resolved through
 * several steps rather than from the location id alone: see {@link #resolveDestination}.
 */
@Component
public class GoToTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoToTaskHandler.class);

    private final NarratorAgent narratorAgent;

    public GoToTaskHandler(NarratorAgent narratorAgent) {
        this.narratorAgent = narratorAgent;
    }

    @Override
    public TaskType type() {
        return TaskType.GO_TO;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        Location destination = resolveDestination(verdict, session);
        if (destination != null) {
            setLocation(session, destination);
            return;
        }
        //wenn es bis jetzt kein reurn gab, ist was schief gelaufen
        LOGGER.debug("destination not reachable for GO_TO: '{}' (id: {})", verdict.target(), verdict.targetId());
        session.chatHistory.narrator("dieser Ort ist mir nicht bekannt");
    }

    /**
     * Where the player wants to go, or {@code null} if nothing in the verdict names a place that
     * exists right now. Four steps, from the most reliable statement to the least:
     * <ol>
     *   <li>the target id is a place – the normal case,</li>
     *   <li>the target id is a person: the destination is where that person currently is, which is
     *       what the player asked for when naming them,</li>
     *   <li>no id resolved, but the target name is a place of this chapter,</li>
     *   <li>the target name is a person of this chapter – again their current whereabouts.</li>
     * </ol>
     * Steps 3 and 4 are the safety net for a verdict that named the destination correctly but got
     * the id wrong ({@code "unbekannt"} or a place absent from the chapter): the same sentence
     * otherwise moved the player one time and was refused as an unknown place the next.
     */
    private Location resolveDestination(Verdict verdict, Session session) {
        Optional<Id> id = verdict.resolvedTargetId();
        if (id.isPresent()) {
            Location location = session.getLocation(id.get());
            if (location != null) {
                return location;
            }
            Location whereThePersonIs = session.getLocationOf(id.get());
            if (whereThePersonIs != null) {
                LOGGER.debug("GO_TO target '{}' is a person - destination is where they are: {}",
                        id.get(), whereThePersonIs.name());
                return whereThePersonIs;
            }
        }
        Location namedLocation = session.findLocationByName(verdict.target());
        if (namedLocation != null) {
            LOGGER.debug("GO_TO target id '{}' did not resolve - matched location by name: {}",
                    verdict.targetId(), namedLocation.name());
            return namedLocation;
        }
        Person namedPerson = session.findPersonByName(verdict.target());
        if (namedPerson != null) {
            Location whereThePersonIs = session.getLocationOf(namedPerson.id());
            if (whereThePersonIs != null) {
                LOGGER.debug("GO_TO target id '{}' did not resolve - matched person '{}' by name, destination is {}",
                        verdict.targetId(), namedPerson.name(), whereThePersonIs.name());
                return whereThePersonIs;
            }
        }
        return null;
    }

    private void setLocation(Session session, Location location) {
        LOGGER.debug("Player moves to: {}", location.name());

        handleLeaveLocationTrigger(session, session.getCurrentLocation());
        session.setCurrentLocation(location);
        handleEnterLocationTrigger(session, location);
        NarratorContext context = NarratorContext.generateWalkToContext(session, location);
        long now = System.currentTimeMillis();
        String narration = narratorAgent.narrate(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration narration evaluation: {} ms", duration);
        session.chatHistory.narrator(narration);
    }
    private void handleLeaveLocationTrigger(Session session, Location location) {
        handleLocationTrigger(session, location, "LEAVE");
    }

    private void handleEnterLocationTrigger(Session session, Location location) {
        handleLocationTrigger(session, location, "ENTER");
    }

    private void handleLocationTrigger(Session session, Location location, String direction) {
        List<Trigger> locationTriggers = session.getTriggers().stream().filter( t -> location.triggerIds().contains(t.id())).toList();
        List<Trigger> triggers = session.sessionTriggers.untriggered(locationTriggers);
        for (Trigger trigger : triggers) {
            LOGGER.debug("handle on {} {} trigger", direction, location.name());
            Event event = trigger.event();
            session.handleEvent(event);
        }
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Event;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
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
import java.util.UUID;

/**
 * Moves the player to the location resolved from {@link Verdict#targetUuid()}. If the
 * verdict carries no resolvable location id, the current location is left unchanged.
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
        Optional<UUID> id = verdict.targetUuid();
        if (id.isPresent()) {
            Location location = session.getLocation(id.get());
            if (location != null) {
                setLocation(session, location);
                return;
            }
        }
        //wenn es bis jetzt kein reurn gab, ist was schief gelaufen
        LOGGER.debug("destination not reachable for GO_TO: '{}' (id: {})", verdict.target(), verdict.targetId());
        session.chatHistory.narrator("dieser Ort ist mir nicht bekannt");
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
        List<Trigger> locationTriggers = session.getTriggers().stream().filter( t -> location.triggers().contains(t.id())).toList();
        List<Trigger> triggers = session.sessionTriggers.untriggered(locationTriggers);
        for (Trigger trigger : triggers) {
            LOGGER.debug("handle on {} {} trigger", direction, location.name());
            Event event = trigger.event();
            session.handleEvent(event);
        }
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Moves the player to the location resolved from {@link Verdict#targetUuid()}. If the
 * verdict carries no resolvable location id, the current location is left unchanged.
 */
@Component
public class GoToTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoToTaskHandler.class);

    @Autowired
    private NarratorAgent narratorAgent;

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
            } else {
                LOGGER.debug("No known destination for GO_TO: '{}' (id: {})", verdict.target(), verdict.targetId());
            }
        }
    }

    private void setLocation(Session session, Location location) {
        LOGGER.debug("Player moves to: {}", location.name());
        session.setCurrentLocation(location);

        NarratorContext context = NarratorContext.generateWalkToContext(session, location);
        long now = System.currentTimeMillis();
        String narration = narratorAgent.narrate(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration narration evaluation: {} ms", duration);
//        LOGGER.debug("Narration: {}", narration);
        session.chatHistory.narrator(narration);
    }
}

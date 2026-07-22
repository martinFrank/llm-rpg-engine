package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles the player actively interacting with an object named in {@link Verdict#target()} –
 * e.g. opening a door or letter, pressing a button, flipping a switch, or touching an orb.
 * <p>
 * Interactable objects are not yet modelled with names or effects, so for now this handler
 * only records the interaction intent. Once such objects (and their effects on the session)
 * exist, the resolution and state changes are added here.
 */
@Component
public class InteragierenTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InteragierenTaskHandler.class);

    @Override
    public TaskType type() {
        return TaskType.INTERAGIEREN;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        LOGGER.debug("Spieler interagiert mit: '{}'", verdict.target());
    }
}

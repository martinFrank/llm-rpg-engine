package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles the player addressing and communicating with the person resolved from
 * {@link Verdict#targetUuid()} (an id from the available-persons list). If the verdict
 * carries no resolvable person id, nothing happens.
 * <p>
 * A conversation state (and a dedicated dialogue agent) is not modelled yet, so for now
 * this handler only resolves and records person the player wants to talk to.
 */
@Component
public class SprechenTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SprechenTaskHandler.class);

    @Override
    public TaskType type() {
        return TaskType.SPRECHEN;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        if (verdict.targetUuid().isPresent()) {
            Person person = session.getPerson(verdict.targetUuid().get());
            if (person != null) {
                LOGGER.debug("Spieler spricht mit: {}", person.name());
            } else {
                LOGGER.info("Kein bekannter Gesprächspartner für SPRECHEN: '{}' (id: {})", verdict.target(), verdict.targetId());
            }
        }

    }
}

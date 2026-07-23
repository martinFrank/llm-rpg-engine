package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.*;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves what the player wants to investigate from {@link Verdict#targetUuid()} against
 * the session: first a location, then a person. Investigating does not change the session
 * state – the resolved subject is what the Narrator later describes to the player. If the
 * target cannot be resolved, nothing happens.
 * <p>
 * Items are covered by the task conceptually, but are not yet modelled, so they cannot be
 * resolved here yet.
 */
@Component
public class UntersuchenTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UntersuchenTaskHandler.class);

    @Autowired
    private NarratorAgent narratorAgent;

    @Override
    public TaskType type() {
        return TaskType.UNTERSUCHEN;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        Optional<UUID> targetId = verdict.targetUuid();

        if (targetId.isPresent()) {
            Location location = session.getLocation(targetId.get());
            if (location != null) {
                inspectLocation(session, location);
                return;
            }

            Person person = session.getPerson(targetId.get());
            if (person != null) {
                inspectPerson(session, person);
                return;
            }

            LOGGER.debug("Kein bekanntes Untersuchungsziel für UNTERSUCHEN: '{}' (id: {})", verdict.target(), verdict.targetId());
        }
    }

    private void inspectLocation(Session session, Location location) {
        LOGGER.debug("Spieler untersucht den Ort: {}", location.name());
        NarratorContext context = NarratorContext.generateInspectLocation(session, location);
        long now = System.currentTimeMillis();
        String narration = narratorAgent.narrate(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration narration evaluation: {} ms", duration);
        LOGGER.debug("Narration: {}", narration);
        session.chatHistory.narrator(narration);
    }

    private void inspectPerson(Session session, Person person) {
        LOGGER.debug("Spieler untersucht die Person: {}", person.name());
    }
}

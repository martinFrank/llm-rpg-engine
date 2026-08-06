package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.*;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Resolves what the player wants to investigate from {@link Verdict#targetUuid()} against
 * the session: first a location, then a person. Investigating does not change the session
 * state – the resolved subject is what the Narrator later describes to the player.
 * <p>
 * A target that resolves to nothing falls back to the place the player is standing in: an
 * unresolvable target still means the player is looking around here, and describing the
 * current location is always a truthful answer – better than the silent dead turn that
 * returning early would produce.
 * <p>
 * Items are covered by the task conceptually, but are not yet modelled, so they cannot be
 * resolved here yet.
 */
@Component
public class InvestigateTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvestigateTaskHandler.class);

    private final NarratorAgent narratorAgent;

    public InvestigateTaskHandler(NarratorAgent narratorAgent) {
        this.narratorAgent = narratorAgent;
    }

    @Override
    public TaskType type() {
        return TaskType.INVESTIGATE;
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
        }

        LOGGER.debug("No known investigation target for INVESTIGATE: '{}' (id: {}) - falling back to the current location",
                verdict.target(), verdict.targetId());
        inspectLocation(session, session.getCurrentLocation());
    }

    private void inspectLocation(Session session, Location location) {
        LOGGER.debug("Player investigates the location: {}", location.name());
        narrate(session, NarratorContext.generateInspectLocationContext(session, location));
    }

    private void inspectPerson(Session session, Person person) {
        LOGGER.debug("Player investigates the person: {}", person.name());
        narrate(session, NarratorContext.generateInspectPersonContext(session, person));
    }

    private void narrate(Session session, NarratorContext context) {
        long now = System.currentTimeMillis();
        String narration = narratorAgent.narrate(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration narration evaluation: {} ms", duration);
        session.chatHistory.narrator(narration);
    }
}

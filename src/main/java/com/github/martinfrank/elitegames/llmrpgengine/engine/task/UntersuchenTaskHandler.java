package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves what the player wants to investigate ({@link Verdict#target()}) against the
 * session: first a location, then a person present at the current location. Investigating
 * does not change the session state – the resolved subject is what the Narrator later
 * describes to the player. If the target cannot be resolved, nothing happens.
 * <p>
 * Items are covered by the task conceptually, but are not yet modelled with names, so they
 * cannot be resolved here yet.
 */
@Component
public class UntersuchenTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UntersuchenTaskHandler.class);

    @Override
    public TaskType type() {
        return TaskType.UNTERSUCHEN;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        Optional<Location> location = Optional.empty(); //FIXME aus verdict holen
        if (location.isPresent()) {
            LOGGER.debug("Spieler untersucht den Ort: {}", location.get().name());
            return;
        }


        Optional<Person> person = Optional.empty(); //FIXME aus verdict ziehen
        if (person.isPresent()) {
            LOGGER.debug("Spieler untersucht die Person: {}", person.get().name());
            return;
        }

        LOGGER.info("Kein bekanntes Untersuchungsziel für UNTERSUCHEN: '{}'", verdict.target());
    }
}

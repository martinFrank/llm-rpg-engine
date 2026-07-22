package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Moves the player to the location resolved from {@link Verdict#targetUuid()}. If the
 * verdict carries no resolvable location id, the current location is left unchanged.
 */
@Component
public class GeheZuTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeheZuTaskHandler.class);

    @Override
    public TaskType type() {
        return TaskType.GEHEZU;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        Optional<Location> target = verdict.targetUuid().flatMap(session::getLocation);
        if (target.isPresent()) {
            session.setCurrentLocation(target.get());
            LOGGER.debug("Spieler bewegt sich nach: {}", target.get().name());
        } else {
            LOGGER.info("Kein bekannter Zielort für GEHEZU: '{}' (id: {})", verdict.target(), verdict.targetId());
        }
    }
}

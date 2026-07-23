package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
        Optional<UUID> id = verdict.targetUuid();
        if (id.isPresent()) {
            Location location = session.getLocation(id.get());
            if (location != null) {
                session.setCurrentLocation(location);
                LOGGER.debug("Spieler bewegt sich nach: {}", location.name());
            } else {
                LOGGER.debug("Kein bekannter Zielort für GEHEZU: '{}' (id: {})", verdict.target(), verdict.targetId());
            }
        }
    }
}

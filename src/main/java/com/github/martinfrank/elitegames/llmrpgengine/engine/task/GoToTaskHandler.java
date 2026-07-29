package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Moves the player to the location resolved from {@link Verdict#targetId()} (guardrail: via
 * {@link Session#resolveLocation(String)}, so a slightly mangled id still resolves). If the
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
        Location location = session.resolveLocation(verdict.targetId());
        if (location != null) {
            setLocation(session, location);
        } else if (verdict.hasTargetId()) {
            LOGGER.debug("No known destination for GO_TO: '{}' (id: {})", verdict.target(), verdict.targetId());
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

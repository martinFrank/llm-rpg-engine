package com.github.martinfrank.elitegames.llmrpgengine.engine;

import com.github.martinfrank.elitegames.llmrpgengine.agent.*;
import com.github.martinfrank.elitegames.llmrpgengine.engine.task.TaskHandler;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GameEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameEngine.class);

    private final VerdictAgent verdictAgent;
    private final NarratorAgent narratorAgent;
    private final Map<TaskType, TaskHandler> taskHandlers;

    public GameEngine(VerdictAgent verdictAgent,
                      NarratorAgent narratorAgent,
                      List<TaskHandler> taskHandlers) {
        this.verdictAgent = verdictAgent;
        this.narratorAgent = narratorAgent;
        this.taskHandlers = taskHandlers.stream()
                .collect(Collectors.toMap(TaskHandler::type, Function.identity()));
    }

    /**
     * Interprets the player's input via the {@link VerdictAgent} and applies the
     * resulting scripted task to the session.
     *
     * @return the verdict that was produced and applied
     */
    public void handleUserInput(String userInput, Session session) {
        VerdictContext context = VerdictContext.generate(session);
//        LOGGER.debug("Context: {}", context);
        long now = System.currentTimeMillis();
        Verdict verdict = verdictAgent.evaluate(context, userInput);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration verdict evaluation: {} ms", duration);
        LOGGER.debug("Verdict: {}", verdict);
        verdict = sanitize(verdict, session);
        session.chatHistory.player(userInput);
        applyTask(verdict, session);
    }

    /**
     * Guardrail: reconcile the (possibly imperfect) verdict with the actual game state before it
     * is applied. A {@link TaskType#TALK} whose target is not a person present here is redirected:
     * if the target is actually a location it becomes {@link TaskType#INVESTIGATE} (looking around
     * that place), otherwise {@link TaskType#UNKNOWN}. This keeps a mis-classified input from
     * producing a dead turn (TALK against a location id would resolve to no conversation partner).
     */
    private Verdict sanitize(Verdict verdict, Session session) {
        if (verdict.task() != TaskType.TALK) {
            return verdict;
        }
        boolean pointsToPerson = verdict.targetUuid().map(session::getPerson).isPresent();
        if (pointsToPerson) {
            return verdict;
        }
        boolean pointsToLocation = verdict.targetUuid().map(session::getLocation).isPresent();
        TaskType fallback = pointsToLocation ? TaskType.INVESTIGATE : TaskType.UNKNOWN;
        LOGGER.info("Guardrail: TALK without a present person (target='{}', id={}) -> {}",
                verdict.target(), verdict.targetId(), fallback);
        return new Verdict(verdict.interpretation(), fallback, verdict.target(), verdict.targetId(),
                "", Verdict.UNKNOWN);
    }

    private void applyTask(Verdict verdict, Session session) {
        TaskHandler handler = taskHandlers.get(verdict.task());
        if (handler != null) {
            handler.execute(verdict, session);
        } else {
            LOGGER.info("No handler registered for task: {}", verdict.task());
        }
    }
}

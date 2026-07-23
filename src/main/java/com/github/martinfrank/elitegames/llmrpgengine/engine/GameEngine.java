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
        session.chatHistory.player(userInput);
        applyTask(verdict, session);
    }

    private void applyTask(Verdict verdict, Session session) {
        TaskHandler handler = taskHandlers.get(verdict.task());
        if (handler != null) {
            handler.execute(verdict, session);
        } else {
            LOGGER.info("Keine Aufgabe registriert für Task: {}", verdict.task());
        }
    }
}

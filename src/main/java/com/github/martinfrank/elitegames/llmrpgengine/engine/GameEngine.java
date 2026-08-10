package com.github.martinfrank.elitegames.llmrpgengine.engine;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
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
        progressChapter(session);
    }

    private void progressChapter(Session session) {
        boolean isCurrentChapterOver = session.evaluate(session.getCurrentChapter().chapterFinishedCondition());
        LOGGER.debug("is current chapter finished? {}", isCurrentChapterOver);
        if(isCurrentChapterOver) {
            session.moveToNextChapter();
        }
    }

    /**
     * Guardrail: reconcile the (possibly imperfect) verdict with the actual game state before it
     * is applied. A {@link TaskType#TALK} whose target is not a person present here has no
     * conversation partner and would produce a dead turn, so it is redirected to
     * {@link TaskType#INVESTIGATE}: of the location the target points at, or – when the target
     * resolves to nothing at all – of the place the player is standing in. The latter is the
     * common case for a question the player asks the game rather than a person ("gibt es hier
     * einen Schmied?"): they want to know what is here, which is exactly looking around.
     */
    private Verdict sanitize(Verdict verdict, Session session) {
        if (verdict.task() != TaskType.TALK) {
            return verdict;
        }
        boolean pointsToPerson = verdict.resolvedTargetId().map(session::getPerson).isPresent();
        if (pointsToPerson) {
            return verdict;
        }
        Location location = verdict.resolvedTargetId()
                .map(session::getLocation)
                .orElseGet(session::getCurrentLocation);
        LOGGER.info("Guardrail: TALK without a present person (target='{}', id={}) -> INVESTIGATE '{}'",
                verdict.target(), verdict.targetId(), location.name());
        return new Verdict(verdict.interpretation(), TaskType.INVESTIGATE,
                location.name(), location.id().toString());
    }

    private void applyTask(Verdict verdict, Session session) {
        TaskHandler handler = taskHandlers.get(verdict.task());
        if (handler != null) {
            handler.execute(verdict, session);
        } else {
            LOGGER.warn("No handler registered for task: {}", verdict.task());
        }
    }
}

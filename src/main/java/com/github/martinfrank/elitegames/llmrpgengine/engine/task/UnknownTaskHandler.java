package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorContext;
import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles input that could not be mapped onto any scripted task. It changes nothing in the
 * session, but it does answer: without this handler an unmapped input produced a dead turn –
 * the player's line went into the history and the game stayed silent. The Narrator says, in
 * character, that this is not possible here.
 */
@Component
public class UnknownTaskHandler implements TaskHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnknownTaskHandler.class);

    private final NarratorAgent narratorAgent;

    public UnknownTaskHandler(NarratorAgent narratorAgent) {
        this.narratorAgent = narratorAgent;
    }

    @Override
    public TaskType type() {
        return TaskType.UNKNOWN;
    }

    @Override
    public void execute(Verdict verdict, Session session) {
        LOGGER.debug("Input could not be mapped to a task: '{}'", verdict.interpretation());
        NarratorContext context = NarratorContext.generateUnknownTaskContext(session);
        long now = System.currentTimeMillis();
        String narration = narratorAgent.narrate(context);
        long duration = System.currentTimeMillis() - now;
        LOGGER.info("Duration narration evaluation: {} ms", duration);
        session.chatHistory.narrator(narration);
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.engine.task;

import com.github.martinfrank.elitegames.llmrpgengine.agent.TaskType;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;

/**
 * Applies the scripted changes of a single {@link TaskType} to a session.
 * <p>
 * Add a new Spring-managed implementation for every new task; the {@code GameEngine}
 * discovers them automatically and dispatches by {@link #type()}.
 */
public interface TaskHandler {

    /** The task this handler is responsible for. */
    TaskType type();

    /** Applies the verdict's task to the session. */
    void execute(Verdict verdict, Session session);
}

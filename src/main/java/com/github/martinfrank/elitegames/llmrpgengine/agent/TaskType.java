package com.github.martinfrank.elitegames.llmrpgengine.agent;

/**
 * The set of scripted follow-up tasks the engine knows how to apply to a session.
 * <p>
 * The {@link VerdictAgent} maps a player's free-text input onto exactly one of these.
 * Keep this list in sync with the task descriptions in {@code prompts/verdict-system.st}
 * and with the registered {@code TaskHandler}s. Extend it as new scripted actions are added.
 */
public enum TaskType {

    /** The player wants to move to another location. Parameter {@code target}: the destination's name. */
    GEHEZU,

    /** Fallback: the input could not be mapped to any known task. */
    UNBEKANNT
}

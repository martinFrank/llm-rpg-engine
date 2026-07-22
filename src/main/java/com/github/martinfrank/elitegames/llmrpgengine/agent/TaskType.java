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

    /**
     * The player wants to investigate something more closely – a location, an item, or a person.
     * Parameter {@code target}: the name of the thing to investigate, as it appears in the context.
     */
    UNTERSUCHEN,

    /**
     * The player wants to actively interact with an object – e.g. open a door or a letter,
     * press a button, flip a switch, or touch a magical orb. This is about manipulating or
     * operating something, as opposed to merely looking at it ({@link #UNTERSUCHEN}).
     * Parameter {@code target}: the name of the object to interact with, as it appears in the context.
     */
    INTERAGIEREN,

    /**
     * The player wants to address a person and communicate with them – talk to, greet,
     * ask, or answer someone. Parameter {@code target}: the person's name; {@code targetId}:
     * the person's id from the available-persons list, or {@value Verdict#UNKNOWN}.
     */
    SPRECHEN,

    /** Fallback: the input could not be mapped to any known task. */
    UNBEKANNT
}

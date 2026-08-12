package com.github.martinfrank.elitegames.llmrpgengine.agent;

/**
 * The set of scripted follow-up tasks the engine knows how to apply to a session.
 * <p>
 * The {@link VerdictAgent} maps a player's free-description input onto exactly one of these.
 * Keep this list in sync with the task descriptions in {@code prompts/verdict-system.st}
 * and with the registered {@code TaskHandler}s. Extend it as new scripted actions are added.
 */
public enum TaskType {

    /** The player wants to move to another location. Parameter {@code target}: the destination's name. */
    GO_TO,

    /**
     * The player wants to investigate something more closely – a location, an item, or a person.
     * Parameter {@code target}: the name of the thing to investigate, as it appears in the context.
     */
    INVESTIGATE,

    /**
     * The player wants to actively interact with an object – e.g. open a door or a letter,
     * press a button, flip a switch, or touch a magical orb. This is about manipulating or
     * operating something, as opposed to merely looking at it ({@link #INVESTIGATE}).
     * Parameter {@code target}: the name of the object to interact with, as it appears in the context.
     */
    INTERACT,

    /**
     * The player wants to address a person and communicate with them – talk to, greet,
     * ask, or answer someone. Parameter {@code target}: the person's name; {@code targetId}:
     * the person's id from the available-persons list, or {@value Verdict#UNKNOWN}.
     */
    TALK,

    /**
     * The player asks the game master about the state of the game rather than acting in the
     * fiction – "wo bin ich?", "wohin kann ich gehen?", "wie spät ist es?", "was war meine
     * Aufgabe?". Parameter {@code facet}: which fact is being asked for, see
     * {@link GameMasterFacet}; {@code target} and {@code targetId} stay empty.
     * <p>
     * This is deliberately not an {@link #INVESTIGATE}: looking around is game mechanics – it
     * rolls the chapter's skill checks and can use up a one-time discovery – while asking what
     * time it is must never cost the player anything.
     */
    ASK_GAME_MASTER,

    /** Fallback: the input could not be mapped to any known task. */
    UNKNOWN
}

package com.github.martinfrank.elitegames.llmrpgengine.agent;

/**
 * Which fact about the game state a {@link TaskType#ASK_GAME_MASTER} question asks for.
 * <p>
 * Every facet is answerable from the session alone, which is the point: the answer is assembled
 * from the actual game state instead of being narrated, so it cannot invent a smith, a path or a
 * time of day that does not exist. Only extend this list by a fact the session really holds –
 * a facet the engine cannot answer truthfully is worse than {@link #UNSPECIFIED}.
 * <p>
 * Keep in sync with the facet descriptions in {@code prompts/verdict-system.st} and with the
 * branches of {@code AskGameMasterTaskHandler}.
 */
public enum GameMasterFacet {

    /** "wo bin ich?" – the place the player is standing in. */
    WHERE_AM_I,

    /** "wohin kann ich gehen?" – the destinations reachable from here in this chapter. */
    WHERE_CAN_I_GO,

    /** "wer ist hier?" – the persons present at the current location right now. */
    WHO_IS_HERE,

    /** "wie spät ist es?" – the current time of day. */
    WHAT_TIME_IS_IT,

    /**
     * "was war meine Aufgabe?", "was weiß ich?" – the briefing the player received plus
     * everything they have found out since. Both questions are the same query: what the player
     * knows <em>is</em> their task, so there is no separate task facet.
     */
    WHAT_DO_I_KNOW,

    /** Fallback: a question to the game master whose subject could not be pinned down. */
    UNSPECIFIED
}

package com.github.martinfrank.elitegames.llmrpgengine.session;

/**
 * One line of the game log.
 *
 * @param kind whether the line belongs to the story or is an aside between the player and the
 *             game master – see {@link Kind}
 */
public record ChatEntry(String actor, String statement, Kind kind){

    /**
     * What a log line is. The player reads both kinds, but only {@link #STORY} is the story:
     * {@link #META} lines are questions about the game state and their answers, and feeding those
     * back into the agents' prompts would let bookkeeping crowd the actual plot out of the
     * context window – see {@link ChatHistory#getLatestStoryEntries(int)}.
     */
    public enum Kind {
        STORY,
        META
    }

    /** A story line, which is what all but the game master's asides are. */
    public ChatEntry(String actor, String statement) {
        this(actor, statement, Kind.STORY);
    }

    @Override
    public String toString() {
        return actor + ": " + statement;
    }
}

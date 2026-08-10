package com.github.martinfrank.elitegames.llmrpgengine.adventure;

/**
 * Whether the player is good enough (or lucky enough) to actually make a discovery. The roll is
 * not made here: the caller supplies it, so that the engine owns the randomness and a test can
 * decide the outcome instead of fighting a coin flip.
 */
public record SkillCheck(double successChance) {

    /** Used by {@link #SkillCheck()} for a check the adventure gives no difficulty for yet. */
    public static final double DEFAULT_SUCCESS_CHANCE = 0.5;

    public SkillCheck() {
        this(DEFAULT_SUCCESS_CHANCE);
    }

    /**
     * @param roll a value in {@code [0,1)}, e.g. from {@link Math#random()}
     * @return whether the check succeeded
     */
    public boolean check(double roll) {
        return roll < successChance;
    }
}

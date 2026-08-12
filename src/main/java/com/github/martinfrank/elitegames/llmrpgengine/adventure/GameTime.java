package com.github.martinfrank.elitegames.llmrpgengine.adventure;

/**
 * The times of day the game knows. The world has no clock: this is the finest resolution there
 * is, which is why a player asking "wie spät ist es?" is told a time of day and not an hour.
 *
 * @param promptLabel the adverbial form the agents' prompts are written with ("nachmittag",
 *                    "abends") – it appears in the TAGESZEIT field of the narrator prompt
 * @param label       the noun the game master names in an answer to the player ("Nachmittag",
 *                    "Abend"), so a sentence like "Es ist Mitternacht." reads correctly
 */
public enum GameTime {

    DAWN("Sonnenaufgang", "Sonnenaufgang"),
    MORNING("morgens", "Morgen"),
    HIGH_NOON("mittags", "Mittag"),
    AFTERNOON("nachmittag", "Nachmittag"),
    IN_THE_EVENING("abends", "Abend"),
    AT_NIGHT("nachts", "Nacht"),
    DUSK("Sonnenuntergang", "Sonnenuntergang"),
    MIDNIGHT("mitternachts", "Mitternacht"),
    ;

    private final String promptLabel;
    private final String label;

    GameTime(String promptLabel, String label) {
        this.promptLabel = promptLabel;
        this.label = label;
    }

    /** The wording used inside the agents' prompts. */
    public String promptLabel() {
        return promptLabel;
    }

    /** The wording used in an answer the player reads. */
    public String label() {
        return label;
    }
}

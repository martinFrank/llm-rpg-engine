package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the current game time reaches the chapter conditions. The conditions evaluate the
 * game-time flag, not {@link Session#getCurrentTime()}, so the two must never drift apart: the flag
 * is what decides which locations are open and where the persons are.
 */
class SessionTest {

    private static final UUID DORFPLATZ = UUID.fromString("0a5df08a-2094-4fbf-a94f-ce6fd74ddfee");
    private static final UUID SCHMIEDE = UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3");
    private static final UUID WIRTSHAUS = UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f");
    private static final UUID HAUS_DES_DORFVORSTEHERS = UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2");
    private static final UUID ULF_STETTEN = UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4");
    private static final UUID RANGOLF_KLINGBEIL = UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636");
    private static final UUID AUFTRAG_ERHALTEN = UUID.fromString("8d824f02-f2ef-4ee2-93f7-89b7e69fef7b");
    private static final UUID SCHLUESSEL_GEFUNDEN = UUID.fromString("67b9fbe4-dbc9-4e57-94b6-3a4d7f803831");

    private final Buchenhain adventure = new Buchenhain();

    private Session startedSession() {
        Session session = new Session(adventure, new Player("Thorsten"));
        session.start();
        return session;
    }

    /** Straight from the adventure, so a location can be looked up regardless of its condition. */
    private Location location(UUID id) {
        return adventure.getLocation(id);
    }


    @Test
    void daytimeConditionsHoldInTheMorning() {
        Session session = startedSession();

        session.setCurrentTime(GameTime.MORNING);

        // The smith keeps his forge during the day; a morning treated as evening would empty it.
        assertThat(session.getCurrentPersons(location(SCHMIEDE))).extracting(Person::name)
                .containsExactly("Rangolf Klingbeil");
        assertThat(session.getLocation(SCHMIEDE)).isNotNull();
    }

    @Test
    void personsMoveToTheirEveningLocation() {
        Session session = startedSession();

        session.setCurrentTime(GameTime.IN_THE_EVENING);

        assertThat(session.getCurrentPersons(location(WIRTSHAUS))).extracting(Person::name)
                .contains("Ulf Stetten");
        assertThat(session.getCurrentPersons(location(HAUS_DES_DORFVORSTEHERS))).isEmpty();
    }

    /**
     * Gossip is a generic dialog: it belongs to no chapter and to no person, so the condition
     * filter must never drop it. Without it a figure the current chapter scripts no dialog for
     * would offer nothing to talk about at all.
     */
    @Test
    void gossipIsAvailableForEveryPerson() {
        Session session = startedSession();

        // Ulf Stetten has a scripted dialog in chapter 1, Rangolf Klingbeil has none.
        assertThat(session.getAvailableDialogs(adventure.getPerson(ULF_STETTEN))).extracting(Dialog::topic)
                .containsExactly(Dialog.GOSSIP.topic(), "Auftrag des Ortsvorstehers");
        assertThat(session.getAvailableDialogs(adventure.getPerson(RANGOLF_KLINGBEIL)))
                .containsExactly(Dialog.GOSSIP);
    }

    @Test
    void personsAreAtTheirDaytimeLocationAfterTheIntro() {
        // Buchenhain starts in the afternoon, so the daytime conditions must hold right away.
        Session session = startedSession();

        assertThat(session.getCurrentPersons(location(HAUS_DES_DORFVORSTEHERS))).extracting(Person::name)
                .containsExactly("Ulf Stetten");
        assertThat(session.getCurrentPersons(location(SCHMIEDE))).extracting(Person::name)
                .containsExactly("Rangolf Klingbeil");
    }

    /**
     * A destination the current chapter does not carry is not a way out: the Dorfplatz points at the
     * Blumental, but chapter 1 has no such location, so it may not be offered as reachable.
     */
    @Test
    void reachableLocationsSkipWhatTheChapterDoesNotCarry() {
        Session session = startedSession();

        assertThat(session.getReachableLocations(location(DORFPLATZ))).extracting(Location::name)
                .containsExactly("Haus des Dorfvorstehers", "Die Dorf Schmiede",
                        "Wirtshaus zum kleinen Adler", "Der Dorfladen");
    }

    @Test
    void reachableLocationsFollowTheTimeOfDay() {
        Session session = startedSession();

        session.setCurrentTime(GameTime.AT_NIGHT);

        assertThat(session.getReachableLocations(location(DORFPLATZ))).extracting(Location::name)
                .containsExactly("Wirtshaus zum kleinen Adler");
    }

    /**
     * The knowledge has to be read back through the session's flags. An authored
     * {@link com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.KnowledgeFlag} reports
     * {@code isRaised() == false} forever, so asking the adventure's flags directly would report
     * that the player knows nothing no matter how far they have come.
     */
    @Test
    void knownKnowledgeGrowsWithTheRaisedFlags() {
        Session session = startedSession();
        assertThat(session.getKnownKnowledge()).isEmpty();

        session.sessionFlags.raiseFlagValue(AUFTRAG_ERHALTEN);

        assertThat(session.getKnownKnowledge()).extracting(Knowledge::name)
                .containsExactly("Auftrag des Ortsvorstehers");
    }

    /** Item and location flags are no journal entries, so only knowledge may show up. */
    @Test
    void knownKnowledgeIgnoresFlagsThatCarryNoKnowledge() {
        Session session = startedSession();

        session.sessionFlags.raiseFlagValue(SCHLUESSEL_GEFUNDEN);

        assertThat(session.getKnownKnowledge()).isEmpty();
    }
}

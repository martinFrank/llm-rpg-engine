package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Chapter;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Intro;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.TinyAdventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

import java.util.List;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the current game time reaches the chapter conditions. The conditions evaluate the
 * game-time flag, not {@link Session#getCurrentTime()}, so the two must never drift apart: the flag
 * is what decides which locations are open and where the persons are.
 */
class SessionTest {

    private static final Id DORFPLATZ = Id.of("location.dorfplatz");
    private static final Id SCHMIEDE = Id.of("location.dorfschmiede");
    private static final Id WIRTSHAUS = Id.of("location.wirtshaus-zum-adler");
    private static final Id HAUS_DES_DORFVORSTEHERS = Id.of("location.haus-des-dorfvorstehers");
    private static final Id ULF_STETTEN = Id.of("person.ulf-stetten");
    private static final Id RANGOLF_KLINGBEIL = Id.of("person.rangolf-klingbeil");
    private static final Id AUFTRAG_ERHALTEN = Id.of("flag.auftrag-erhalten");
    private static final Id SCHLUESSEL_GEFUNDEN = Id.of("flag.schluessel-gefunden");

    private final Adventure adventure = new Buchenhain().build();

    private Session startedSession() {
        Session session = new Session(adventure, new Player("Thorsten"));
        session.start();
        return session;
    }

    /** Straight from the adventure, so a location can be looked up regardless of its condition. */
    private Location location(Id id) {
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

    /**
     * A chapter that names no start location continues where the player is.
     * <p>
     * Copying the intro over unconditionally put them nowhere and the clock at no time, and every
     * later read of the session then failed on a {@code null} that had nothing to do with where it
     * surfaced – the page rendering the state, several turns after the chapter had turned over.
     */
    @Test
    void aChapterWithoutAStartLocationContinuesWhereThePlayerIs() {
        Session session = new Session(twoChapters(), new Player("Thorsten"));
        session.start();
        session.setCurrentTime(GameTime.IN_THE_EVENING);

        session.moveToNextChapter();

        assertThat(session.getCurrentChapter().name()).isEqualTo("Zweites Kapitel");
        assertThat(session.getCurrentLocation()).isNotNull()
                .extracting(Location::name).isEqualTo("Dorfplatz");
        assertThat(session.getCurrentTime()).isEqualTo(GameTime.IN_THE_EVENING);
    }

    /** Two chapters over one place, the second of which does not say where or when it begins. */
    private static Adventure twoChapters() {
        Location dorfplatz = TinyAdventure.location("location.dorfplatz", "Dorfplatz");
        return new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(dorfplatz);
            }

            @Override protected List<Chapter> defineChapters() {
                return List.of(
                        chapter("chapter.eins", "Erstes Kapitel",
                                new Intro("es geht los", dorfplatz, GameTime.AFTERNOON), dorfplatz),
                        chapter("chapter.zwei", "Zweites Kapitel",
                                new Intro("weiter geht es", null, null), dorfplatz));
            }
        }.build();
    }

    private static Chapter chapter(String id, String name, Intro intro, Location open) {
        return new Chapter.Builder()
                .id(id)
                .name(name)
                .summary("egal")
                .intro(intro)
                .locationConditions(List.of(new LocationCondition(open, Condition.ALWAYS_TRUE)))
                .chapterFinishedCondition(Condition.ALWAYS_TRUE)
                .build();
    }
}

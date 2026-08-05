package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.GameTime;
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

    private static final UUID SCHMIEDE = UUID.fromString("2badab9d-825c-4561-815c-80afcb774ad3");
    private static final UUID WIRTSHAUS = UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f");
    private static final UUID HAUS_DES_DORFVORSTEHERS = UUID.fromString("b8d0d64b-1d64-4707-86c5-b63b0ce7d5e2");

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

    @Test
    void personsAreAtTheirDaytimeLocationAfterTheIntro() {
        // Buchenhain starts in the afternoon, so the daytime conditions must hold right away.
        Session session = startedSession();

        assertThat(session.getCurrentPersons(location(HAUS_DES_DORFVORSTEHERS))).extracting(Person::name)
                .containsExactly("Ulf Stetten");
        assertThat(session.getCurrentPersons(location(SCHMIEDE))).extracting(Person::name)
                .containsExactly("Rangolf Klingbeil");
    }
}

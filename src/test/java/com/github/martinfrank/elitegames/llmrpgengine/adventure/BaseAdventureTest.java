package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies what {@link BaseAdventure} adds over a hand-written adventure: the definitions are read
 * once and indexed, and a reference that resolves to nothing says so loudly instead of handing out
 * {@code null} for someone else to trip over later.
 */
class BaseAdventureTest {

    /** An adventure with one place in it, and a count of how often that was read. */
    private static class OneLocationAdventure extends TinyAdventure {
        private final Location dorfplatz = TinyAdventure.location("location.dorfplatz", "Dorfplatz");
        private int reads = 0;

        @Override
        protected List<Location> defineLocations() {
            reads++;
            return List.of(dorfplatz);
        }
    }

    @Test
    void aReferenceResolvesToTheDefinedInstance() {
        OneLocationAdventure adventure = new OneLocationAdventure();
        adventure.build();

        assertThat(adventure.getLocation("location.dorfplatz")).isSameAs(adventure.dorfplatz);
    }

    /**
     * The point of the index: the chapter definitions look places and figures up over and over,
     * and each of those used to rebuild the whole adventure and walk the list.
     */
    @Test
    void theDefinitionsAreReadExactlyOnce() {
        OneLocationAdventure adventure = new OneLocationAdventure();
        adventure.build();

        adventure.getLocations();
        adventure.getLocation("location.dorfplatz");
        adventure.getLocation("location.dorfplatz");
        adventure.build();

        assertThat(adventure.reads).isEqualTo(1);
    }

    @Test
    void aTypoNamesTheClosestKnownId() {
        Adventure adventure = new OneLocationAdventure().build();

        assertThatThrownBy(() -> adventure.getLocation("location.dorflatz"))
                .isInstanceOf(AdventureDefinitionException.class)
                .hasMessageContaining("unknown location 'location.dorflatz'")
                .hasMessageContaining("did you mean 'location.dorfplatz'");
    }

    /** The copy-paste of an id into a slot of another kind – worth saying outright, not guessing at. */
    @Test
    void anIdOfAnotherKindIsNamedAsSuch() {
        Adventure adventure = new OneLocationAdventure().build();

        assertThatThrownBy(() -> adventure.getPerson("location.dorfplatz"))
                .isInstanceOf(AdventureDefinitionException.class)
                .hasMessageContaining("it is a location, not a person");
    }

    @Test
    void anIdThatResemblesNothingReportsHowMuchIsDefined() {
        Adventure adventure = new OneLocationAdventure().build();

        assertThatThrownBy(() -> adventure.getLocation("location.saturnring-oberdeck"))
                .isInstanceOf(AdventureDefinitionException.class)
                .hasMessageContaining("defines 1 location ids");
    }

    @Test
    void aDuplicateIdIsRejectedWhileBuilding() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override
            protected List<Location> defineLocations() {
                return List.of(
                        TinyAdventure.location("location.dorfplatz", "Dorfplatz"),
                        TinyAdventure.location("location.dorfplatz", "Noch ein Dorfplatz"));
            }
        };

        assertThatThrownBy(adventure::build)
                .isInstanceOf(AdventureDefinitionException.class)
                .hasMessageContaining("duplicate location id 'location.dorfplatz'");
    }

    @Test
    void anUnbuiltAdventureSaysWhatIsMissing() {
        Adventure adventure = new OneLocationAdventure();

        assertThatThrownBy(adventure::getLocations)
                .isInstanceOf(AdventureDefinitionException.class)
                .hasMessageContaining("has not been built")
                .hasMessageContaining("build()");
    }

    /**
     * A definition reaching for something its own phase runs before is the only way the build
     * order can fail, and the message has to point at that rather than at a missing build().
     */
    @Test
    void aLookupAheadOfItsPhasePointsAtTheBuildOrder() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override
            protected List<Location> defineLocations() {
                // Conditions are registered long after locations.
                getCondition("condition.immer-wahr");
                return List.of();
            }
        };

        assertThatThrownBy(adventure::build)
                .isInstanceOf(AdventureDefinitionException.class)
                .hasMessageContaining("conditions are registered after whatever is asking for them");
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.InvestigateCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.KnowledgeFlag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.martinfrank.elitegames.llmrpgengine.adventure.TinyAdventure.location;
import static com.github.martinfrank.elitegames.llmrpgengine.adventure.TinyAdventure.person;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies the checks that neither the type system nor {@link BaseAdventure}'s index can make:
 * the references an adventure writes as bare ids, the ones that only make sense inside a chapter,
 * and the definitions that have no effect.
 * <p>
 * Errors are asserted through {@link BaseAdventure#build()}, because refusing to build is the
 * behaviour that matters – a broken adventure must not reach a session.
 */
class AdventureValidatorTest {

    private static final Location DORFPLATZ = location("location.dorfplatz", "Dorfplatz");
    private static final Location WIRTSHAUS = location("location.wirtshaus-zum-adler", "Wirtshaus");
    private static final Person WIRTIN = person("person.kalgeria-mondlaeufer", "Kalgeria");

    private static Chapter chapterAt(Location... open) {
        return chapter(List.of(open), List.of(), List.of(), List.of(), open[0]);
    }

    private static Chapter chapter(List<Location> open,
                                   List<PersonCondition> persons,
                                   List<DialogCondition> dialogs,
                                   List<InvestigateCondition<?>> investigations,
                                   Location start) {
        return new Chapter.Builder()
                .id("chapter.erstes-kapitel")
                .name("Erstes Kapitel")
                .summary("egal")
                .intro(new Intro("es geht los", start, GameTime.AFTERNOON))
                .locationConditions(open.stream()
                        .map(l -> new LocationCondition(l, Condition.ALWAYS_TRUE)).toList())
                .personConditions(persons)
                .dialogConditions(dialogs)
                .investigateConditions(investigations)
                .chapterFinishedCondition(Condition.ALWAYS_TRUE)
                .build();
    }

    private static ValidationResult validationOf(BaseAdventure adventure) {
        return AdventureValidator.validate(adventure.build());
    }

    @Test
    void anIdInTheWrongNamespaceIsRejected() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(location("person.dorfplatz", "Dorfplatz"));
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("'person.dorfplatz' is a location, so its id must start with 'location.'");
    }

    /** The bare-id reference nothing resolves: an unknown destination silently is no way out. */
    @Test
    void aDestinationThatIsNoLocationIsRejected() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(new Location.Builder()
                        .id("location.dorfplatz").name("Dorfplatz").description("Dorfplatz")
                        .destinations("location.hinterhof")
                        .build());
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("location 'location.dorfplatz' leads to 'location.hinterhof'")
                .hasMessageContaining("not a location of this adventure");
    }

    /** The other bare-id reference: an unknown trigger id simply never fires. */
    @Test
    void aLocationTriggerThatIsNoTriggerIsRejected() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(new Location.Builder()
                        .id("location.dorfplatz").name("Dorfplatz").description("Dorfplatz")
                        .triggers("trigger.betreten")
                        .build());
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("carries trigger 'trigger.betreten'")
                .hasMessageContaining("not a trigger of this adventure");
    }

    @Test
    void anAdventureMustNotListAGenericDialog() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Dialog> defineDialogs() {
                return List.of(Dialog.GOSSIP);
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("is a generic dialog that the engine adds to every person");
    }

    @Test
    void aChapterCannotStartWhereItDoesNotLead() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(DORFPLATZ, WIRTSHAUS);
            }
            @Override protected List<Chapter> defineChapters() {
                return List.of(chapter(List.of(DORFPLATZ), List.of(), List.of(), List.of(), WIRTSHAUS));
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("starts in 'location.wirtshaus-zum-adler'")
                .hasMessageContaining("does not open up");
    }

    @Test
    void aPersonCannotBePlacedWhereTheChapterDoesNotLead() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(DORFPLATZ, WIRTSHAUS);
            }
            @Override protected List<Person> definePersons() {
                return List.of(WIRTIN);
            }
            @Override protected List<Chapter> defineChapters() {
                return List.of(chapter(List.of(DORFPLATZ),
                        List.of(new PersonCondition(WIRTIN, WIRTSHAUS, Condition.ALWAYS_TRUE)),
                        List.of(), List.of(), DORFPLATZ));
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("places 'person.kalgeria-mondlaeufer' in 'location.wirtshaus-zum-adler'")
                .hasMessageContaining("nobody can ever meet them there");
    }

    /** The copy-paste that was actually in Buchenhain: the same dialog offered twice. */
    @Test
    void theSameDialogTwiceForOnePersonIsRejected() {
        Dialog gefahr = new Dialog(Id.of("dialog.gefahr-fuer-das-dorf"),
                "Gefahr", "die Gefahr", "es ist gefaehrlich", List.of());
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(DORFPLATZ);
            }
            @Override protected List<Person> definePersons() {
                return List.of(WIRTIN);
            }
            @Override protected List<Dialog> defineDialogs() {
                return List.of(gefahr);
            }
            @Override protected List<Chapter> defineChapters() {
                return List.of(chapter(List.of(DORFPLATZ),
                        List.of(new PersonCondition(WIRTIN, DORFPLATZ, Condition.ALWAYS_TRUE)),
                        List.of(new DialogCondition(WIRTIN, gefahr, Condition.ALWAYS_TRUE),
                                new DialogCondition(WIRTIN, gefahr, Condition.ALWAYS_TRUE)),
                        List.of(), DORFPLATZ));
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("be talked about twice");
    }

    @Test
    void anInvestigationCannotHideOnSomethingOutsideTheChapter() {
        Trigger found = new Trigger(Id.of("trigger.etwas-gefunden"), "gefunden",
                new Event.Builder().description("etwas liegt da").build());
        Investigation search = new Investigation(Id.of("investigation.umsehen"), "umsehen",
                new SkillCheck(), found);
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(DORFPLATZ, WIRTSHAUS);
            }
            @Override protected List<Trigger> defineTriggers() {
                return List.of(found);
            }
            @Override protected List<Investigation> defineInvestigations() {
                return List.of(search);
            }
            @Override protected List<Chapter> defineChapters() {
                return List.of(chapter(List.of(DORFPLATZ), List.of(), List.of(),
                        List.of(new InvestigateCondition<>(WIRTSHAUS, search, Condition.ALWAYS_TRUE)),
                        DORFPLATZ));
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("hides 'investigation.umsehen' on 'location.wirtshaus-zum-adler'")
                .hasMessageContaining("neither a place nor a person of this chapter");
    }

    /**
     * The precondition the agents' id recovery rests on. Two ids a typo apart mean a mistyped one
     * resolves to the wrong thing instead of being rejected.
     */
    @Test
    void idsThatAreTooCloseTogetherAreRejected() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(
                        location("location.dorfplatz", "Dorfplatz"),
                        location("location.dorfplotz", "Dorfplotz"));
            }
        };

        assertThatThrownBy(adventure::build)
                .hasMessageContaining("'location.dorfplatz' and 'location.dorfplotz' are only 1 character(s) apart");
    }

    @Test
    void everyErrorIsReportedAtOnce() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(new Location.Builder()
                        .id("person.dorfplatz").name("Dorfplatz").description("Dorfplatz")
                        .destinations("location.hinterhof")
                        .build());
            }
        };

        // A build that stops at the first problem makes the author pay a full round trip per typo.
        assertThatThrownBy(adventure::build)
                .hasMessageContaining("2 error(s)")
                .hasMessageContaining("must start with 'location.'")
                .hasMessageContaining("leads to 'location.hinterhof'");
    }

    @Test
    void aFlagNoConditionLooksAtIsAWarningNotAnError() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Flag<?>> defineFlags() {
                return List.of(new KnowledgeFlag(Id.of("flag.weiss-bescheid"), "weiss bescheid",
                        new Knowledge("Bescheid", "die Spieler wissen bescheid")));
            }
        };

        ValidationResult validation = validationOf(adventure);

        assertThat(validation.hasErrors()).isFalse();
        assertThat(validation.warnings())
                .anyMatch(w -> w.contains("flag.weiss-bescheid") && w.contains("not considered by any condition"))
                .anyMatch(w -> w.contains("flag.weiss-bescheid") && w.contains("raised by no trigger"));
    }

    @Test
    void anItemThatCanNeverBeHandedOutIsAWarning() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Item> defineItems() {
                return List.of(new Item(Id.of("item.silberring"), "Silberring", "ein Ring"));
            }
        };

        assertThat(validationOf(adventure).warnings())
                .anyMatch(w -> w.contains("item.silberring") && w.contains("can never reach the player"));
    }

    @Test
    void aLocationNoChapterOpensUpIsAWarning() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(DORFPLATZ, WIRTSHAUS);
            }
            @Override protected List<Chapter> defineChapters() {
                return List.of(chapterAt(DORFPLATZ));
            }
        };

        assertThat(validationOf(adventure).warnings())
                .anyMatch(w -> w.contains("location.wirtshaus-zum-adler") && w.contains("opened up by no chapter"));
    }

    @Test
    void aCleanAdventureReportsNothing() {
        BaseAdventure adventure = new TinyAdventure() {
            @Override protected List<Location> defineLocations() {
                return List.of(DORFPLATZ);
            }
            @Override protected List<Chapter> defineChapters() {
                return List.of(chapterAt(DORFPLATZ));
            }
        };

        ValidationResult validation = validationOf(adventure);

        assertThat(validation.errors()).isEmpty();
        assertThat(validation.warnings()).isEmpty();
    }
}

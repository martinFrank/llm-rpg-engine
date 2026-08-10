package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What an adventure is written against: the author fills in the {@code defineX()} methods,
 * {@link #build()} turns them into an indexed, immutable adventure.
 * <p>
 * Two things this buys over implementing {@link Adventure} directly. The definitions are read
 * exactly once, so a {@code getLocation(...)} is a map lookup instead of rebuilding every location
 * of the adventure and walking the list – which is what the chapter definitions did dozens of
 * times over. And a reference that resolves to nothing raises
 * {@link AdventureDefinitionException} instead of quietly handing out {@code null}.
 *
 * <h2>Why build() has phases</h2>
 * The definitions reference each other: a trigger raises a flag, a dialog carries triggers, a
 * chapter places persons in locations. {@link #build()} therefore fills the registries in
 * dependency order, so by the time a {@code defineX()} runs, everything it may look up is already
 * registered. That order is only possible because the references form a DAG.
 * <p>
 * The one place that would close a cycle – a location carries triggers, a trigger raises a flag,
 * a flag can be about that same location – is already broken by {@link Location} holding trigger
 * <em>ids</em> rather than {@link Trigger} objects. That is the escape hatch for any future cycle
 * too: reference by {@link Id} instead of by object, and resolve it when it is needed.
 *
 * <h2>Why build() is not the constructor</h2>
 * Calling an overridable {@code defineX()} from the constructor would run it before the
 * subclass's own fields are initialized. It happens to work for an adventure whose definitions
 * are plain literals, and breaks silently for the first one that is not.
 */
public abstract class BaseAdventure implements Adventure {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAdventure.class);

    private final Registry<Location> locations = new Registry<>("location");
    private final Registry<Person> persons = new Registry<>("person");
    private final Registry<Item> items = new Registry<>("item");
    private final Registry<Flag<?>> flags = new Registry<>("flag");
    private final Registry<Trigger> triggers = new Registry<>("trigger");
    private final Registry<Dialog> dialogs = new Registry<>("dialog");
    private final Registry<Investigation> investigations = new Registry<>("investigation");
    private final Registry<Condition> conditions = new Registry<>("condition");
    private final Registry<Chapter> chapters = new Registry<>("chapter");

    private boolean built = false;

    /** True only while {@link #build()} runs, so an early lookup can name the real reason. */
    private boolean building = false;

    protected abstract List<Location> defineLocations();
    protected abstract List<Person> definePersons();
    protected abstract List<Item> defineItems();
    protected abstract List<Flag<?>> defineFlags();
    protected abstract List<Trigger> defineTriggers();
    protected abstract List<Dialog> defineDialogs();
    protected abstract List<Investigation> defineInvestigations();
    protected abstract List<Condition> defineConditions();
    protected abstract List<Chapter> defineChapters();

    /**
     * Reads the definitions and indexes them. Idempotent, so building an already built adventure
     * is a no-op rather than a second read.
     *
     * @return this adventure, ready to be played
     */
    public final Adventure build() {
        if (built) {
            return this;
        }
        building = true;
        locations.fill(defineLocations());              // references nothing
        persons.fill(definePersons());                  // references nothing
        items.fill(defineItems());                      // references nothing
        flags.fill(defineFlags());                      // -> locations, items
        triggers.fill(defineTriggers());                // -> flags, items, locations
        dialogs.fill(defineDialogs());                  // -> triggers
        investigations.fill(defineInvestigations());    // -> triggers
        conditions.fill(defineConditions());            // -> flags
        chapters.fill(defineChapters());                // -> everything
        building = false;
        built = true;

        ValidationResult validation = AdventureValidator.validate(this);
        validation.warnings().forEach(warning -> LOGGER.warn("adventure '{}': {}",
                getMetadata().title(), warning));
        validation.throwOnError();
        return this;
    }

    @Override
    public List<Chapter> getChapters() {
        return chapters.all();
    }

    @Override
    public List<Person> getPersons() {
        return persons.all();
    }

    @Override
    public List<Item> getItems() {
        return items.all();
    }

    @Override
    public List<Dialog> getDialogs() {
        return dialogs.all();
    }

    @Override
    public List<Location> getLocations() {
        return locations.all();
    }

    @Override
    public List<Condition> getConditions() {
        return conditions.all();
    }

    @Override
    public List<Flag<?>> getFlags() {
        return flags.all();
    }

    @Override
    public List<Trigger> getTriggers() {
        return triggers.all();
    }

    @Override
    public List<Investigation> getInvestigations() {
        return investigations.all();
    }

    @Override
    public Condition getCondition(Id id) {
        return conditions.require(id);
    }

    @Override
    public Location getLocation(Id id) {
        return locations.require(id);
    }

    @Override
    public Person getPerson(Id id) {
        return persons.require(id);
    }

    @Override
    public Item getItem(Id id) {
        return items.require(id);
    }

    @Override
    public Flag<?> getFlag(Id id) {
        return flags.require(id);
    }

    @Override
    public Dialog getDialog(Id id) {
        return dialogs.require(id);
    }

    @Override
    public Trigger getTrigger(Id id) {
        return triggers.require(id);
    }

    @Override
    public Investigation getInvestigation(Id id) {
        return investigations.require(id);
    }

    /** All registries in build order, for error messages that need to look beyond their own kind. */
    private List<Registry<?>> registries() {
        return List.of(locations, persons, items, flags, triggers, dialogs, investigations,
                conditions, chapters);
    }

    /**
     * Everything of one kind, indexed by id. Also the place where a lookup that finds nothing is
     * turned into a message that says what to do about it.
     */
    private final class Registry<T extends Identifiable> {

        /** How far a known id may be from the one asked for and still be offered as a guess. */
        private static final int MAX_SUGGESTION_DISTANCE = 4;

        private final String kind;
        private final Map<Id, T> byId = new LinkedHashMap<>();
        private List<T> all = List.of();
        private boolean filled = false;

        private Registry(String kind) {
            this.kind = kind;
        }

        private void fill(List<T> entries) {
            for (T entry : entries) {
                T previous = byId.put(entry.id(), entry);
                if (previous != null) {
                    throw new AdventureDefinitionException(
                            "duplicate " + kind + " id '" + entry.id() + "' - every id must be unique");
                }
            }
            all = List.copyOf(entries);
            filled = true;
        }

        private List<T> all() {
            requireFilled(null);
            return all;
        }

        private T require(Id id) {
            requireFilled(id);
            if (id == null) {
                throw new AdventureDefinitionException("no " + kind + " id given");
            }
            T found = byId.get(id);
            if (found == null) {
                throw new AdventureDefinitionException("unknown " + kind + " '" + id + "'" + hint(id));
            }
            return found;
        }

        /**
         * Guards the two ways to ask too early. The adventure was never built at all – then
         * nothing is filled. Or a definition looked up something its own phase runs before, which
         * only happens when a new kind of reference closes a cycle in the build order.
         */
        private void requireFilled(Id wanted) {
            if (filled) {
                return;
            }
            String subject = wanted == null ? "the " + kind + "s" : kind + " '" + wanted + "'";
            if (building) {
                throw new AdventureDefinitionException("cannot resolve " + subject
                        + ": " + kind + "s are registered after whatever is asking for them"
                        + " - see the phase order in BaseAdventure.build()");
            }
            throw new AdventureDefinitionException("cannot resolve " + subject
                    + ": the adventure has not been built - call build() on it before playing it");
        }

        /**
         * Why the id did not resolve. An id that exists under another kind is named as such –
         * that is the copy-paste of a location id into a person slot, and saying so beats any
         * guess. Otherwise the closest known id is offered, which covers the typo.
         */
        private String hint(Id id) {
            for (Registry<?> other : registries()) {
                if (other != this && other.byId.containsKey(id)) {
                    return " - it is a " + other.kind + ", not a " + kind;
                }
            }
            Id nearest = null;
            int shortest = Integer.MAX_VALUE;
            for (Id candidate : byId.keySet()) {
                int distance = Levenshtein.distance(id.value(), candidate.value());
                if (distance < shortest) {
                    shortest = distance;
                    nearest = candidate;
                }
            }
            if (nearest != null && shortest <= MAX_SUGGESTION_DISTANCE) {
                return " - did you mean '" + nearest + "'?";
            }
            return " (the adventure defines " + byId.size() + " " + kind + " ids)";
        }
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.InvestigateCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.LocationCondition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.PersonCondition;
import com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks an adventure for the mistakes the type system and {@link BaseAdventure}'s index cannot
 * catch. The index already rejects a duplicate id and a reference to something that does not
 * exist; what is left is everything expressed as a plain id, everything that only makes sense in
 * the context of a chapter, and everything that is defined but has no effect.
 * <p>
 * The split matters: an <em>error</em> means the adventure is broken and must not be played, a
 * <em>warning</em> means it plays but something in it does nothing. Half-written content is
 * normal while authoring, so it must not block a run.
 */
public final class AdventureValidator {

    /**
     * How far apart the ids of an adventure have to be. The agents copy ids back verbatim and get
     * them slightly wrong; {@code TalkTaskHandler} recovers that by taking the nearest known id.
     * That recovery is only safe while no two ids are close enough to be confused, which is what
     * this rule turns from a hope into a checked property.
     */
    public static final int MIN_ID_DISTANCE = 3;

    private final Adventure adventure;
    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    private AdventureValidator(Adventure adventure) {
        this.adventure = adventure;
    }

    public static ValidationResult validate(Adventure adventure) {
        AdventureValidator validator = new AdventureValidator(adventure);
        validator.run();
        return new ValidationResult(validator.errors, validator.warnings);
    }

    private void run() {
        checkNamespaces();
        checkLocationReferences();
        checkGenericDialogsAreNotRedefined();
        checkChapters();
        checkIdsAreFarApart();

        warnAboutUnusedFlags();
        warnAboutUnusedTriggers();
        warnAboutUnusedConditions();
        warnAboutUnusedItems();
        warnAboutUnreachableLocations();
        warnAboutUnreachablePersons();
        warnAboutMissingChapters();
    }

    // ---------------------------------------------------------------- errors

    /**
     * The namespace has to name the kind of thing it belongs to, otherwise it is not the reading
     * aid it is meant to be – and an id in the wrong slot stops being visible at a glance.
     */
    private void checkNamespaces() {
        checkNamespace(adventure.getLocations(), "location");
        checkNamespace(adventure.getPersons(), "person");
        checkNamespace(adventure.getItems(), "item");
        checkNamespace(adventure.getFlags(), "flag");
        checkNamespace(adventure.getTriggers(), "trigger");
        checkNamespace(adventure.getDialogs(), "dialog");
        checkNamespace(adventure.getInvestigations(), "investigation");
        checkNamespace(adventure.getConditions(), "condition");
        checkNamespace(adventure.getChapters(), "chapter");
    }

    private void checkNamespace(Collection<? extends Identifiable> entries, String expected) {
        for (Identifiable entry : entries) {
            if (!expected.equals(entry.id().namespace())) {
                errors.add("'" + entry.id() + "' is a " + expected
                        + ", so its id must start with '" + expected + ".'");
            }
        }
    }

    /**
     * The two references an adventure writes as bare ids instead of objects. Nothing resolves them
     * through the index, so nothing has ever complained about a typo in them: an unknown
     * destination simply never shows up as a way out, and an unknown trigger id simply never fires
     * (see {@code GoToTaskHandler#handleLocationTrigger}).
     */
    private void checkLocationReferences() {
        Set<Id> knownLocations = idsOf(adventure.getLocations());
        Set<Id> knownTriggers = idsOf(adventure.getTriggers());
        for (Location location : adventure.getLocations()) {
            for (Id destination : location.destinationIds()) {
                if (!knownLocations.contains(destination)) {
                    errors.add("location '" + location.id() + "' leads to '" + destination
                            + "', which is not a location of this adventure");
                }
            }
            for (Id trigger : location.triggerIds()) {
                if (!knownTriggers.contains(trigger)) {
                    errors.add("location '" + location.id() + "' carries trigger '" + trigger
                            + "', which is not a trigger of this adventure");
                }
            }
        }
    }

    /**
     * The generic dialogs belong to the engine, which adds them to every person regardless of any
     * condition. An adventure listing one of them again would have it offered twice.
     */
    private void checkGenericDialogsAreNotRedefined() {
        Set<Id> generic = idsOf(Dialog.GENERIC);
        for (Dialog dialog : adventure.getDialogs()) {
            if (generic.contains(dialog.id())) {
                errors.add("dialog '" + dialog.id() + "' is a generic dialog that the engine adds"
                        + " to every person - an adventure must not list it");
            }
        }
    }

    private void checkChapters() {
        for (Chapter chapter : adventure.getChapters()) {
            Set<Id> here = chapter.locationConditions().stream()
                    .map(LocationCondition::location)
                    .map(Location::id)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

            Intro intro = chapter.intro();
            if (intro != null && intro.startLocation() != null
                    && !here.contains(intro.startLocation().id())) {
                errors.add("chapter '" + chapter.id() + "' starts in '" + intro.startLocation().id()
                        + "', which the chapter does not open up");
            }

            for (PersonCondition personCondition : chapter.personConditions()) {
                if (!here.contains(personCondition.location().id())) {
                    errors.add("chapter '" + chapter.id() + "' places '" + personCondition.person().id()
                            + "' in '" + personCondition.location().id()
                            + "', which the chapter does not open up - nobody can ever meet them there");
                }
            }

            Set<Id> subjects = new HashSet<>(here);
            chapter.personConditions().forEach(pc -> subjects.add(pc.person().id()));
            for (InvestigateCondition<?> investigateCondition : chapter.investigateConditions()) {
                Id subject = investigateCondition.subject().id();
                if (!subjects.contains(subject)) {
                    errors.add("chapter '" + chapter.id() + "' hides '"
                            + investigateCondition.investigation().id() + "' on '" + subject
                            + "', which is neither a place nor a person of this chapter");
                }
            }

            Set<String> pairs = new HashSet<>();
            for (DialogCondition dialogCondition : chapter.dialogConditions()) {
                String pair = dialogCondition.person().id() + " + " + dialogCondition.dialog().id();
                if (!pairs.add(pair)) {
                    errors.add("chapter '" + chapter.id() + "' lets " + pair
                            + " be talked about twice - the dialog would be offered twice");
                }
            }
        }
    }

    /**
     * See {@link #MIN_ID_DISTANCE}. Reported per pair, because the fix is renaming one of the two
     * and the author has to know which two.
     */
    private void checkIdsAreFarApart() {
        List<Id> ids = new ArrayList<>(allIds());
        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                String one = ids.get(i).value();
                String other = ids.get(j).value();
                int distance = Levenshtein.distance(one, other);
                if (distance < MIN_ID_DISTANCE) {
                    errors.add("'" + one + "' and '" + other + "' are only " + distance
                            + " character(s) apart - an agent mistyping one would resolve to the other"
                            + " (at least " + MIN_ID_DISTANCE + " required)");
                }
            }
        }
    }

    // -------------------------------------------------------------- warnings

    private void warnAboutUnusedFlags() {
        Set<Id> considered = new HashSet<>();
        for (Condition condition : allConditions()) {
            condition.consideredFlags().forEach(flag -> considered.add(flag.id()));
        }
        Set<Id> raised = new HashSet<>();
        for (Trigger trigger : adventure.getTriggers()) {
            if (trigger.event() != null && trigger.event().raisedFlags() != null) {
                trigger.event().raisedFlags().forEach(flag -> raised.add(flag.id()));
            }
        }
        for (Flag<?> flag : adventure.getFlags()) {
            if (!considered.contains(flag.id())) {
                warnings.add("flag '" + flag.id() + "' is not considered by any condition"
                        + " - raising it changes nothing");
            }
            if (!raised.contains(flag.id())) {
                warnings.add("flag '" + flag.id() + "' is raised by no trigger"
                        + " - every condition on it stays unsatisfied");
            }
        }
    }

    private void warnAboutUnusedTriggers() {
        Set<Id> used = new HashSet<>();
        adventure.getDialogs().forEach(d -> d.knowledgeTriggers().forEach(t -> used.add(t.id())));
        adventure.getInvestigations().forEach(i -> {
            if (i.trigger() != null) {
                used.add(i.trigger().id());
            }
        });
        adventure.getLocations().forEach(l -> used.addAll(l.triggerIds()));
        for (Trigger trigger : adventure.getTriggers()) {
            if (!used.contains(trigger.id())) {
                warnings.add("trigger '" + trigger.id() + "' is referenced by no dialog,"
                        + " investigation or location - it can never fire");
            }
        }
    }

    private void warnAboutUnusedConditions() {
        Set<Id> used = idsOf(allConditions());
        for (Condition condition : adventure.getConditions()) {
            if (!used.contains(condition.id())) {
                warnings.add("condition '" + condition.id() + "' is used by no chapter");
            }
        }
    }

    private void warnAboutUnusedItems() {
        Set<Id> used = new HashSet<>();
        for (Flag<?> flag : adventure.getFlags()) {
            if (flag.content() instanceof Item item) {
                used.add(item.id());
            }
        }
        for (Trigger trigger : adventure.getTriggers()) {
            Event event = trigger.event();
            if (event == null) {
                continue;
            }
            addItemIds(used, event.addedItems());
            addItemIds(used, event.removedItems());
        }
        for (Item item : adventure.getItems()) {
            if (!used.contains(item.id())) {
                warnings.add("item '" + item.id() + "' can never reach the player");
            }
        }
    }

    private void warnAboutUnreachableLocations() {
        Set<Id> open = new HashSet<>();
        adventure.getChapters().forEach(chapter -> chapter.locationConditions()
                .forEach(locationCondition -> open.add(locationCondition.location().id())));
        for (Location location : adventure.getLocations()) {
            if (!open.contains(location.id())) {
                warnings.add("location '" + location.id() + "' is opened up by no chapter");
            }
        }
    }

    /** A person nobody can meet, but who has something scripted to say, is a dead end. */
    private void warnAboutUnreachablePersons() {
        for (Chapter chapter : adventure.getChapters()) {
            Set<Id> present = new HashSet<>();
            chapter.personConditions().forEach(pc -> present.add(pc.person().id()));
            for (DialogCondition dialogCondition : chapter.dialogConditions()) {
                Id person = dialogCondition.person().id();
                if (!present.contains(person)) {
                    warnings.add("chapter '" + chapter.id() + "' gives '" + person + "' the dialog '"
                            + dialogCondition.dialog().id() + "', but places them nowhere");
                }
            }
        }
    }

    private void warnAboutMissingChapters() {
        if (adventure.getChapters().isEmpty()) {
            warnings.add("the adventure has no chapters - starting a session on it will fail");
        }
    }

    // --------------------------------------------------------------- helpers

    private static void addItemIds(Set<Id> target, List<Item> items) {
        if (items != null) {
            items.forEach(item -> target.add(item.id()));
        }
    }

    private static Set<Id> idsOf(Collection<? extends Identifiable> entries) {
        Set<Id> ids = new LinkedHashSet<>();
        entries.forEach(entry -> ids.add(entry.id()));
        return ids;
    }

    /** Every condition a chapter actually evaluates, including the engine's shared constants. */
    private Set<Condition> allConditions() {
        Set<Condition> conditions = new LinkedHashSet<>();
        for (Chapter chapter : adventure.getChapters()) {
            chapter.locationConditions().forEach(c -> conditions.add(c.condition()));
            chapter.personConditions().forEach(c -> conditions.add(c.condition()));
            chapter.dialogConditions().forEach(c -> conditions.add(c.condition()));
            chapter.investigateConditions().forEach(c -> conditions.add(c.condition()));
            conditions.add(chapter.chapterFinishedCondition());
        }
        conditions.remove(null);
        return conditions;
    }

    /**
     * Every id an agent can be asked to copy back, which is what the distance rule is about. The
     * generic dialogs belong in here even though they are not the adventure's: they are offered
     * alongside its own dialogs in the same list.
     */
    private Set<Id> allIds() {
        Set<Id> ids = new LinkedHashSet<>();
        ids.addAll(idsOf(adventure.getLocations()));
        ids.addAll(idsOf(adventure.getPersons()));
        ids.addAll(idsOf(adventure.getItems()));
        ids.addAll(idsOf(adventure.getFlags()));
        ids.addAll(idsOf(adventure.getTriggers()));
        ids.addAll(idsOf(adventure.getDialogs()));
        ids.addAll(idsOf(Dialog.GENERIC));
        ids.addAll(idsOf(adventure.getInvestigations()));
        ids.addAll(idsOf(adventure.getConditions()));
        ids.addAll(idsOf(adventure.getChapters()));
        return ids;
    }
}

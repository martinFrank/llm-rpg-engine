package com.github.martinfrank.elitegames.llmrpgengine.adventure.format;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.AdventureValidator;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;
import com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * An id made from the name the author typed – "Ulf Stetten" in the person's name field becomes
 * {@code person.ulf-stetten} without anyone spelling it out.
 * <p>
 * The two rules that make an id awkward to write by hand are both applied here rather than
 * explained: no umlauts, because the agents reproduce plain ASCII more reliably, and at least
 * {@link AdventureValidator#MIN_ID_DISTANCE} characters between any two ids, because a mistyped
 * id is recovered by nearest match and a near-collision would recover the wrong one.
 * <p>
 * A conflict is reported, not worked around. Appending a digit would produce exactly the pair of
 * ids the distance rule exists to forbid, so the honest answer is to say which existing id is in
 * the way and let the author pick a different name.
 */
public final class IdSuggester {

    private IdSuggester() {
    }

    /**
     * @param id       the proposed id, or {@code null} when the name yields nothing usable
     * @param taken    whether that id already exists
     * @param tooClose existing ids the proposal is nearer to than the distance rule allows
     */
    public record Suggestion(String id, boolean taken, List<String> tooClose) {

        public boolean usable() {
            return id != null && !taken && tooClose.isEmpty();
        }
    }

    public static Suggestion suggest(Adventure adventure, String namespace, String name) {
        String slug = slugOf(name);
        if (slug.isEmpty() || !namespace.matches("[a-z]+")) {
            return new Suggestion(null, false, List.of());
        }
        String proposed = namespace + "." + slug;

        Set<String> existing = idsOf(adventure);
        if (existing.contains(proposed)) {
            return new Suggestion(proposed, true, List.of());
        }
        List<String> tooClose = new ArrayList<>();
        for (String candidate : existing) {
            if (Levenshtein.distance(proposed, candidate) < AdventureValidator.MIN_ID_DISTANCE) {
                tooClose.add(candidate);
            }
        }
        return new Suggestion(proposed, false, List.copyOf(tooClose));
    }

    /**
     * The slug part of an id: lowercase ASCII with hyphens. German umlauts are spelled out before
     * the general accent stripping, because {@code ü} should become {@code ue} and not {@code u}.
     */
    public static String slugOf(String name) {
        if (name == null) {
            return "";
        }
        String spelled = name.toLowerCase(Locale.ROOT)
                .replace("ä", "ae").replace("ö", "oe").replace("ü", "ue")
                .replace("ß", "ss");
        String ascii = Normalizer.normalize(spelled, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return ascii.replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+", "")
                .replaceAll("-+$", "");
    }

    /**
     * Every id an adventure occupies. The generic dialogs count: the engine offers them alongside
     * the adventure's own, so an id close to {@code dialog.small-talk} is as confusable as any.
     */
    private static Set<String> idsOf(Adventure adventure) {
        Set<String> ids = new LinkedHashSet<>();
        for (ModelSchema.Section section : ModelSchema.SECTIONS) {
            for (Identifiable entry : section.read().apply(adventure)) {
                ids.add(entry.id().value());
            }
        }
        Builtins.conditions().keySet().forEach(id -> ids.add(id.value()));
        Dialog.GENERIC.forEach(dialog -> ids.add(dialog.id().value()));
        return ids;
    }

    /** Whether a string is a well-formed id at all, for the editor to say so as it is typed. */
    public static boolean isWellFormed(String id) {
        return Id.parse(id).isPresent();
    }
}

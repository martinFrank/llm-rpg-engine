package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public interface Identifiable {

    UUID id();

    /**
     * The candidate with exactly this id, or {@code null} if none has it. For ids that come from
     * an agent (and may be slightly mangled) use
     * {@link com.github.martinfrank.elitegames.llmrpgengine.util.Levenshtein#findClosest(String, List)}
     * instead.
     */
    static <T extends Identifiable> T find(UUID id, List<T> identifiables) {
        for (T identifiable : identifiables) {
            if (identifiable.id().equals(id)) {
                return identifiable;
            }
        }
        return null;
    }

}

package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public interface Identifiable {

    UUID getId();

    static Identifiable find(UUID id, List<? extends Identifiable> identifiables) {
        for (Identifiable identifiable : identifiables) {
            if (identifiable.getId().equals(id)) {
                return identifiable;
            }
        }
        return null;
    }

}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;

import java.util.UUID;

public record KnowledgeFlag (UUID id, String name, Knowledge content) implements Flag<Knowledge> {

    @Override
    public boolean isRaised() {
        return false;
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Knowledge;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

public record KnowledgeFlag (Id id, String name, Knowledge content) implements Flag<Knowledge> {

    @Override
    public boolean isRaised() {
        return false;
    }
}

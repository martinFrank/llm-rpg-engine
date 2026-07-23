package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.UUID;

public record IntegerFlag(UUID id, String name, Integer value) implements Flag<Integer> {

}

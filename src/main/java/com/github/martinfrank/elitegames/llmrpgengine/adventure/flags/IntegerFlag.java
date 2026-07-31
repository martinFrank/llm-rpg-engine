package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.UUID;

public record IntegerFlag<R>(UUID id, String name, R data, Integer value) implements Flag<Integer, R> {

}

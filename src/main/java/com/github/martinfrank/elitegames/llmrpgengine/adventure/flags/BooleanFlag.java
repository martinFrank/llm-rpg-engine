package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.UUID;

public record BooleanFlag<R> (UUID id, String name, R data, Boolean value) implements Flag<Boolean> {

}

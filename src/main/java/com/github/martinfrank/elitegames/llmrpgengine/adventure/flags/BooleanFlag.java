package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;

import java.util.UUID;

public record BooleanFlag (UUID id, String name, Boolean value) implements Flag<Boolean> {

}

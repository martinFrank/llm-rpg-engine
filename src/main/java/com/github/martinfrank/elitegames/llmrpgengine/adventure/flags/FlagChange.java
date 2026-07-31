package com.github.martinfrank.elitegames.llmrpgengine.adventure.flags;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Flag;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Identifiable;

import java.util.UUID;

public record FlagChange<R,V> (UUID id, Flag<R,V> flag, R newValue) implements Identifiable {
}

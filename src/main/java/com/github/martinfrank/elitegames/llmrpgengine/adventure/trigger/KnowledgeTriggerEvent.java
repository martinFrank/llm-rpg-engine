package com.github.martinfrank.elitegames.llmrpgengine.adventure.trigger;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.TriggeredEvent;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.List;
import java.util.UUID;

public record KnowledgeTriggerEvent<R> (UUID id, String name, String description, Location newLocation, List<FlagChange<R>> flagChanges) implements TriggeredEvent<R> {
}

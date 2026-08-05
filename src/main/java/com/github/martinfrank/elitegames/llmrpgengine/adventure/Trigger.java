package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.List;
import java.util.UUID;

public record Trigger(UUID id, String trigger, Event event) implements Identifiable{

}

package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.FlagChange;

import java.util.List;
import java.util.UUID;

public record KnowledgeTrigger(UUID id, String trigger, Knowledge knowledge, List<FlagChange<?>> flagChanges) implements Identifiable{

}

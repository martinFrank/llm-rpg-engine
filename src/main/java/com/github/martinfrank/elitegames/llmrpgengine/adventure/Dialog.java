package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.trigger.KnowledgeTrigger;

import java.util.List;
import java.util.UUID;

public record Dialog(UUID id, String topic, String summary, boolean isCommonKnowledge, String context, List<Trigger<?>> knowledgeTriggers) implements Identifiable {

}

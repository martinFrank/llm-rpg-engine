package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public record Dialog(UUID id, String topic, String summary, String context, List<KnowledgeTrigger> knowledgeTriggers) implements Identifiable {

}

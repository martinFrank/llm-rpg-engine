package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.UUID;

public record Trigger(UUID id, String trigger, Event event) implements Identifiable{

}

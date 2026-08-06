package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.UUID;

public record Item(UUID id, String name, String description) implements Identifiable {
}

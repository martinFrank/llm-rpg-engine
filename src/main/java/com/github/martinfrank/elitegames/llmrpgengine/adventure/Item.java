package com.github.martinfrank.elitegames.llmrpgengine.adventure;


public record Item(Id id, String name, String description) implements Identifiable {
}

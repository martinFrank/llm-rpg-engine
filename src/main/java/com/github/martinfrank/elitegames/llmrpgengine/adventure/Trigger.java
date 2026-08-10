package com.github.martinfrank.elitegames.llmrpgengine.adventure;


public record Trigger(Id id, String trigger, Event event) implements Identifiable{

}

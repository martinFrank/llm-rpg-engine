package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.UUID;

//könnte das nicht auch ein Flag sein? ChatGpt sagt nein, weil die daten damit besser strukturiert sind - siehe auch Flag.java
public record Knowledge (UUID id, String name, String knowledge) implements Identifiable{

}

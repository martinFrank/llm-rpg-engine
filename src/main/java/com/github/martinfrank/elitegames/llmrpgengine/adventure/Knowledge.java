package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.UUID;

public record Knowledge (UUID id, String knowledge) implements Identifiable{

}

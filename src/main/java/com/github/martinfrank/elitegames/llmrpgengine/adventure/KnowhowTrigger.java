package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.UUID;

public record KnowhowTrigger(UUID id, String trigger, Knowledge knowledge) implements Identifiable{

}

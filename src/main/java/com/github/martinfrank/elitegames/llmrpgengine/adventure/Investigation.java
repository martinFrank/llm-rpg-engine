package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.UUID;

public record Investigation(UUID id, String name, SkillCheck check, Trigger trigger) implements Identifiable {
}

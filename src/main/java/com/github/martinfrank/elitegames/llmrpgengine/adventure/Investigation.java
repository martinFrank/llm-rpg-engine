package com.github.martinfrank.elitegames.llmrpgengine.adventure;


public record Investigation(Id id, String name, SkillCheck check, Trigger trigger) implements Identifiable {
}

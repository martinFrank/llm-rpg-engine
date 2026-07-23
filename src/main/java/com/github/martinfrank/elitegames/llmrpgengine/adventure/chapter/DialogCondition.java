package com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Dialog;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;

public record DialogCondition(Person person, Dialog dialog, Condition<?> condition) {
}

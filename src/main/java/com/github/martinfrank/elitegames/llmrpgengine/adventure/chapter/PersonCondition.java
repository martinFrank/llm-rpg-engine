package com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Person;

public record PersonCondition(Person who, Location where, Condition condition) {
}

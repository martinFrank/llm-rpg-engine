package com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Location;

public record LocationCondition(Location location, Condition<?> condition) {
}

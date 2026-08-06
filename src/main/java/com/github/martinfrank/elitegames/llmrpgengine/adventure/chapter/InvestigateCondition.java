package com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Condition;

public record InvestigateCondition<R>(R subject, Condition condition) {
}

package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

public record PersonTimetable (Person who, Location where, List<GameTime> when) {
}

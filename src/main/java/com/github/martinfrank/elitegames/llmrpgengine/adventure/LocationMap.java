package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public class LocationMap {

    private record Node(UUID from, List<UUID> to){}



}

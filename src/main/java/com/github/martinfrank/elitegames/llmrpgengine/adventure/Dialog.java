package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;
import java.util.UUID;

public class Dialog implements Identifiable {

    public final UUID id;
    public final String topic;
    public final String summary;
    public final String context;
    public final List<KnowhowTrigger> knowhowTriggers;

    public Dialog(UUID id, String topic, String summary, String context, List<KnowhowTrigger> knowhowTriggers) {
        this.id = id;
        this.topic = topic;
        this.summary = summary;
        this.context = context;
        this.knowhowTriggers = knowhowTriggers;
    }

    @Override
    public UUID id() {
        return id;
    }
}

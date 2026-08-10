package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import java.util.List;

public record Dialog(Id id, String topic, String summary, String context, List<Trigger> knowledgeTriggers) implements Identifiable {

    public static final Dialog GOSSIP = new Dialog(
        Id.of("dialog.small-talk"),
            "Small talk",
            "Belangslose Themen",
            """
                    Dieser Dialog wird verwendet, wenn sonst kein besonderes Thema im Vordergrund
                    steht. Hier kann man einfach die Gesprächshistorie verwenden, um den Dialog
                    lebendig zu erhalten.
                    """,
            List.of()
    );

    /**
     * Dialogs that belong to no adventure and to no chapter: every person can always talk about
     * them, so they are added to the available dialogs regardless of any condition (see
     * {@code Session#getAvailableDialogs}). An adventure must not list them in its own dialogs.
     */
    public static final List<Dialog> GENERIC = List.of(GOSSIP);

}

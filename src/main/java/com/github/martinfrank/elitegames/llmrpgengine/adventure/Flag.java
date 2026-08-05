package com.github.martinfrank.elitegames.llmrpgengine.adventure;

public interface Flag<R> extends Identifiable{

    String name();
    R content();
    boolean isRaised();


    /*
    Also nicht:
    - Flags
    - Knowledge
    - Visited
    - Quest

    sondern
    - Facts

    mit Typen.

    Beispielsweise
    Fact
    - type = KNOWLEDGE
    - id = dragon_location

    Fact
    - type = VISITED
    - id = castle

    Fact
    - type = QUEST
    - id = dragon
    - state = started
     */
}

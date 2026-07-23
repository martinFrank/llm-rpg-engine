package com.github.martinfrank.elitegames.llmrpgengine.session;

public record TalkEntry(String actor, String statement)  {

    public String toString() {
        return actor + ": " + statement;
    }
}

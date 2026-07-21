package com.github.martinfrank.elitegames.llmrpgengine.session;

public record ChatEntry(String actor, String statement){

    @Override
    public String toString() {
        return actor + ": " + statement;
    }
}

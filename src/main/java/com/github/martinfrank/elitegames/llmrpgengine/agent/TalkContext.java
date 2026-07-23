package com.github.martinfrank.elitegames.llmrpgengine.agent;

public record TalkContext(String talkTo, String location, String primaryDialog, String triggers, String talkHistory, String chatHistory) {

}

package com.github.martinfrank.elitegames.llmrpgengine.session;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {

    private static final String NARRATOR = "Narrator";
    private static final String PLAYER = "Player";

    private final List<ChatEntry> chatEntries = new ArrayList<>();

    public void narrator(String statement) {
        chatEntries.add(new ChatEntry(NARRATOR, statement));
    }
    public void player(String statement) {
        chatEntries.add(new ChatEntry(PLAYER, statement));
    }
}

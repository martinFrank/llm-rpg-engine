package com.github.martinfrank.elitegames.llmrpgengine.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {

    private static final String NARRATOR = "Narrator";
    private static final String PLAYER = "Player";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatHistory.class);

    private final List<ChatEntry> chatEntries = new ArrayList<>();

    public void narrator(String statement) {
        add(NARRATOR, statement);
    }

    public void player(String statement) {
        add(PLAYER, statement);
    }

    private void add(String actor, String statement) {
        String normalized = StringNormalizer.normalize(statement);
        LOGGER.debug("{}: {}", actor, normalized);
        chatEntries.add(new ChatEntry(actor, normalized));
    }


}

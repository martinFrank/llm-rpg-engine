package com.github.martinfrank.elitegames.llmrpgengine.session;

import java.util.*;

public class TalkHistory {

    private final Map<UUID, List<TalkEntry>> talks = new HashMap<>();


    private static final String NPC = "Npc";
    private static final String PLAYER = "Player";
//    private static final Logger LOGGER = LoggerFactory.getLogger(TalkHistory.class);

    public void npc(UUID person, String statement) {
        add(person, NPC, statement);
    }

    public void player(UUID person, String statement) {
        add(person, PLAYER, statement);
    }

    private void add(UUID person, String actor, String statement) {
        String normalized = StringNormalizer.normalize(statement);
        TalkEntry entry = new TalkEntry(actor, normalized);
        talks.computeIfAbsent(person, _ -> new ArrayList<>()).add(entry);
    }

    public List<TalkEntry> getTalk(UUID uuid) {
        return talks.computeIfAbsent(uuid, _ -> new ArrayList<>());
    }

    public List<TalkEntry> getTalk(UUID uuid, int length) {
        List<TalkEntry> talk = getTalk(uuid);
        int from = Math.max(0, talk.size()-length);
        int to = talk.size();
        return talk.subList(from, to);
    }
}

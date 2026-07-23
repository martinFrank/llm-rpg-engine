package com.github.martinfrank.elitegames.llmrpgengine.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TalkHistoryTest {

    @Test
    void getTalkHistory() {
        TalkHistory t = new TalkHistory();
        UUID uuid = UUID.randomUUID();

        t.npc(uuid, "test1");
        t.player(uuid, "test2");
        t.npc(uuid, "test3");
        t.player(uuid, "test4");
        t.npc(uuid, "test5");
        t.player(uuid, "test6");

        List<TalkEntry> talkHistory = t.getTalk(uuid);
        Assertions.assertEquals(6, talkHistory.size());
        System.out.println(talkHistory);

        List<TalkEntry> talkHistory2 = t.getTalk(UUID.randomUUID());
        Assertions.assertEquals(0, talkHistory2.size());

        List<TalkEntry> talkHistory3 = t.getTalk(uuid, 5);
//        Assertions.assertEquals(5, talkHistory3.size());
        System.out.println(talkHistory3);
    }

}
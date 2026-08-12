package com.github.martinfrank.elitegames.llmrpgengine.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Id;

import static org.junit.jupiter.api.Assertions.*;

class TalkHistoryTest {

    @Test
    void getTalkHistory() {
        TalkHistory t = new TalkHistory();
        Id person = Id.of("person.gespraechspartner");

        t.npc(person, "test1");
        t.player(person, "test2");
        t.npc(person, "test3");
        t.player(person, "test4");
        t.npc(person, "test5");
        t.player(person, "test6");

        List<TalkEntry> talkHistory = t.getTalk(person);
        Assertions.assertEquals(6, talkHistory.size());
        System.out.println(talkHistory);

        List<TalkEntry> talkHistory2 = t.getTalk(Id.of("person.jemand-anderes"));
        Assertions.assertEquals(0, talkHistory2.size());

        List<TalkEntry> talkHistory3 = t.getTalk(person, 5);
//        Assertions.assertEquals(5, talkHistory3.size());
        System.out.println(talkHistory3);
    }

}
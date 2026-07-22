package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.flags.BaseFlag;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

public class AdventureTest {

    @Test
    void testPersonCondition(){
        //given
        Adventure buchenhain = new Buchenhain();
        Session session = new Session(buchenhain, new Player("testeee"));
        session.start();
        Location gasthaus = buchenhain.getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"));

        Person ulf = buchenhain.getPerson(UUID.fromString("3037dd8d-62d6-42b3-88b0-800fb0e3ccd4"));
        Person rangolf = buchenhain.getPerson(UUID.fromString("dcd181fb-3bc9-4941-92d4-4edc3aa68636"));

        //when
        session.setFlag(BaseFlag.GAME_TIME_FLAG.getId(), GameTime.IN_THE_EVENING);


        //then
        List<Person> persons = session.getCurrentPersons(gasthaus);
        Assertions.assertEquals(2, persons.size());
        Assertions.assertTrue(persons.contains(ulf));
        Assertions.assertTrue(persons.contains(rangolf));
    }

    @Test
    void testPersonCondition2(){
        //given
        Adventure buchenhain = new Buchenhain();
        Session session = new Session(buchenhain, new Player("testeee"));
        session.start();
        Location gasthaus = buchenhain.getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"));

        //when
        session.setFlag(BaseFlag.GAME_TIME_FLAG.getId(), GameTime.AFTERNOON);

        //then
        List<Person> persons = session.getCurrentPersons(gasthaus);
        Assertions.assertTrue(persons.isEmpty());
    }
}

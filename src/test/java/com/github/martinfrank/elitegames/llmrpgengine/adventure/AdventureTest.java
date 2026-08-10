package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import io.netty.util.internal.SuppressJava8Requirement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AdventureTest {

    @Test
    void testPersonCondition(){
        //given
        Adventure buchenhain = new Buchenhain().build();
        Session session = new Session(buchenhain, new Player("testeee"));
        session.start();
        Location gasthaus = buchenhain.getLocation(Id.of("location.wirtshaus-zum-adler"));

        Person ulf = buchenhain.getPerson(Id.of("person.ulf-stetten"));
        Person rangolf = buchenhain.getPerson(Id.of("person.rangolf-klingbeil"));
        Person kalgeria = buchenhain.getPerson(Id.of("person.kalgeria-mondlaeufer"));

        //when
//        session.setFlag(Flag.GAME_TIME_FLAG.id(), GameTime.IN_THE_EVENING);
        session.setCurrentTime(GameTime.IN_THE_EVENING);

        //then
        List<Person> persons = session.getCurrentPersons(gasthaus);
        Assertions.assertEquals(3, persons.size());
        Assertions.assertTrue(persons.contains(ulf));
        Assertions.assertTrue(persons.contains(rangolf));
        Assertions.assertTrue(persons.contains(kalgeria));
    }



}

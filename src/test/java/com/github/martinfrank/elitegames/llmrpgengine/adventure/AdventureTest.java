package com.github.martinfrank.elitegames.llmrpgengine.adventure;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.chapter.DialogCondition;
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
        Person kalgeria = buchenhain.getPerson(UUID.fromString("4bdd45a1-33d0-4ea4-91af-86a53e53dc61"));

        //when
        session.setFlag(Flag.GAME_TIME_FLAG.id(), GameTime.IN_THE_EVENING);

        //then
        List<Person> persons = session.getCurrentPersons(gasthaus);
        Assertions.assertEquals(3, persons.size());
        Assertions.assertTrue(persons.contains(ulf));
        Assertions.assertTrue(persons.contains(rangolf));
        Assertions.assertTrue(persons.contains(kalgeria));
    }

    @Test
    void testDialogCondition(){
        //given
        Adventure buchenhain = new Buchenhain();
        Session session = new Session(buchenhain, new Player("testeee"));
        session.start();
        Location gasthaus = buchenhain.getLocation(UUID.fromString("603696b5-e1be-4f85-a0e1-1209147b8a3f"));
        session.setFlag(Flag.GAME_TIME_FLAG.id(), GameTime.IN_THE_EVENING);
        session.setCurrentLocation(gasthaus);

        //then
        List<Person> persons = session.getCurrentPersons(gasthaus);
        Assertions.assertEquals(3, persons.size());

        List<Dialog> commonKnowledge = buchenhain.getDialogs().stream().filter(Dialog::isCommonKnowledge).toList();
        Assertions.assertEquals(1, commonKnowledge.size());

        for (Person person: persons){
            for(DialogCondition dialogCondition: session.getCurrentChapter().dialogConditions()){
                if (dialogCondition.person().id().equals(person.id())){
                    List requiredFlags = dialogCondition.condition().consideredFlags();
                    if (dialogCondition.condition().evaluate(requiredFlags)){
                        System.out.println(person.name()+" has dialog "+dialogCondition.dialog().topic());
                    }
                }
            }
        }


    }

}

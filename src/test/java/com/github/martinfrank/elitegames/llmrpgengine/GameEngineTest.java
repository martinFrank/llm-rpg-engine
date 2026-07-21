package com.github.martinfrank.elitegames.llmrpgengine;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Buchenhain;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Intro;
import com.github.martinfrank.elitegames.llmrpgengine.engine.GameEngine;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;
import org.junit.jupiter.api.Test;

class GameEngineTest {

    @Test
    void testEngine(){

        Adventure adventure = new Buchenhain();
        Player player = new Player("Thorsten");

        GameEngine engine = new GameEngine();
        Session session = new Session(adventure, player);
        String userInput = null;
        session.start();
        do{
            //userInput = IO.readln();
            userInput = "beschreibe mir den marktplatz";


            userInput = "exit";
        }while(!"exit".equalsIgnoreCase(userInput));
    }

}

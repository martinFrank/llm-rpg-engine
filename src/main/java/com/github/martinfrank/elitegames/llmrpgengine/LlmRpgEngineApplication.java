package com.github.martinfrank.elitegames.llmrpgengine;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import com.github.martinfrank.elitegames.llmrpgengine.user.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LlmRpgEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmRpgEngineApplication.class, args);
    }

}

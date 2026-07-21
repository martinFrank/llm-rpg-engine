package com.github.martinfrank.elitegames.llmrpgengine.engine;

import com.github.martinfrank.elitegames.llmrpgengine.agent.NarratorAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictAgent;
import com.github.martinfrank.elitegames.llmrpgengine.agent.VerdictContext;
import com.github.martinfrank.elitegames.llmrpgengine.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameEngine {

    @Autowired
    private VerdictAgent verdictAgent;

    @Autowired
    private NarratorAgent narratorAgent;

    public void handleUserInput(String userInput, Session session) {
        VerdictContext verdictContext = VerdictContext.generate(session);
//        Verdict verdict = verdictAgent.evaluate(verdictContext, userInput);
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.session;

import com.github.martinfrank.elitegames.llmrpgengine.adventure.Adventure;
import com.github.martinfrank.elitegames.llmrpgengine.adventure.Intro;
import com.github.martinfrank.elitegames.llmrpgengine.user.Player;

public class Session {

    private final Adventure adventure;
    private final Player player;
    private final ChatHistory chatHistory = new ChatHistory();

    public Session(Adventure adventure, Player player) {
        this.adventure = adventure;
        this.player = player;
    }

    public void start() {
        chatHistory.narrator(adventure.getIntro().title());
        chatHistory.narrator(adventure.getIntro().author());
        chatHistory.narrator(adventure.getIntro().intro());
    }
}

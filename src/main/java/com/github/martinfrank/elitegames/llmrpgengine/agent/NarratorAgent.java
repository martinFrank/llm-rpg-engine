package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class NarratorAgent {

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public NarratorAgent(@Qualifier("narratorChatClient") ChatClient chatClient,
                         @Value("classpath:prompts/narrator-system.st") Resource systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    public String narrate(NarratorContext context) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(u -> u
                        .text("""
                                SCENARIO:
                                {scenario}

                                LOCATION: {location}

                                RECENT STORY:
                                {transcript}

                                THE PLAYER ATTEMPTS:
                                {action}

                                WHAT MECHANICALLY HAPPENS (from the GameMaster, dramatize this faithfully):
                                {outcome}
                                HP change: {hpDelta}
                                Items gained: {itemsGained}
                                Items lost: {itemsLost}
                                Game over: {gameOver}
                                """)
//                        .param("scenario", session.getScenario())
//                        .param("where", session.getWorldState().getLocation())
//                        .param("transcript", recentTranscript)
//                        .param("action", playerAction)
//                        .param("outcome", verdict.outcome())
//                        .param("hpDelta", verdict.hpDelta())
//                        .param("itemsGained", String.join(", ", verdict.itemsGained()))
//                        .param("itemsLost", String.join(", ", verdict.itemsLost()))
//                        .param("gameOver", verdict.gameOver())
                )
                .call()
                .content();
    }
}

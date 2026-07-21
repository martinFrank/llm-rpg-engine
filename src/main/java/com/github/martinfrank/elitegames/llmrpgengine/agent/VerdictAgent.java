package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * The rules engine of the game. Given the current world state, the scenario and
 * the player's action, it decides — as structured data — what mechanically
 * happens. It deliberately does NOT write prose; that is the Narrator's job.
 */
@Component
public class VerdictAgent {

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public VerdictAgent(@Qualifier("verdictChatClient") ChatClient chatClient,
                        @Value("classpath:prompts/verdict-system.st") Resource systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    public Verdict evaluate(VerdictContext context, String userInput) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(u -> u
                        .text("""
                                SCENARIO:
                                {scenario}

                                CURRENT WORLD STATE:
                                Location: {location}
                                Player: {playerName} ({playerClass}), Level {level}, HP {hp}/{maxHp}
                                Inventory: {inventory}
                                Quest log: {quests}

                                RECENT STORY:
                                {transcript}

                                PLAYER ACTION:
                                {action}
                                """)
//                        .param("scenario", session.getScenario())
//                        .param("where", session.getWorldState().getLocation())
//                        .param("playerName", session.getWorldState().getPlayer().getName())
//                        .param("playerClass", session.getWorldState().getPlayer().getCharacterClass())
//                        .param("level", session.getWorldState().getPlayer().getLevel())
//                        .param("hp", session.getWorldState().getPlayer().getHp())
//                        .param("maxHp", session.getWorldState().getPlayer().getMaxHp())
//                        .param("inventory", String.join(", ", session.getWorldState().getPlayer().getInventory()))
//                        .param("quests", String.join(", ", session.getWorldState().getQuestLog()))
//                        .param("transcript", recentTranscript)
//                        .param("action", playerAction)
                )
                .call()
                .entity(Verdict.class);
    }
}

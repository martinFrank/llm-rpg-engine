package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Turns a {@link NarratorContext} into immersive prose that the player reads.
 * <p>
 * The agent is deliberately generic: it does not know <em>which</em> task it is
 * narrating. The {@link NarratorContext#purpose()} field describes what the player
 * is doing (e.g. investigating a location), so the same {@link #narrate(NarratorContext)}
 * call serves every task type. To support a new kind of narration – investigating a
 * person, moving to a location, etc. – add a factory method to {@link NarratorContext}
 * instead of changing this agent.
 */
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
                                AUFGABE:
                                {purpose}

                                ORT:
                                {location}

                                ANWESENDE PERSONEN:
                                {persons}

                                TAGESZEIT:
                                {time}

                                BESONDERE DETAILS:
                                {interestingDetails}

                                BISHERIGER VERLAUF:
                                {conversationHistory}
                                """)
                        .param("purpose", orEmpty(context.purpose()))
                        .param("location", orEmpty(context.location()))
                        .param("persons", orEmpty(context.persons()))
                        .param("time", orEmpty(context.time()))
                        .param("interestingDetails", orEmpty(context.interestingDetails()))
                        .param("conversationHistory", orEmpty(context.conversationHistory()))
                )
                .call()
                .content();
    }

    private static String orEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}

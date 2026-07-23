package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Speaks as the non-player character the player addressed: turns a {@link TalkContext}
 * into a spoken, in-character reply that the player reads.
 * <p>
 * When a scripted dialog is present ({@link TalkContext#primaryDialog()}), the agent
 * follows its instructions on <em>what</em> to say; otherwise it makes small talk
 * (gossip). Either way the reply stays in character, guided by the short person
 * description carried in {@link TalkContext#talkTo()}.
 */
@Component
public class TalkAgent {

    private final ChatClient chatClient;
    private final Resource systemPrompt;

    public TalkAgent(@Qualifier("talkChatClient") ChatClient chatClient,
                     @Value("classpath:prompts/talk-system.st") Resource systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    public String talk(TalkContext context) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(u -> u
                        .text("""
                                GESPRÄCHSPARTNER (die Person, die du verkörperst):
                                {talkTo}

                                ORT:
                                {location}

                                MASSGEBLICHER DIALOG (Anweisungen, was gesagt werden soll):
                                {primaryDialog}

                                MÖGLICHE TRIGGER-THEMEN:
                                {triggers}

                                BISHERIGES GESPRÄCH MIT DIESER PERSON:
                                {talkHistory}

                                BISHERIGER SPIELVERLAUF:
                                {chatHistory}

                                DER SPIELER SAGT ZU DIR:
                                {statement}
                                """)
                        .param("talkTo", orEmpty(context.talkTo()))
                        .param("location", orEmpty(context.location()))
                        .param("primaryDialog", orEmpty(context.primaryDialog()))
                        .param("triggers", orEmpty(context.triggers()))
                        .param("talkHistory", orEmpty(context.talkHistory()))
                        .param("chatHistory", orEmpty(context.chatHistory()))
                        .param("statement", orEmpty(context.statement()))
                )
                .call()
                .content();
    }

    private static String orEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}

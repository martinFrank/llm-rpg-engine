package com.github.martinfrank.elitegames.llmrpgengine.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Figures out what the player's input actually means and maps it onto one of the
 * scripted {@link TaskType}s. It produces structured data ({@link Verdict}) only;
 * the resulting changes to the session are applied by the engine, and prose is the
 * Narrator's job.
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
                                KAPITEL:
                                {chapter}

                                AKTUELLER ORT:
                                {location}

                                ANWESENDE PERSONEN:
                                {persons}

                                GEGENSTÄNDE:
                                {items}

                                VERFÜGBARE ORTE (Name, id und Beschreibung):
                                {availableLocations}

                                VERFÜGBARE PERSONEN (Name, id und Beschreibung):
                                {availablePersons}

                                MÖGLICHE GESPRÄCHSTHEMEN (pro anwesender Person, mit Dialog-ID):
                                {dialogTopics}

                                BISHERIGER VERLAUF:
                                {chatHistory}

                                EINGABE DES SPIELERS:
                                {input}
                                """)
                        .param("chapter", orEmpty(context.chapterSummary()))
                        .param("location", orEmpty(context.location()))
                        .param("persons", orEmpty(context.persons()))
                        .param("items", orEmpty(context.items()))
                        .param("availableLocations", orEmpty(context.availableLocations()))
                        .param("availablePersons", orEmpty(context.availablePersons()))
                        .param("dialogTopics", orEmpty(context.dialogTopics()))
                        .param("chatHistory", orEmpty(context.chatHistory()))
                        .param("input", orEmpty(userInput))
                )
                .call()
                .entity(Verdict.class);
    }

    private static String orEmpty(String value) {
        return Objects.requireNonNullElse(value, "");
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    ChatClient verdictChatClient(ChatClient.Builder builder,
                                 @Value("${rpg.verdict.model}") String model,
                                 @Value("${rpg.verdict.temperature}") Double temperature) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(temperature))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean
    ChatClient narratorChatClient(ChatClient.Builder builder,
                                  @Value("${rpg.narrator.model}") String model,
                                  @Value("${rpg.narrator.temperature}") Double temperature) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(temperature))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}

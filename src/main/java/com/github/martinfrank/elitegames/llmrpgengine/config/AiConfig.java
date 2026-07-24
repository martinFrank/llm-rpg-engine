package com.github.martinfrank.elitegames.llmrpgengine.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /** Keep the model loaded in (V)RAM between requests so no reload latency is paid per turn. */
    private static final String KEEP_ALIVE = "30m";

    /** Context window large enough to hold the full prompt (verdict prompts reach ~4k tokens). */
    private static final int NUM_CTX = 8192;

    @Bean
    ChatClient verdictChatClient(ChatClient.Builder builder,
                                 @Value("${rpg.verdict.model}") String model,
                                 @Value("${rpg.verdict.temperature}") Double temperature) {
        return build(builder, model, temperature);
    }

    @Bean
    ChatClient narratorChatClient(ChatClient.Builder builder,
                                  @Value("${rpg.narrator.model}") String model,
                                  @Value("${rpg.narrator.temperature}") Double temperature) {
        return build(builder, model, temperature);
    }

    @Bean
    ChatClient talkChatClient(ChatClient.Builder builder,
                              @Value("${rpg.talk.model}") String model,
                              @Value("${rpg.talk.temperature}") Double temperature) {
        return build(builder, model, temperature);
    }

    private static ChatClient build(ChatClient.Builder builder, String model, Double temperature) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .keepAlive(KEEP_ALIVE)
                        .numCtx(NUM_CTX))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}

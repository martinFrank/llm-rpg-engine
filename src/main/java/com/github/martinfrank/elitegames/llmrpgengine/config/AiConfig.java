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

    @Bean
    ChatClient verdictChatClient(ChatClient.Builder builder,
                                 @Value("${rpg.verdict.model}") String model,
                                 @Value("${rpg.verdict.temperature}") Double temperature,
                                 @Value("${rpg.num-ctx}") Integer numCtx,
                                 @Value("${rpg.verdict.num-predict}") Integer numPredict) {
        return build(builder, model, temperature, numCtx, numPredict);
    }

    @Bean
    ChatClient narratorChatClient(ChatClient.Builder builder,
                                  @Value("${rpg.narrator.model}") String model,
                                  @Value("${rpg.narrator.temperature}") Double temperature,
                                  @Value("${rpg.num-ctx}") Integer numCtx,
                                  @Value("${rpg.narrator.num-predict}") Integer numPredict) {
        return build(builder, model, temperature, numCtx, numPredict);
    }

    @Bean
    ChatClient talkChatClient(ChatClient.Builder builder,
                              @Value("${rpg.talk.model}") String model,
                              @Value("${rpg.talk.temperature}") Double temperature,
                              @Value("${rpg.num-ctx}") Integer numCtx,
                              @Value("${rpg.talk.num-predict}") Integer numPredict) {
        return build(builder, model, temperature, numCtx, numPredict);
    }

    /**
     * @param numCtx     the context window, covering prompt <em>and</em> generated response. Too
     *                   small a window makes the model run out of room mid-answer, which for the
     *                   agents that return structured data means unparseable, truncated JSON.
     *                   Shared by all agents on purpose, see {@code rpg.num-ctx}.
     * @param numPredict upper bound on generated tokens. Set generously above what a well-behaved
     *                   answer needs: it is not there to shape the answer but to stop a model that
     *                   rambles from eating the whole context window (and the wall-clock with it).
     */
    private static ChatClient build(ChatClient.Builder builder, String model, Double temperature,
                                    Integer numCtx, Integer numPredict) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .keepAlive(KEEP_ALIVE)
                        .numCtx(numCtx)
                        .numPredict(numPredict))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}

package com.github.martinfrank.elitegames.llmrpgengine.config;

import com.github.martinfrank.elitegames.llmrpgengine.agent.TalkResponse;
import com.github.martinfrank.elitegames.llmrpgengine.agent.Verdict;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.converter.BeanOutputConverter;
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
        return build(builder, model, temperature, numCtx, numPredict, schemaOf(Verdict.class));
    }

    @Bean
    ChatClient narratorChatClient(ChatClient.Builder builder,
                                  @Value("${rpg.narrator.model}") String model,
                                  @Value("${rpg.narrator.temperature}") Double temperature,
                                  @Value("${rpg.num-ctx}") Integer numCtx,
                                  @Value("${rpg.narrator.num-predict}") Integer numPredict) {
        // No schema: the Narrator answers in prose, and constraining it to JSON would be nonsense.
        return build(builder, model, temperature, numCtx, numPredict, null);
    }

    @Bean
    ChatClient talkChatClient(ChatClient.Builder builder,
                              @Value("${rpg.talk.model}") String model,
                              @Value("${rpg.talk.temperature}") Double temperature,
                              @Value("${rpg.num-ctx}") Integer numCtx,
                              @Value("${rpg.talk.num-predict}") Integer numPredict) {
        return build(builder, model, temperature, numCtx, numPredict, schemaOf(TalkResponse.class));
    }

    /**
     * The JSON schema of what an agent has to return, in the form Ollama takes as its
     * {@code format}.
     * <p>
     * Asking for structured data in the prompt alone is not enough. A local model reliably produces
     * <em>roughly</em> the right thing and every so often something that cannot be read at all: an
     * object with its braces missing ({@code "reply": "..."}), the schema echoed instead of an
     * instance, prose wrapped around the JSON. Handed the schema, Ollama constrains decoding to it,
     * so those shapes cannot be generated in the first place – which beats every attempt to repair
     * them afterwards.
     * <p>
     * It is the same schema {@code ChatClient.entity(...)} puts into the prompt (both come from
     * {@link BeanOutputConverter}), so prompt and grammar cannot drift apart.
     * <p>
     * What this does <em>not</em> cover: a reply cut off by {@code numPredict} or a full context
     * window is still unreadable, and a field can still hold a made-up id. Both stay the callers'
     * business – see {@code TalkTaskHandler} and the engine's guardrails.
     */
    private static String schemaOf(Class<?> type) {
        return new BeanOutputConverter<>(type).getJsonSchema();
    }

    /**
     * @param numCtx       the context window, covering prompt <em>and</em> generated response. Too
     *                     small a window makes the model run out of room mid-answer, which for the
     *                     agents that return structured data means unparseable, truncated JSON.
     *                     Shared by all agents on purpose, see {@code rpg.num-ctx}.
     * @param numPredict   upper bound on generated tokens. Set generously above what a well-behaved
     *                     answer needs: it is not there to shape the answer but to stop a model that
     *                     rambles from eating the whole context window (and the wall-clock with it).
     * @param outputSchema the schema the answer is constrained to (see {@link #schemaOf}), or
     *                     {@code null} for an agent that answers in prose
     */
    private static ChatClient build(ChatClient.Builder builder, String model, Double temperature,
                                    Integer numCtx, Integer numPredict, String outputSchema) {
        return builder
                .defaultOptions(OllamaChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .keepAlive(KEEP_ALIVE)
                        .numCtx(numCtx)
                        .numPredict(numPredict)
                        .outputSchema(outputSchema))
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}

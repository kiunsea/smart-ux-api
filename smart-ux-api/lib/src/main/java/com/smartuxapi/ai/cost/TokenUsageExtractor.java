package com.smartuxapi.ai.cost;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provider 응답 JSON 에서 토큰 사용량을 추출하는 유틸.
 *
 * <p>두 provider 의 필드명이 상이:
 * <ul>
 *   <li>OpenAI Responses API: {@code usage.input_tokens} / {@code usage.output_tokens}</li>
 *   <li>OpenAI Chat Completions / Embeddings: {@code usage.prompt_tokens} / {@code usage.completion_tokens}</li>
 *   <li>Gemini: {@code usageMetadata.promptTokenCount} / {@code usageMetadata.candidatesTokenCount}</li>
 * </ul>
 *
 * @since 0.9.1
 */
public final class TokenUsageExtractor {

    private TokenUsageExtractor() {}

    public static final class Usage {
        public final int inputTokens;
        public final int outputTokens;
        public Usage(int inputTokens, int outputTokens) {
            this.inputTokens = Math.max(0, inputTokens);
            this.outputTokens = Math.max(0, outputTokens);
        }
        public static final Usage ZERO = new Usage(0, 0);
    }

    /**
     * OpenAI 응답에서 usage 파싱. 둘 다 아니면 0 반환.
     */
    public static Usage fromOpenAi(JsonNode responseJson) {
        if (responseJson == null) return Usage.ZERO;
        JsonNode usage = responseJson.get("usage");
        if (usage == null) return Usage.ZERO;

        int in = 0;
        int out = 0;
        // Responses API 신형
        if (usage.has("input_tokens")) in = usage.path("input_tokens").asInt(0);
        if (usage.has("output_tokens")) out = usage.path("output_tokens").asInt(0);
        // Chat Completions / Embeddings 구형
        if (in == 0 && usage.has("prompt_tokens")) in = usage.path("prompt_tokens").asInt(0);
        if (out == 0 && usage.has("completion_tokens")) out = usage.path("completion_tokens").asInt(0);
        return new Usage(in, out);
    }

    /**
     * Gemini 응답에서 usage 파싱.
     */
    public static Usage fromGemini(JsonNode responseJson) {
        if (responseJson == null) return Usage.ZERO;
        JsonNode usage = responseJson.get("usageMetadata");
        if (usage == null) return Usage.ZERO;
        int in = usage.path("promptTokenCount").asInt(0);
        int out = usage.path("candidatesTokenCount").asInt(0);
        return new Usage(in, out);
    }
}

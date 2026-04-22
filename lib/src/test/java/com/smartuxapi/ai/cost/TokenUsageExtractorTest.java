package com.smartuxapi.ai.cost;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenUsageExtractor 단위 테스트")
class TokenUsageExtractorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("OpenAI Responses API — input_tokens/output_tokens")
    void testOpenAiResponsesAPI() throws Exception {
        String json = "{\"usage\":{\"input_tokens\":100,\"output_tokens\":50}}";
        TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(MAPPER.readTree(json));
        assertEquals(100, u.inputTokens);
        assertEquals(50, u.outputTokens);
    }

    @Test
    @DisplayName("OpenAI Chat Completions — prompt_tokens/completion_tokens")
    void testOpenAiChatCompletions() throws Exception {
        String json = "{\"usage\":{\"prompt_tokens\":200,\"completion_tokens\":100}}";
        TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(MAPPER.readTree(json));
        assertEquals(200, u.inputTokens);
        assertEquals(100, u.outputTokens);
    }

    @Test
    @DisplayName("OpenAI Embeddings — prompt_tokens 만")
    void testOpenAiEmbeddings() throws Exception {
        String json = "{\"usage\":{\"prompt_tokens\":20}}";
        TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(MAPPER.readTree(json));
        assertEquals(20, u.inputTokens);
        assertEquals(0, u.outputTokens);
    }

    @Test
    @DisplayName("OpenAI usage 없는 경우 — ZERO")
    void testOpenAiMissingUsage() throws Exception {
        TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(MAPPER.readTree("{}"));
        assertEquals(0, u.inputTokens);
        assertEquals(0, u.outputTokens);
    }

    @Test
    @DisplayName("Gemini — usageMetadata 필드 파싱")
    void testGemini() throws Exception {
        String json = "{\"usageMetadata\":{\"promptTokenCount\":150,\"candidatesTokenCount\":75,\"totalTokenCount\":225}}";
        TokenUsageExtractor.Usage u = TokenUsageExtractor.fromGemini(MAPPER.readTree(json));
        assertEquals(150, u.inputTokens);
        assertEquals(75, u.outputTokens);
    }

    @Test
    @DisplayName("Gemini usageMetadata 없으면 ZERO")
    void testGeminiMissingUsage() throws Exception {
        TokenUsageExtractor.Usage u = TokenUsageExtractor.fromGemini(MAPPER.readTree("{}"));
        assertEquals(0, u.inputTokens);
        assertEquals(0, u.outputTokens);
    }

    @Test
    @DisplayName("null input → ZERO")
    void testNullInput() {
        assertSame(TokenUsageExtractor.Usage.ZERO, TokenUsageExtractor.fromOpenAi(null));
        assertSame(TokenUsageExtractor.Usage.ZERO, TokenUsageExtractor.fromGemini(null));
    }
}

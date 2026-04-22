package com.smartuxapi.ai.embedding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.embedding.impl.GeminiEmbeddingService;
import com.smartuxapi.ai.embedding.impl.OpenAiEmbeddingService;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmbeddingServiceFactory 단위 테스트")
class EmbeddingServiceFactoryTest {

    @Test
    @DisplayName("createOpenAI — 기본 모델/차원")
    void testOpenAI() {
        EmbeddingService s = EmbeddingServiceFactory.createOpenAI("fake");
        assertTrue(s instanceof OpenAiEmbeddingService);
        assertEquals(1536, s.getDimension());
    }

    @Test
    @DisplayName("createOpenAI(apiKey, model) — 모델 지정")
    void testOpenAIWithModel() {
        EmbeddingService s = EmbeddingServiceFactory.createOpenAI("fake", "text-embedding-3-large");
        assertEquals(3072, s.getDimension());
    }

    @Test
    @DisplayName("createGemini — 기본 모델/차원")
    void testGemini() {
        EmbeddingService s = EmbeddingServiceFactory.createGemini("fake");
        assertTrue(s instanceof GeminiEmbeddingService);
        assertEquals(768, s.getDimension());
    }

    @Test
    @DisplayName("createGemini(apiKey, model) — 모델 지정")
    void testGeminiWithModel() {
        EmbeddingService s = EmbeddingServiceFactory.createGemini("fake", "embedding-001");
        assertEquals(768, s.getDimension());
    }

    @Test
    @DisplayName("createOpenAIFromEnv — 환경변수 없으면 disabled")
    void testOpenAIFromEnvWithoutKey() {
        // 테스트 환경에 OPENAI_API_KEY 가 없으면 isEnabled=false
        EmbeddingService s = EmbeddingServiceFactory.createOpenAIFromEnv();
        String envKey = System.getenv("OPENAI_API_KEY");
        String propKey = System.getProperty("openai.api.key");
        if ((envKey == null || envKey.isEmpty()) && (propKey == null || propKey.isEmpty())) {
            assertFalse(s.isEnabled());
        } else {
            assertTrue(s.isEnabled());
        }
    }

    @Test
    @DisplayName("createGeminiFromEnv — 환경변수 없으면 disabled")
    void testGeminiFromEnvWithoutKey() {
        EmbeddingService s = EmbeddingServiceFactory.createGeminiFromEnv();
        String envKey = System.getenv("GEMINI_API_KEY");
        String propKey = System.getProperty("gemini.api.key");
        if ((envKey == null || envKey.isEmpty()) && (propKey == null || propKey.isEmpty())) {
            assertFalse(s.isEnabled());
        } else {
            assertTrue(s.isEnabled());
        }
    }
}

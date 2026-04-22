package com.smartuxapi.ai.embedding;

import com.smartuxapi.ai.embedding.impl.GeminiEmbeddingService;
import com.smartuxapi.ai.embedding.impl.OpenAiEmbeddingService;

/**
 * {@link EmbeddingService} 인스턴스 팩토리.
 *
 * <p>기본 모델:
 * <ul>
 *   <li>OpenAI — {@code text-embedding-3-small} (1536 차원)</li>
 *   <li>Gemini — {@code text-embedding-004} (768 차원)</li>
 * </ul>
 *
 * @since 0.9.0
 */
public final class EmbeddingServiceFactory {

    private EmbeddingServiceFactory() {}

    public static EmbeddingService createOpenAI(String apiKey) {
        return new OpenAiEmbeddingService(apiKey);
    }

    public static EmbeddingService createOpenAI(String apiKey, String model) {
        return new OpenAiEmbeddingService(apiKey, model);
    }

    /**
     * 환경 변수에서 API 키를 읽어 OpenAI Embedding 서비스 생성.
     * 조회 순서: {@code OPENAI_API_KEY} 환경변수 → {@code openai.api.key} 시스템 프로퍼티.
     */
    public static EmbeddingService createOpenAIFromEnv() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("openai.api.key");
        }
        return new OpenAiEmbeddingService(apiKey);
    }

    public static EmbeddingService createGemini(String apiKey) {
        return new GeminiEmbeddingService(apiKey);
    }

    public static EmbeddingService createGemini(String apiKey, String model) {
        return new GeminiEmbeddingService(apiKey, model);
    }

    /**
     * 환경 변수에서 API 키를 읽어 Gemini Embedding 서비스 생성.
     * 조회 순서: {@code GEMINI_API_KEY} 환경변수 → {@code gemini.api.key} 시스템 프로퍼티.
     */
    public static EmbeddingService createGeminiFromEnv() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("gemini.api.key");
        }
        return new GeminiEmbeddingService(apiKey);
    }
}

package com.smartuxapi.ai.vision;

import com.smartuxapi.ai.vision.impl.GeminiVisionService;
import com.smartuxapi.ai.vision.impl.OpenAiVisionService;

/**
 * {@link VisionService} 인스턴스 팩토리.
 *
 * <p>v0.7.0: OpenAI Vision 만 지원.
 * <p>v0.8.1: Gemini Vision 추가 (HTTPS URL 은 자동 다운로드 후 base64 inline 전달).
 *
 * @since 0.7.0
 */
public final class VisionServiceFactory {

    private VisionServiceFactory() {}

    /**
     * OpenAI Vision 서비스 생성 — 기본 모델 (gpt-4o-mini).
     *
     * @param apiKey OpenAI API 키 (null/empty 허용 — {@link VisionService#isEnabled()} false 반환)
     */
    public static VisionService createOpenAI(String apiKey) {
        return new OpenAiVisionService(apiKey);
    }

    /**
     * OpenAI Vision 서비스 생성 — 모델 지정.
     *
     * @param apiKey OpenAI API 키
     * @param model 모델 식별자 (예: "gpt-4o", "gpt-4o-mini")
     */
    public static VisionService createOpenAI(String apiKey, String model) {
        return new OpenAiVisionService(apiKey, model);
    }

    /**
     * 환경 변수에서 API 키를 읽어 OpenAI Vision 서비스 생성.
     *
     * <p>조회 순서:
     * <ol>
     *   <li>환경변수 {@code OPENAI_API_KEY}</li>
     *   <li>시스템 프로퍼티 {@code openai.api.key}</li>
     * </ol>
     * 모두 없으면 비활성 상태의 서비스 반환 ({@code isEnabled()} false).
     */
    public static VisionService createOpenAIFromEnv() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("openai.api.key");
        }
        return new OpenAiVisionService(apiKey);
    }

    /**
     * Gemini Vision 서비스 생성 — 기본 모델 (gemini-1.5-flash).
     *
     * @param apiKey Gemini API 키
     * @since 0.8.1
     */
    public static VisionService createGemini(String apiKey) {
        return new GeminiVisionService(apiKey);
    }

    /**
     * Gemini Vision 서비스 생성 — 모델 지정 (예: gemini-1.5-flash, gemini-2.5-flash, gemini-1.5-pro).
     *
     * @param apiKey Gemini API 키
     * @param model 모델 식별자
     * @since 0.8.1
     */
    public static VisionService createGemini(String apiKey, String model) {
        return new GeminiVisionService(apiKey, model);
    }

    /**
     * 환경 변수에서 API 키를 읽어 Gemini Vision 서비스 생성.
     * 조회 순서: {@code GEMINI_API_KEY} 환경변수 → {@code gemini.api.key} 시스템 프로퍼티.
     *
     * @since 0.8.1
     */
    public static VisionService createGeminiFromEnv() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = System.getProperty("gemini.api.key");
        }
        return new GeminiVisionService(apiKey);
    }
}

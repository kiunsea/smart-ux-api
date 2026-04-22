package com.smartuxapi.ai.vision.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.vision.VisionException;
import com.smartuxapi.ai.vision.VisionService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenAiVisionService 단위 테스트 — 네트워크 호출 없는 경로만 검증.
 * 실제 API 호출은 API 키 필요 — 통합 테스트로 분리.
 */
@DisplayName("OpenAiVisionService 단위 테스트")
class OpenAiVisionServiceTest {

    @Test
    @DisplayName("API 키 없으면 isEnabled()=false")
    void testDisabled() {
        VisionService s = new OpenAiVisionService(null);
        assertFalse(s.isEnabled());
        VisionService s2 = new OpenAiVisionService("");
        assertFalse(s2.isEnabled());
        VisionService s3 = new OpenAiVisionService("   ");
        assertFalse(s3.isEnabled());
    }

    @Test
    @DisplayName("API 키 있으면 isEnabled()=true")
    void testEnabled() {
        VisionService s = new OpenAiVisionService("sk-fake");
        assertTrue(s.isEnabled());
    }

    @Test
    @DisplayName("비활성 상태에서 extractText 호출 시 VisionException")
    void testExtractTextWhenDisabled() {
        VisionService s = new OpenAiVisionService("");
        VisionException ex = assertThrows(VisionException.class,
                () -> s.extractText("https://x/1.png"));
        assertTrue(ex.getMessage().contains("API key"));
    }

    @Test
    @DisplayName("빈 imageUrl 은 VisionException")
    void testEmptyUrl() {
        VisionService s = new OpenAiVisionService("sk-fake");
        assertThrows(VisionException.class, () -> s.extractText(""));
        assertThrows(VisionException.class, () -> s.extractText(null));
    }

    @Test
    @DisplayName("빈 base64 는 VisionException")
    void testEmptyBase64() {
        VisionService s = new OpenAiVisionService("sk-fake");
        assertThrows(VisionException.class, () -> s.extractTextFromBase64(""));
        assertThrows(VisionException.class, () -> s.extractTextFromBase64(null));
    }

    @Test
    @DisplayName("model null 시 기본 모델(gpt-4o-mini) 로 fallback")
    void testModelFallback() {
        // 내부 필드 검증이 직접 불가능하므로 생성자가 예외 없이 동작하는지만 체크
        assertDoesNotThrow(() -> new OpenAiVisionService("sk-fake", null));
        assertDoesNotThrow(() -> new OpenAiVisionService("sk-fake", ""));
        assertDoesNotThrow(() -> new OpenAiVisionService("sk-fake", "   "));
    }
}

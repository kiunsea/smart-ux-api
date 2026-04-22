package com.smartuxapi.ai.vision.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.vision.VisionException;
import com.smartuxapi.ai.vision.VisionService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GeminiVisionService 단위 테스트 — 네트워크 호출 없는 경로만 검증.
 * 실제 API 호출은 API 키 필요 — 통합 테스트로 분리.
 */
@DisplayName("GeminiVisionService 단위 테스트")
class GeminiVisionServiceTest {

    @Test
    @DisplayName("API 키 없으면 isEnabled()=false")
    void testDisabled() {
        assertFalse(new GeminiVisionService(null).isEnabled());
        assertFalse(new GeminiVisionService("").isEnabled());
        assertFalse(new GeminiVisionService("   ").isEnabled());
    }

    @Test
    @DisplayName("API 키 있으면 isEnabled()=true")
    void testEnabled() {
        assertTrue(new GeminiVisionService("fake-key").isEnabled());
    }

    @Test
    @DisplayName("비활성 상태에서 extractText 호출 시 VisionException")
    void testExtractTextWhenDisabled() {
        VisionService s = new GeminiVisionService("");
        VisionException ex = assertThrows(VisionException.class,
                () -> s.extractText("https://x/1.png"));
        assertTrue(ex.getMessage().contains("API key"));
    }

    @Test
    @DisplayName("빈 imageUrl 은 VisionException")
    void testEmptyUrl() {
        VisionService s = new GeminiVisionService("fake");
        assertThrows(VisionException.class, () -> s.extractText(""));
        assertThrows(VisionException.class, () -> s.extractText(null));
    }

    @Test
    @DisplayName("빈 base64 는 VisionException")
    void testEmptyBase64() {
        VisionService s = new GeminiVisionService("fake");
        assertThrows(VisionException.class, () -> s.extractTextFromBase64(""));
        assertThrows(VisionException.class, () -> s.extractTextFromBase64(null));
    }

    @Test
    @DisplayName("잘못된 data URI 는 VisionException (comma 없음)")
    void testMalformedDataUri() {
        VisionService s = new GeminiVisionService("fake");
        VisionException ex = assertThrows(VisionException.class,
                () -> s.extractText("data:image/png;base64xxxx"));
        assertTrue(ex.getMessage().contains("comma") || ex.getMessage().contains("data"));
    }

    @Test
    @DisplayName("guessMimeTypeFromUrl — 확장자 매핑")
    void testMimeGuess() {
        assertEquals("image/png", GeminiVisionService.guessMimeTypeFromUrl("foo/bar.PNG"));
        assertEquals("image/webp", GeminiVisionService.guessMimeTypeFromUrl("foo/bar.webp"));
        assertEquals("image/gif", GeminiVisionService.guessMimeTypeFromUrl("a.gif"));
        assertEquals("image/bmp", GeminiVisionService.guessMimeTypeFromUrl("a.bmp"));
        assertEquals("image/heic", GeminiVisionService.guessMimeTypeFromUrl("a.heic"));
        assertEquals("image/heic", GeminiVisionService.guessMimeTypeFromUrl("a.heif"));
        assertEquals("image/jpeg", GeminiVisionService.guessMimeTypeFromUrl("unknown.xyz"));
        assertEquals("image/jpeg", GeminiVisionService.guessMimeTypeFromUrl("no-extension"));
        // query string 제거 후 확장자 판단
        assertEquals("image/png", GeminiVisionService.guessMimeTypeFromUrl("a.png?v=123"));
    }
}

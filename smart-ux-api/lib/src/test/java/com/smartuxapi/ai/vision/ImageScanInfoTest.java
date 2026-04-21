package com.smartuxapi.ai.vision;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ImageScanInfo 단위 테스트")
class ImageScanInfoTest {

    @Test
    @DisplayName("편의 생성자 — confidence=1.0, modelUsed=null")
    void testConvenienceConstructor() {
        ImageScanInfo info = new ImageScanInfo("https://x/1.png", "Login");
        assertEquals("https://x/1.png", info.getImageUrl());
        assertEquals("Login", info.getExtractedText());
        assertEquals(1.0, info.getConfidence());
        assertNull(info.getModelUsed());
        assertNotNull(info.getTimestamp());
    }

    @Test
    @DisplayName("전체 필드 생성자 — 모든 값이 반영된다")
    void testFullConstructor() {
        ImageScanInfo info = new ImageScanInfo("u", "t", 0.8, "gpt-4o-mini");
        assertEquals(0.8, info.getConfidence(), 0.0001);
        assertEquals("gpt-4o-mini", info.getModelUsed());
    }

    @Test
    @DisplayName("extractedText null 은 빈 문자열로 정규화")
    void testNullTextNormalized() {
        ImageScanInfo info = new ImageScanInfo("u", null);
        assertEquals("", info.getExtractedText());
    }

    @Test
    @DisplayName("imageUrl null 은 NullPointerException")
    void testNullUrl() {
        assertThrows(NullPointerException.class, () -> new ImageScanInfo(null, "t"));
    }

    @Test
    @DisplayName("confidence 는 0.0~1.0 으로 clamp")
    void testConfidenceClamp() {
        assertEquals(0.0, new ImageScanInfo("u", "t", -0.5, null).getConfidence());
        assertEquals(1.0, new ImageScanInfo("u", "t",  1.5, null).getConfidence());
        assertEquals(0.0, new ImageScanInfo("u", "t",  0.0, null).getConfidence());
        assertEquals(1.0, new ImageScanInfo("u", "t",  1.0, null).getConfidence());
    }

    @Test
    @DisplayName("toJSON — 고정 키 5종이 모두 포함됨 (스케치 §6.3)")
    void testToJsonKeys() {
        ImageScanInfo info = new ImageScanInfo("u", "t", 0.5, "m");
        JSONObject json = info.toJSON();

        assertTrue(json.containsKey("imageUrl"));
        assertTrue(json.containsKey("extractedText"));
        assertTrue(json.containsKey("confidence"));
        assertTrue(json.containsKey("timestamp"));
        assertTrue(json.containsKey("modelUsed"));

        assertEquals("u", json.get("imageUrl"));
        assertEquals("t", json.get("extractedText"));
        assertEquals(0.5, json.get("confidence"));
        assertEquals("m", json.get("modelUsed"));
    }

    @Test
    @DisplayName("toString 은 텍스트 원문을 노출하지 않는다 (길이만)")
    void testToStringNoTextLeak() {
        String secret = "SECRET-BUTTON-LABEL";
        ImageScanInfo info = new ImageScanInfo("u", secret);
        assertFalse(info.toString().contains(secret));
        assertTrue(info.toString().contains("textLen=" + secret.length()));
    }
}

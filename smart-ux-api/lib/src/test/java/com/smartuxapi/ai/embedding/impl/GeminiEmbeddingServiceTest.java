package com.smartuxapi.ai.embedding.impl;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.embedding.EmbeddingException;
import com.smartuxapi.ai.embedding.EmbeddingService;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GeminiEmbeddingService 단위 테스트")
class GeminiEmbeddingServiceTest {

    @Test
    @DisplayName("API 키 없으면 isEnabled()=false")
    void testDisabled() {
        assertFalse(new GeminiEmbeddingService(null).isEnabled());
        assertFalse(new GeminiEmbeddingService("").isEnabled());
    }

    @Test
    @DisplayName("기본 모델은 text-embedding-004 (768 차원)")
    void testDefaultModel() {
        EmbeddingService s = new GeminiEmbeddingService("fake");
        assertEquals("text-embedding-004", s.getModel());
        assertEquals(768, s.getDimension());
    }

    @Test
    @DisplayName("embedding-001 → 768 차원")
    void testAltModel() {
        EmbeddingService s = new GeminiEmbeddingService("fake", "embedding-001");
        assertEquals(768, s.getDimension());
    }

    @Test
    @DisplayName("미등록 모델 → getDimension()=-1")
    void testUnknownModel() {
        assertEquals(-1, new GeminiEmbeddingService("fake", "future-model").getDimension());
    }

    @Test
    @DisplayName("비활성 / 빈 입력 / 빈 배치 → EmbeddingException")
    void testInvalidInputs() {
        assertThrows(EmbeddingException.class, () -> new GeminiEmbeddingService("").embed("hi"));
        EmbeddingService s = new GeminiEmbeddingService("fake");
        assertThrows(EmbeddingException.class, () -> s.embed(""));
        assertThrows(EmbeddingException.class, () -> s.embed(null));
        assertThrows(EmbeddingException.class, () -> s.embedBatch(null));
        assertThrows(EmbeddingException.class, () -> s.embedBatch(Collections.emptyList()));
    }
}

package com.smartuxapi.ai.embedding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmbeddingResult 단위 테스트")
class EmbeddingResultTest {

    @Test
    @DisplayName("기본 필드 — size/dimension/model/promptTokens")
    void testFields() {
        float[][] vecs = {
                {0.1f, 0.2f, 0.3f},
                {0.4f, 0.5f, 0.6f}
        };
        EmbeddingResult r = new EmbeddingResult(vecs, "m", 42);
        assertEquals(2, r.size());
        assertEquals(3, r.getDimension());
        assertEquals("m", r.getModel());
        assertEquals(42, r.getPromptTokens());
        assertArrayEquals(new float[]{0.4f, 0.5f, 0.6f}, r.get(1));
    }

    @Test
    @DisplayName("model null → 빈 문자열, promptTokens 음수 → 0")
    void testDefaults() {
        float[][] vecs = {{1f}};
        EmbeddingResult r = new EmbeddingResult(vecs, null, -5);
        assertEquals("", r.getModel());
        assertEquals(0, r.getPromptTokens());
    }

    @Test
    @DisplayName("vectors null/empty → IllegalArgumentException")
    void testInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new EmbeddingResult(null, "m", 0));
        assertThrows(IllegalArgumentException.class,
                () -> new EmbeddingResult(new float[0][], "m", 0));
    }

    @Test
    @DisplayName("toString — size/dim/model 포함")
    void testToString() {
        float[][] vecs = {{1f, 2f}};
        String s = new EmbeddingResult(vecs, "mdl", 10).toString();
        assertTrue(s.contains("size=1"));
        assertTrue(s.contains("dim=2"));
        assertTrue(s.contains("mdl"));
    }
}

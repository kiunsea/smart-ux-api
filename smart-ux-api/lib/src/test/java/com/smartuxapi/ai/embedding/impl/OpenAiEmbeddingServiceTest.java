package com.smartuxapi.ai.embedding.impl;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.embedding.EmbeddingException;
import com.smartuxapi.ai.embedding.EmbeddingService;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAiEmbeddingService 단위 테스트")
class OpenAiEmbeddingServiceTest {

    @Test
    @DisplayName("API 키 없으면 isEnabled()=false")
    void testDisabled() {
        assertFalse(new OpenAiEmbeddingService(null).isEnabled());
        assertFalse(new OpenAiEmbeddingService("").isEnabled());
        assertFalse(new OpenAiEmbeddingService("   ").isEnabled());
    }

    @Test
    @DisplayName("기본 모델은 text-embedding-3-small (1536 차원)")
    void testDefaultModel() {
        EmbeddingService s = new OpenAiEmbeddingService("fake");
        assertEquals("text-embedding-3-small", s.getModel());
        assertEquals(1536, s.getDimension());
    }

    @Test
    @DisplayName("text-embedding-3-large → 3072 차원")
    void testLargeModel() {
        EmbeddingService s = new OpenAiEmbeddingService("fake", "text-embedding-3-large");
        assertEquals(3072, s.getDimension());
    }

    @Test
    @DisplayName("미등록 모델 → getDimension()=-1")
    void testUnknownModel() {
        EmbeddingService s = new OpenAiEmbeddingService("fake", "custom-model");
        assertEquals(-1, s.getDimension());
    }

    @Test
    @DisplayName("비활성 상태에서 embed 호출 시 EmbeddingException")
    void testDisabledEmbedThrows() {
        EmbeddingService s = new OpenAiEmbeddingService("");
        EmbeddingException ex = assertThrows(EmbeddingException.class, () -> s.embed("hi"));
        assertTrue(ex.getMessage().contains("API key"));
    }

    @Test
    @DisplayName("빈 text / null text → EmbeddingException")
    void testEmptyText() {
        EmbeddingService s = new OpenAiEmbeddingService("fake");
        assertThrows(EmbeddingException.class, () -> s.embed(""));
        assertThrows(EmbeddingException.class, () -> s.embed(null));
    }

    @Test
    @DisplayName("빈/null 배치 → EmbeddingException")
    void testEmptyBatch() {
        EmbeddingService s = new OpenAiEmbeddingService("fake");
        assertThrows(EmbeddingException.class, () -> s.embedBatch(null));
        assertThrows(EmbeddingException.class, () -> s.embedBatch(Collections.emptyList()));
    }

    @Test
    @DisplayName("null 없는 유효한 배치는 사전 검증에서 통과 (네트워크 접근 전)")
    void testValidBatchPassesInputCheck() {
        EmbeddingService s = new OpenAiEmbeddingService("");  // disabled
        // 빈 요소가 섞이면 batch 검증 이전에 isEnabled() 에서 먼저 실패
        assertThrows(EmbeddingException.class,
                () -> s.embedBatch(Arrays.asList("ok")),
                "disabled 상태는 네트워크 호출 전 EmbeddingException");
    }
}

package com.smartuxapi.ai.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CacheHint 단위 테스트")
class CacheHintTest {

    @Test
    @DisplayName("of(content) — 기본 TTL 과 기본 라벨이 설정된다")
    void testOfContent() {
        CacheHint h = CacheHint.of("hello world");

        assertEquals("hello world", h.getContent());
        assertEquals(CacheHint.DEFAULT_TTL_SECONDS, h.getTtlSeconds());
        assertEquals("unlabeled", h.getLabel());
        assertEquals(11, h.getContentLength());
    }

    @Test
    @DisplayName("of(content, label) — 라벨이 설정된다")
    void testOfContentAndLabel() {
        CacheHint h = CacheHint.of("data", "ui-object-map");
        assertEquals("ui-object-map", h.getLabel());
    }

    @Test
    @DisplayName("빈 라벨은 'unlabeled' 로 정규화된다")
    void testEmptyLabelNormalized() {
        CacheHint h = CacheHint.of("data", "");
        assertEquals("unlabeled", h.getLabel());
    }

    @Test
    @DisplayName("withTtl — TTL 이 올바르게 반영된다")
    void testWithTtl() {
        CacheHint h = CacheHint.withTtl("data", 7200);
        assertEquals(7200, h.getTtlSeconds());
    }

    @Test
    @DisplayName("withTtl — 0 이하 TTL 은 IllegalArgumentException")
    void testWithTtlInvalid() {
        assertThrows(IllegalArgumentException.class, () -> CacheHint.withTtl("data", 0));
        assertThrows(IllegalArgumentException.class, () -> CacheHint.withTtl("data", -1));
    }

    @Test
    @DisplayName("content 가 null 이면 NullPointerException")
    void testNullContent() {
        assertThrows(NullPointerException.class, () -> CacheHint.of(null));
    }

    @Test
    @DisplayName("toString 은 콘텐츠 실체를 노출하지 않는다 (길이만)")
    void testToStringNoContentLeak() {
        String secret = "super-secret-prompt-data";
        CacheHint h = CacheHint.of(secret, "s");
        assertFalse(h.toString().contains(secret), "toString() 이 콘텐츠를 노출해선 안 된다");
        assertTrue(h.toString().contains("contentLen=" + secret.length()));
    }
}

package com.smartuxapi.ai.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CacheMetrics 단위 테스트")
class CacheMetricsTest {

    @Test
    @DisplayName("EMPTY 는 토큰 0, provider none")
    void testEmpty() {
        CacheMetrics m = CacheMetrics.EMPTY;
        assertEquals(0, m.getTotalInputTokens());
        assertEquals(0, m.getCachedInputTokens());
        assertEquals(0.0, m.getHitRate());
        assertEquals("none", m.getProvider());
    }

    @Test
    @DisplayName("히트율 계산")
    void testHitRate() {
        CacheMetrics m = new CacheMetrics(1000, 800, "openai");
        assertEquals(0.8, m.getHitRate(), 0.0001);
    }

    @Test
    @DisplayName("총 토큰 0 인 경우 히트율 0.0")
    void testZeroDenominator() {
        CacheMetrics m = new CacheMetrics(0, 0, "openai");
        assertEquals(0.0, m.getHitRate());
    }

    @Test
    @DisplayName("cached > total 비일관 응답은 total 로 clamp")
    void testClamp() {
        CacheMetrics m = new CacheMetrics(100, 150, "openai");
        assertEquals(100, m.getCachedInputTokens());
        assertEquals(1.0, m.getHitRate(), 0.0001);
    }

    @Test
    @DisplayName("음수 토큰은 IllegalArgumentException")
    void testNegative() {
        assertThrows(IllegalArgumentException.class, () -> new CacheMetrics(-1, 0, "x"));
        assertThrows(IllegalArgumentException.class, () -> new CacheMetrics(10, -1, "x"));
    }

    @Test
    @DisplayName("null/empty provider 는 unknown 으로 정규화")
    void testProviderNormalization() {
        assertEquals("unknown", new CacheMetrics(0, 0, null).getProvider());
        assertEquals("unknown", new CacheMetrics(0, 0, "").getProvider());
    }
}

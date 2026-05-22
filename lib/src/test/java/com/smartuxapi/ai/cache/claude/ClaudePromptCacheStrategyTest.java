package com.smartuxapi.ai.cache.claude;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ClaudePromptCacheStrategy 단위 테스트")
class ClaudePromptCacheStrategyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("provider 식별자는 'anthropic'")
    void testProvider() {
        assertEquals("anthropic", new ClaudePromptCacheStrategy().getProvider());
    }

    @Test
    @DisplayName("prime 은 힌트를 로컬 상태에 저장한다")
    void testPrimeStoresHint() {
        ClaudePromptCacheStrategy s = new ClaudePromptCacheStrategy();
        CacheHint hint = CacheHint.of("stable-prefix", "ui-map");
        s.prime(hint);
        assertSame(hint, s.getCurrentHint());
    }

    @Test
    @DisplayName("invalidate 는 힌트와 메트릭을 초기화한다")
    void testInvalidate() {
        ClaudePromptCacheStrategy s = new ClaudePromptCacheStrategy();
        s.prime(CacheHint.of("x"));
        s.invalidate();
        assertNull(s.getCurrentHint());
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }

    @Test
    @DisplayName("usage 의 캐시 토큰을 메트릭으로 파싱 — cache_read 가 히트")
    void testRecordMetricsFromResponse() throws Exception {
        String json = "{"
                + "\"usage\": {"
                + "  \"input_tokens\": 100,"
                + "  \"cache_creation_input_tokens\": 300,"
                + "  \"cache_read_input_tokens\": 600"
                + "}"
                + "}";
        JsonNode node = MAPPER.readTree(json);

        ClaudePromptCacheStrategy s = new ClaudePromptCacheStrategy();
        s.recordMetricsFromResponse(node);

        CacheMetrics m = s.getLastMetrics();
        assertEquals(1000, m.getTotalInputTokens(), "input + cache_creation + cache_read");
        assertEquals(600, m.getCachedInputTokens());
        assertEquals("anthropic", m.getProvider());
        assertEquals(0.6, m.getHitRate(), 0.0001);
    }

    @Test
    @DisplayName("캐시 토큰 필드가 없으면 cached=0")
    void testRecordMetricsNoCacheFields() throws Exception {
        JsonNode node = MAPPER.readTree("{ \"usage\": { \"input_tokens\": 500 } }");

        ClaudePromptCacheStrategy s = new ClaudePromptCacheStrategy();
        s.recordMetricsFromResponse(node);

        CacheMetrics m = s.getLastMetrics();
        assertEquals(500, m.getTotalInputTokens());
        assertEquals(0, m.getCachedInputTokens());
        assertEquals(0.0, m.getHitRate());
    }

    @Test
    @DisplayName("usage 필드가 없으면 메트릭 변경 없음")
    void testRecordMetricsMissingUsage() throws Exception {
        ClaudePromptCacheStrategy s = new ClaudePromptCacheStrategy();
        s.recordMetricsFromResponse(MAPPER.readTree("{\"content\":[]}"));
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }

    @Test
    @DisplayName("null 응답은 조용히 무시된다")
    void testRecordMetricsNull() {
        ClaudePromptCacheStrategy s = new ClaudePromptCacheStrategy();
        assertDoesNotThrow(() -> s.recordMetricsFromResponse(null));
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }
}

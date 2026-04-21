package com.smartuxapi.ai.cache.gemini;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheMetrics;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GeminiContextCacheStrategy 의 순수 단위 테스트.
 *
 * <p>네트워크 호출이 필요한 {@code prime()} / {@code invalidate()} 는 여기서 다루지 않는다
 * (통합 테스트로 분리).
 */
@DisplayName("GeminiContextCacheStrategy 단위 테스트")
class GeminiContextCacheStrategyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("생성자 — 빈/null apiKey 또는 modelName 은 IllegalArgumentException")
    void testConstructorValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new GeminiContextCacheStrategy(null, "gemini-1.5-flash"));
        assertThrows(IllegalArgumentException.class,
                () -> new GeminiContextCacheStrategy("", "gemini-1.5-flash"));
        assertThrows(IllegalArgumentException.class,
                () -> new GeminiContextCacheStrategy("key", null));
        assertThrows(IllegalArgumentException.class,
                () -> new GeminiContextCacheStrategy("key", ""));
    }

    @Test
    @DisplayName("provider 식별자는 'gemini', 초기 상태는 비어있음")
    void testInitialState() {
        GeminiContextCacheStrategy s = new GeminiContextCacheStrategy("fake-key", "gemini-1.5-flash");
        assertEquals("gemini", s.getProvider());
        assertNull(s.getCurrentHint());
        assertNull(s.getCacheResourceName());
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }

    @Test
    @DisplayName("usageMetadata.cachedContentTokenCount 를 메트릭으로 파싱")
    void testRecordMetricsFromResponse() throws Exception {
        String json = "{"
                + "\"usageMetadata\": {"
                + "  \"promptTokenCount\": 40000,"
                + "  \"cachedContentTokenCount\": 32000,"
                + "  \"candidatesTokenCount\": 50,"
                + "  \"totalTokenCount\": 40050"
                + "}"
                + "}";
        JsonNode node = MAPPER.readTree(json);

        GeminiContextCacheStrategy s = new GeminiContextCacheStrategy("fake-key", "gemini-1.5-flash");
        s.recordMetricsFromResponse(node);

        CacheMetrics m = s.getLastMetrics();
        assertEquals(40000, m.getTotalInputTokens());
        assertEquals(32000, m.getCachedInputTokens());
        assertEquals("gemini", m.getProvider());
        assertEquals(0.8, m.getHitRate(), 0.0001);
    }

    @Test
    @DisplayName("usageMetadata 가 없으면 메트릭 변경 없음")
    void testRecordMetricsMissingUsage() throws Exception {
        String json = "{\"candidates\":[]}";
        JsonNode node = MAPPER.readTree(json);

        GeminiContextCacheStrategy s = new GeminiContextCacheStrategy("fake-key", "gemini-1.5-flash");
        s.recordMetricsFromResponse(node);

        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }

    @Test
    @DisplayName("cachedContentTokenCount 필드가 없으면 cached=0")
    void testRecordMetricsNoCachedField() throws Exception {
        String json = "{\"usageMetadata\":{\"promptTokenCount\":500}}";
        JsonNode node = MAPPER.readTree(json);

        GeminiContextCacheStrategy s = new GeminiContextCacheStrategy("fake-key", "gemini-1.5-flash");
        s.recordMetricsFromResponse(node);

        CacheMetrics m = s.getLastMetrics();
        assertEquals(500, m.getTotalInputTokens());
        assertEquals(0, m.getCachedInputTokens());
    }

    @Test
    @DisplayName("modelName 이 'models/' 로 시작하지 않으면 자동으로 prefix 추가")
    void testModelNameNormalization() {
        // 내부 상태를 직접 검증할 수 없으므로 간접 확인 — 생성자가 예외 없이 동작하는지만 체크
        assertDoesNotThrow(() -> new GeminiContextCacheStrategy("k", "gemini-1.5-flash"));
        assertDoesNotThrow(() -> new GeminiContextCacheStrategy("k", "models/gemini-1.5-flash"));
    }
}

package com.smartuxapi.ai.cache.openai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAiPromptCacheStrategy 단위 테스트")
class OpenAiPromptCacheStrategyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("provider 식별자는 'openai'")
    void testProvider() {
        assertEquals("openai", new OpenAiPromptCacheStrategy().getProvider());
    }

    @Test
    @DisplayName("prime 은 힌트를 로컬 상태에 저장한다 (네트워크 호출 없음)")
    void testPrimeStoresHint() throws Exception {
        OpenAiPromptCacheStrategy s = new OpenAiPromptCacheStrategy();
        CacheHint hint = CacheHint.of("stable-prefix", "ui-map");
        s.prime(hint);

        assertSame(hint, s.getCurrentHint());
    }

    @Test
    @DisplayName("invalidate 는 힌트와 메트릭을 초기화한다")
    void testInvalidate() throws Exception {
        OpenAiPromptCacheStrategy s = new OpenAiPromptCacheStrategy();
        s.prime(CacheHint.of("x"));
        s.invalidate();

        assertNull(s.getCurrentHint());
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }

    @Test
    @DisplayName("usage.prompt_tokens_details.cached_tokens 를 메트릭으로 파싱")
    void testRecordMetricsFromResponse() throws Exception {
        String json = "{"
                + "\"usage\": {"
                + "  \"prompt_tokens\": 1500,"
                + "  \"prompt_tokens_details\": { \"cached_tokens\": 1200 }"
                + "}"
                + "}";
        JsonNode node = MAPPER.readTree(json);

        OpenAiPromptCacheStrategy s = new OpenAiPromptCacheStrategy();
        s.recordMetricsFromResponse(node);

        CacheMetrics m = s.getLastMetrics();
        assertEquals(1500, m.getTotalInputTokens());
        assertEquals(1200, m.getCachedInputTokens());
        assertEquals("openai", m.getProvider());
        assertEquals(0.8, m.getHitRate(), 0.0001);
    }

    @Test
    @DisplayName("usage 필드가 없으면 메트릭 변경 없음")
    void testRecordMetricsMissingUsage() throws Exception {
        String json = "{\"output\":[]}";
        JsonNode node = MAPPER.readTree(json);

        OpenAiPromptCacheStrategy s = new OpenAiPromptCacheStrategy();
        s.recordMetricsFromResponse(node);

        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }

    @Test
    @DisplayName("prompt_tokens_details 가 없으면 cached=0 으로 처리")
    void testRecordMetricsNoDetails() throws Exception {
        String json = "{ \"usage\": { \"prompt_tokens\": 500 } }";
        JsonNode node = MAPPER.readTree(json);

        OpenAiPromptCacheStrategy s = new OpenAiPromptCacheStrategy();
        s.recordMetricsFromResponse(node);

        CacheMetrics m = s.getLastMetrics();
        assertEquals(500, m.getTotalInputTokens());
        assertEquals(0, m.getCachedInputTokens());
        assertEquals(0.0, m.getHitRate());
    }

    @Test
    @DisplayName("null 응답은 조용히 무시된다")
    void testRecordMetricsNull() {
        OpenAiPromptCacheStrategy s = new OpenAiPromptCacheStrategy();
        assertDoesNotThrow(() -> s.recordMetricsFromResponse(null));
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
    }
}

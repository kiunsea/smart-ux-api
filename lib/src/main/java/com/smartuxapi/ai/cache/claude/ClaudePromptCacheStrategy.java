package com.smartuxapi.ai.cache.claude;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;

/**
 * Anthropic Messages API prompt caching 전략.
 *
 * <p>Anthropic 측 정책:
 * <ul>
 *   <li>요청 content 블록에 {@code cache_control: {type:"ephemeral"}} 를 부여하면 캐시 대상이 됨</li>
 *   <li>응답 {@code usage.cache_read_input_tokens} 로 캐시 히트를 확인</li>
 * </ul>
 *
 * <p>본 전략의 역할:
 * <ol>
 *   <li>{@link #prime(CacheHint)} 로 받은 콘텐츠를 로컬에 보관</li>
 *   <li>{@link com.smartuxapi.ai.claude.ClaudeAPIConnection} 이 이 힌트를 top-level
 *       {@code system} 블록으로 배치하고 {@code cache_control} 을 부여</li>
 *   <li>응답에서 캐시 토큰 수를 파싱하여 메트릭으로 보관</li>
 * </ol>
 *
 * @since 0.10.0
 */
public class ClaudePromptCacheStrategy implements CacheStrategy {

    private static final Logger log = LogManager.getLogger(ClaudePromptCacheStrategy.class);

    private CacheHint currentHint = null;
    private CacheMetrics lastMetrics = CacheMetrics.EMPTY;

    @Override
    public void prime(CacheHint hint) {
        this.currentHint = hint;
        if (hint != null) {
            log.debug("Claude cache primed (ephemeral system prefix): {}", hint);
        }
    }

    @Override
    public CacheMetrics getLastMetrics() {
        return lastMetrics;
    }

    /**
     * Anthropic 응답에서 {@code usage.input_tokens} 와 캐시 토큰
     * ({@code cache_creation_input_tokens} / {@code cache_read_input_tokens}) 을 추출하여
     * 메트릭을 갱신한다. 히트는 {@code cache_read_input_tokens} 로 집계.
     */
    @Override
    public void recordMetricsFromResponse(JsonNode responseJson) {
        if (responseJson == null) return;
        JsonNode usage = responseJson.get("usage");
        if (usage == null) {
            log.debug("Claude response missing 'usage' field; metrics unchanged");
            return;
        }

        long input = readLong(usage, "input_tokens", 0L);
        long cacheCreate = readLong(usage, "cache_creation_input_tokens", 0L);
        long cacheRead = readLong(usage, "cache_read_input_tokens", 0L);
        long total = input + cacheCreate + cacheRead;

        this.lastMetrics = new CacheMetrics(total, cacheRead, "anthropic");
        log.debug("Claude cache metrics: {}", this.lastMetrics);
    }

    @Override
    public void invalidate() {
        this.currentHint = null;
        this.lastMetrics = CacheMetrics.EMPTY;
        log.debug("Claude cache invalidated (local)");
    }

    @Override
    public CacheHint getCurrentHint() {
        return currentHint;
    }

    @Override
    public String getProvider() {
        return "anthropic";
    }

    private static long readLong(JsonNode node, String field, long fallback) {
        JsonNode v = node.get(field);
        return (v == null || !v.canConvertToLong()) ? fallback : v.asLong();
    }
}

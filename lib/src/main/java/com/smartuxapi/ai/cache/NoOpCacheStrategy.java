package com.smartuxapi.ai.cache;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 아무 캐시도 적용하지 않는 기본 구현체.
 *
 * <p>{@link CacheStrategy} 가 설정되지 않았을 때의 안전한 fallback.
 *
 * @since 0.7.0
 */
public final class NoOpCacheStrategy implements CacheStrategy {

    public static final NoOpCacheStrategy INSTANCE = new NoOpCacheStrategy();

    private NoOpCacheStrategy() {}

    @Override public void prime(CacheHint hint) { /* no-op */ }

    @Override public CacheMetrics getLastMetrics() { return CacheMetrics.EMPTY; }

    @Override public void recordMetricsFromResponse(JsonNode responseJson) { /* no-op */ }

    @Override public void invalidate() { /* no-op */ }

    @Override public CacheHint getCurrentHint() { return null; }

    @Override public String getProvider() { return "none"; }
}

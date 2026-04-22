package com.smartuxapi.ai.cache;

/**
 * 캐시 히트 지표 (마지막 API 호출 기준).
 *
 * <p>{@link CacheStrategy#getLastMetrics()} 로 조회한다.
 *
 * @since 0.7.0
 */
public final class CacheMetrics {

    public static final CacheMetrics EMPTY = new CacheMetrics(0L, 0L, "none");

    private final long totalInputTokens;
    private final long cachedInputTokens;
    private final String provider;

    public CacheMetrics(long totalInputTokens, long cachedInputTokens, String provider) {
        if (totalInputTokens < 0 || cachedInputTokens < 0) {
            throw new IllegalArgumentException("token counts must be non-negative");
        }
        if (cachedInputTokens > totalInputTokens) {
            // 방어적: 일관성 깨진 응답은 hitRate 계산에서 1.0 으로 클램프됨
            cachedInputTokens = totalInputTokens;
        }
        this.totalInputTokens = totalInputTokens;
        this.cachedInputTokens = cachedInputTokens;
        this.provider = (provider == null || provider.isEmpty()) ? "unknown" : provider;
    }

    public long getTotalInputTokens()  { return totalInputTokens; }
    public long getCachedInputTokens() { return cachedInputTokens; }
    public String getProvider()        { return provider; }

    /**
     * 히트율 (0.0 ~ 1.0). 토큰 총합이 0 이면 0.0 반환.
     */
    public double getHitRate() {
        if (totalInputTokens == 0) return 0.0;
        return (double) cachedInputTokens / (double) totalInputTokens;
    }

    @Override
    public String toString() {
        return String.format("CacheMetrics{provider=%s, cached=%d/%d (%.1f%%)}",
                provider, cachedInputTokens, totalInputTokens, getHitRate() * 100.0);
    }
}

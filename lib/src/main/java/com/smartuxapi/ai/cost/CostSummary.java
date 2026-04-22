package com.smartuxapi.ai.cost;

import java.util.Collections;
import java.util.Map;

/**
 * {@link CostTracker} 의 스냅샷 집계 결과.
 *
 * <p>모든 필드는 읽기 전용. {@code byProvider} / {@code byCallKind} 는 {@link Collections#unmodifiableMap}.
 *
 * @since 0.9.1
 */
public final class CostSummary {

    private final int totalInputTokens;
    private final int totalOutputTokens;
    private final double totalUsd;
    private final int entryCount;
    private final int fallbackCount;
    private final Map<String, Double> byProvider;
    private final Map<String, Double> byCallKind;

    public CostSummary(int totalInputTokens, int totalOutputTokens, double totalUsd,
                       int entryCount, int fallbackCount,
                       Map<String, Double> byProvider, Map<String, Double> byCallKind) {
        this.totalInputTokens = totalInputTokens;
        this.totalOutputTokens = totalOutputTokens;
        this.totalUsd = totalUsd;
        this.entryCount = entryCount;
        this.fallbackCount = fallbackCount;
        this.byProvider = byProvider == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(byProvider);
        this.byCallKind = byCallKind == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(byCallKind);
    }

    public int getTotalInputTokens() { return totalInputTokens; }
    public int getTotalOutputTokens() { return totalOutputTokens; }
    public double getTotalUsd() { return totalUsd; }
    public int getEntryCount() { return entryCount; }
    public int getFallbackCount() { return fallbackCount; }
    public Map<String, Double> getByProvider() { return byProvider; }
    public Map<String, Double> getByCallKind() { return byCallKind; }

    @Override
    public String toString() {
        return String.format(
                "CostSummary{entries=%d, tokens=%d in + %d out, usd=%.6f, fallback=%d}",
                entryCount, totalInputTokens, totalOutputTokens, totalUsd, fallbackCount);
    }
}

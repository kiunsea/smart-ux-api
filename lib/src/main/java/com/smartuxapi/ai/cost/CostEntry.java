package com.smartuxapi.ai.cost;

/**
 * 개별 API 호출의 토큰 사용량/비용 기록.
 *
 * <p>불변 객체. {@link CostTracker} 에 저장되어 세션/전체 통계 집계에 사용된다.
 *
 * @since 0.9.1
 */
public final class CostEntry {

    private final String provider;       // "openai" / "gemini"
    private final String model;          // 예: "gpt-4o-mini"
    private final int inputTokens;
    private final int outputTokens;
    private final double usdCost;        // 추정값 (CostTable 단가 기반)
    private final long timestampMs;
    private final boolean fallbackTriggered;
    private final String callKind;       // "chat" / "tool_use" / "structured" / "vision" / "embedding"

    public CostEntry(String provider, String model,
                     int inputTokens, int outputTokens, double usdCost,
                     boolean fallbackTriggered, String callKind) {
        this(provider, model, inputTokens, outputTokens, usdCost,
                System.currentTimeMillis(), fallbackTriggered, callKind);
    }

    public CostEntry(String provider, String model,
                     int inputTokens, int outputTokens, double usdCost,
                     long timestampMs, boolean fallbackTriggered, String callKind) {
        this.provider = provider == null ? "" : provider;
        this.model = model == null ? "" : model;
        this.inputTokens = Math.max(0, inputTokens);
        this.outputTokens = Math.max(0, outputTokens);
        this.usdCost = Math.max(0.0, usdCost);
        this.timestampMs = timestampMs;
        this.fallbackTriggered = fallbackTriggered;
        this.callKind = callKind == null ? "chat" : callKind;
    }

    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public int getInputTokens() { return inputTokens; }
    public int getOutputTokens() { return outputTokens; }
    public double getUsdCost() { return usdCost; }
    public long getTimestampMs() { return timestampMs; }
    public boolean isFallbackTriggered() { return fallbackTriggered; }
    public String getCallKind() { return callKind; }

    @Override
    public String toString() {
        return String.format(
                "CostEntry{provider=%s, model=%s, in=%d, out=%d, usd=%.6f, fallback=%s, kind=%s}",
                provider, model, inputTokens, outputTokens, usdCost, fallbackTriggered, callKind);
    }
}

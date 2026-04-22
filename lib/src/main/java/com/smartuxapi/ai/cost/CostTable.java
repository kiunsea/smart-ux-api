package com.smartuxapi.ai.cost;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 모델 단가 테이블 (USD per 1M tokens).
 *
 * <p>정적 하드코딩 기본값 + 런타임 {@link #register(String, double, double)} override.
 * 2026-Q2 기준 공개 가격. 변동 시 소비자가 override 하거나 smart-ux-api 업데이트 대기.
 *
 * <p>미등록 모델은 {@link #lookup(String)} 에서 {@code null} 반환 → {@link CostTracker} 가
 * $0 로 기록하고 WARN 로그 출력.
 *
 * @since 0.9.1
 */
public final class CostTable {

    private static final Logger log = LogManager.getLogger(CostTable.class);
    private static final Map<String, Entry> TABLE = new LinkedHashMap<>();

    public static final class Entry {
        private final double inputPer1M;
        private final double outputPer1M;
        public Entry(double inputPer1M, double outputPer1M) {
            this.inputPer1M = inputPer1M;
            this.outputPer1M = outputPer1M;
        }
        public double getInputPer1M() { return inputPer1M; }
        public double getOutputPer1M() { return outputPer1M; }
    }

    static {
        // OpenAI chat models
        put("gpt-4o-mini",               0.15,  0.60);
        put("gpt-4o",                    2.50, 10.00);
        put("gpt-4.1",                   3.00, 12.00);
        put("gpt-4.1-mini",              0.40,  1.60);

        // OpenAI embeddings (output 미사용)
        put("text-embedding-3-small",    0.02,  0.00);
        put("text-embedding-3-large",    0.13,  0.00);
        put("text-embedding-ada-002",    0.10,  0.00);

        // Gemini (Google AI Studio / paid)
        put("gemini-1.5-flash",          0.075, 0.30);
        put("gemini-2.5-flash",          0.075, 0.30);
        put("gemini-1.5-pro",            1.25,  5.00);

        // Gemini embeddings — 무료 tier 기본 0
        put("text-embedding-004",        0.00,  0.00);
        put("embedding-001",             0.00,  0.00);
    }

    private CostTable() {}

    /**
     * 런타임 단가 등록 / 갱신. 동일 이름 존재 시 교체.
     *
     * @param model 모델 이름
     * @param inputPer1M USD per 1M input tokens
     * @param outputPer1M USD per 1M output tokens (embedding 처럼 미사용이면 0)
     */
    public static synchronized void register(String model, double inputPer1M, double outputPer1M) {
        if (model == null || model.isEmpty()) {
            throw new IllegalArgumentException("model is required");
        }
        TABLE.put(model, new Entry(inputPer1M, outputPer1M));
    }

    private static void put(String model, double in, double out) {
        TABLE.put(model, new Entry(in, out));
    }

    /**
     * 단가 조회. 미등록 모델은 {@code null} 반환.
     */
    public static synchronized Entry lookup(String model) {
        if (model == null) return null;
        return TABLE.get(model);
    }

    /**
     * 비용 계산. 미등록 모델은 0 반환 + WARN 로그.
     */
    public static double calculate(String model, int inputTokens, int outputTokens) {
        Entry e = lookup(model);
        if (e == null) {
            log.warn("CostTable: 미등록 모델 '{}' — cost=0 으로 기록. CostTable.register() 로 등록 가능.", model);
            return 0.0;
        }
        double cost = (inputTokens / 1_000_000.0) * e.inputPer1M
                + (outputTokens / 1_000_000.0) * e.outputPer1M;
        return cost;
    }

    /**
     * 전체 등록된 단가 스냅샷 (테스트/디버깅용).
     */
    public static synchronized Map<String, Entry> snapshot() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(TABLE));
    }
}

package com.smartuxapi.ai.cost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * API 호출 비용 누적 기록.
 *
 * <p>싱글톤 {@link #INSTANCE} + 독립 인스턴스 생성(세션별/테스트용) 모두 지원.
 * 기본 동작은 항상 켜져 있으며 {@link #setEnabled(boolean)} 로 off 가능.
 *
 * <p>내부적으로 Deque 로 저장. {@link #setMaxEntries(int)} 로 FIFO ring buffer 전환 가능.
 *
 * <p>Thread-safe — 모든 변경 메서드 synchronized.
 *
 * @since 0.9.1
 */
public final class CostTracker {

    /** 전역 싱글톤. 대부분의 호출 경로에서 자동으로 사용. */
    public static final CostTracker INSTANCE = new CostTracker();

    private final Deque<CostEntry> entries = new LinkedList<>();
    private volatile boolean enabled = true;
    private int maxEntries = 0;   // 0 = unbounded

    public CostTracker() {}

    public synchronized void record(CostEntry entry) {
        if (!enabled || entry == null) return;
        entries.addLast(entry);
        if (maxEntries > 0) {
            while (entries.size() > maxEntries) {
                entries.removeFirst();
            }
        }
    }

    /**
     * 현재까지 쌓인 entry 들의 스냅샷 (시간 순).
     */
    public synchronized List<CostEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized int size() {
        return entries.size();
    }

    public CostSummary getSummary() {
        return getSummary(null);
    }

    /**
     * 필터링된 집계. filter 가 null 이면 전체.
     */
    public synchronized CostSummary getSummary(Predicate<CostEntry> filter) {
        int totalIn = 0;
        int totalOut = 0;
        double totalUsd = 0.0;
        int count = 0;
        int fallbackCount = 0;
        Map<String, Double> byProvider = new HashMap<>();
        Map<String, Double> byCallKind = new HashMap<>();

        for (CostEntry e : entries) {
            if (filter != null && !filter.test(e)) continue;
            count++;
            totalIn += e.getInputTokens();
            totalOut += e.getOutputTokens();
            totalUsd += e.getUsdCost();
            if (e.isFallbackTriggered()) fallbackCount++;
            byProvider.merge(e.getProvider(), e.getUsdCost(), Double::sum);
            byCallKind.merge(e.getCallKind(), e.getUsdCost(), Double::sum);
        }
        return new CostSummary(totalIn, totalOut, totalUsd, count, fallbackCount,
                byProvider, byCallKind);
    }

    public synchronized void reset() {
        entries.clear();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 최대 보관 수 설정 — 초과 시 FIFO 로 가장 오래된 entry 제거. 0 = 무한.
     */
    public synchronized void setMaxEntries(int max) {
        this.maxEntries = Math.max(0, max);
        while (maxEntries > 0 && entries.size() > maxEntries) {
            entries.removeFirst();
        }
    }

    public int getMaxEntries() {
        return maxEntries;
    }
}

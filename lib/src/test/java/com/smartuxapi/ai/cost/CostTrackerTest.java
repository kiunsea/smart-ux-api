package com.smartuxapi.ai.cost;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CostTracker + CostSummary 단위 테스트")
class CostTrackerTest {

    @Test
    @DisplayName("INSTANCE 싱글톤 존재")
    void testSingleton() {
        assertNotNull(CostTracker.INSTANCE);
        assertSame(CostTracker.INSTANCE, CostTracker.INSTANCE);
    }

    @Test
    @DisplayName("record + getSummary — 합계/카운트/fallback 집계")
    void testRecordAndSummary() {
        CostTracker t = new CostTracker();
        t.record(new CostEntry("openai", "gpt-4o-mini", 100, 50, 0.0001, false, "chat"));
        t.record(new CostEntry("openai", "gpt-4o-mini", 200, 100, 0.0002, true, "chat"));
        t.record(new CostEntry("gemini", "gemini-1.5-flash", 300, 150, 0.00005, false, "vision"));

        CostSummary s = t.getSummary();
        assertEquals(600, s.getTotalInputTokens());
        assertEquals(300, s.getTotalOutputTokens());
        assertEquals(0.00035, s.getTotalUsd(), 1e-9);
        assertEquals(3, s.getEntryCount());
        assertEquals(1, s.getFallbackCount());
        assertEquals(0.0003, s.getByProvider().get("openai"), 1e-9);
        assertEquals(0.00005, s.getByProvider().get("gemini"), 1e-9);
        assertEquals(0.0003, s.getByCallKind().get("chat"), 1e-9);
        assertEquals(0.00005, s.getByCallKind().get("vision"), 1e-9);
    }

    @Test
    @DisplayName("getSummary(filter) — predicate 적용")
    void testFilteredSummary() {
        CostTracker t = new CostTracker();
        t.record(new CostEntry("openai", "a", 100, 0, 0.01, false, "chat"));
        t.record(new CostEntry("gemini", "b", 200, 0, 0.02, false, "chat"));

        CostSummary openaiOnly = t.getSummary(e -> "openai".equals(e.getProvider()));
        assertEquals(1, openaiOnly.getEntryCount());
        assertEquals(0.01, openaiOnly.getTotalUsd(), 1e-9);
    }

    @Test
    @DisplayName("setEnabled(false) — record 무시")
    void testDisable() {
        CostTracker t = new CostTracker();
        t.setEnabled(false);
        t.record(new CostEntry("o", "m", 100, 50, 0.001, false, "chat"));
        assertEquals(0, t.size());
        assertFalse(t.isEnabled());

        t.setEnabled(true);
        t.record(new CostEntry("o", "m", 100, 50, 0.001, false, "chat"));
        assertEquals(1, t.size());
    }

    @Test
    @DisplayName("reset — entries 클리어")
    void testReset() {
        CostTracker t = new CostTracker();
        t.record(new CostEntry("o", "m", 100, 50, 0.001, false, "chat"));
        assertEquals(1, t.size());
        t.reset();
        assertEquals(0, t.size());
    }

    @Test
    @DisplayName("setMaxEntries — FIFO ring buffer")
    void testMaxEntries() {
        CostTracker t = new CostTracker();
        t.setMaxEntries(3);
        for (int i = 0; i < 5; i++) {
            t.record(new CostEntry("o", "m", i, 0, 0.0, false, "chat"));
        }
        assertEquals(3, t.size(), "가장 오래된 2건 제거");
        // 남은 entry 의 inputTokens 는 2, 3, 4
        assertEquals(2, t.getEntries().get(0).getInputTokens());
        assertEquals(4, t.getEntries().get(2).getInputTokens());
    }

    @Test
    @DisplayName("setMaxEntries(0) — 무제한")
    void testMaxEntriesUnbounded() {
        CostTracker t = new CostTracker();
        t.setMaxEntries(0);
        for (int i = 0; i < 100; i++) {
            t.record(new CostEntry("o", "m", i, 0, 0.0, false, "chat"));
        }
        assertEquals(100, t.size());
    }

    @Test
    @DisplayName("getEntries — 불변 리스트")
    void testEntriesImmutable() {
        CostTracker t = new CostTracker();
        t.record(new CostEntry("o", "m", 1, 0, 0.0, false, "chat"));
        assertThrows(UnsupportedOperationException.class,
                () -> t.getEntries().add(new CostEntry("o", "m", 1, 0, 0.0, false, "chat")));
    }

    @Test
    @DisplayName("record(null) / null CostEntry — 안전하게 무시")
    void testNullIgnored() {
        CostTracker t = new CostTracker();
        t.record(null);
        assertEquals(0, t.size());
    }
}

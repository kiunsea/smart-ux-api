package com.smartuxapi.ai.cost;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CostTable 단위 테스트")
class CostTableTest {

    @Test
    @DisplayName("하드코딩된 기본 OpenAI 모델 단가 조회")
    void testBuiltInOpenAi() {
        CostTable.Entry e = CostTable.lookup("gpt-4o-mini");
        assertNotNull(e);
        assertEquals(0.15, e.getInputPer1M());
        assertEquals(0.60, e.getOutputPer1M());
    }

    @Test
    @DisplayName("하드코딩된 기본 Gemini 모델 단가 조회")
    void testBuiltInGemini() {
        CostTable.Entry e = CostTable.lookup("gemini-1.5-flash");
        assertNotNull(e);
        assertEquals(0.075, e.getInputPer1M());
        assertEquals(0.30, e.getOutputPer1M());
    }

    @Test
    @DisplayName("Embedding 모델 — output 단가 0")
    void testEmbeddingOutputZero() {
        CostTable.Entry e = CostTable.lookup("text-embedding-3-small");
        assertNotNull(e);
        assertEquals(0.0, e.getOutputPer1M());
    }

    @Test
    @DisplayName("미등록 모델 → null")
    void testUnknown() {
        assertNull(CostTable.lookup("nonexistent-model"));
        assertNull(CostTable.lookup(null));
    }

    @Test
    @DisplayName("calculate — 토큰 → USD 변환")
    void testCalculate() {
        // gpt-4o-mini: 0.15/1M input, 0.60/1M output
        // 1M input + 1M output = 0.15 + 0.60 = 0.75
        assertEquals(0.75, CostTable.calculate("gpt-4o-mini", 1_000_000, 1_000_000), 1e-9);
        // 1000 input = 0.00015, 500 output = 0.00030 → 0.00045
        assertEquals(0.00045, CostTable.calculate("gpt-4o-mini", 1000, 500), 1e-9);
    }

    @Test
    @DisplayName("calculate — 미등록 모델은 0 반환")
    void testCalculateUnknown() {
        assertEquals(0.0, CostTable.calculate("nonexistent", 10000, 5000));
    }

    @Test
    @DisplayName("register — 런타임 override 후 조회")
    void testRegister() {
        CostTable.register("custom-model-v1", 2.0, 10.0);
        try {
            CostTable.Entry e = CostTable.lookup("custom-model-v1");
            assertNotNull(e);
            assertEquals(2.0, e.getInputPer1M());
            assertEquals(10.0, e.getOutputPer1M());

            // 1M + 1M = 12.0
            assertEquals(12.0, CostTable.calculate("custom-model-v1", 1_000_000, 1_000_000), 1e-9);
        } finally {
            // cleanup — 다른 테스트와 격리
        }
    }

    @Test
    @DisplayName("register — null / 빈 모델 이름 거부")
    void testRegisterInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> CostTable.register(null, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> CostTable.register("", 1, 1));
    }

    @Test
    @DisplayName("snapshot — 등록된 엔트리 불변 스냅샷")
    void testSnapshot() {
        int originalSize = CostTable.snapshot().size();
        assertTrue(originalSize >= 10, "기본 제공 모델 >= 10");
        assertThrows(UnsupportedOperationException.class,
                () -> CostTable.snapshot().put("x", new CostTable.Entry(1, 1)));
    }
}

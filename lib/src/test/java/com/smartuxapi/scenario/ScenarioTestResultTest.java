package com.smartuxapi.scenario;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ScenarioTestResult / TurnTestResult 단위 테스트")
class ScenarioTestResultTest {

    @Test
    @DisplayName("TurnTestResult.pass 생성 + 상태 확인")
    void testPass() {
        TurnTestResult t = TurnTestResult.pass(1, "exp", "act", 250L);
        assertEquals(TurnTestResult.Status.PASS, t.getStatus());
        assertTrue(t.isPass());
        assertEquals(1, t.getTurnNo());
        assertEquals(250L, t.getElapsedMs());
        assertNotNull(t.getValidationResult());
        assertTrue(t.getValidationResult().isExactMatch());
    }

    @Test
    @DisplayName("TurnTestResult.fail / error / skipped")
    void testNonPass() {
        ValidationResult vr = ValidationResult.of(java.util.Collections.singletonList(
                new ActionDiff("/x", ActionDiff.Kind.VALUE_DIFFERS, "1", "2")));
        TurnTestResult fail = TurnTestResult.fail(2, vr, "{}", "{}", 100L);
        assertEquals(TurnTestResult.Status.FAIL, fail.getStatus());
        assertEquals(1, fail.getValidationResult().diffCount());

        TurnTestResult err = TurnTestResult.error(3, "boom", 50L);
        assertEquals(TurnTestResult.Status.ERROR_RUNTIME, err.getStatus());
        assertEquals("boom", err.getErrorMessage());

        TurnTestResult sk = TurnTestResult.skipped(4, "no api key");
        assertEquals(TurnTestResult.Status.SKIPPED, sk.getStatus());
        assertEquals("no api key", sk.getErrorMessage());
    }

    @Test
    @DisplayName("ScenarioTestResult — 집계 카운터")
    void testAggregation() {
        TurnTestResult p1 = TurnTestResult.pass(1, "", "", 100L);
        TurnTestResult p2 = TurnTestResult.pass(2, "", "", 200L);
        ValidationResult vr = ValidationResult.of(java.util.Collections.singletonList(
                new ActionDiff("/x", ActionDiff.Kind.VALUE_DIFFERS, "a", "b")));
        TurnTestResult f3 = TurnTestResult.fail(3, vr, "", "", 150L);
        TurnTestResult e4 = TurnTestResult.error(4, "x", 10L);
        TurnTestResult s5 = TurnTestResult.skipped(5, "skip");

        ScenarioTestResult r = new ScenarioTestResult("file.json", "sess1", "chatgpt",
                Arrays.asList(p1, p2, f3, e4, s5));
        assertEquals(5, r.total());
        assertEquals(2, r.passed());
        assertEquals(1, r.failed());
        assertEquals(1, r.errored());
        assertEquals(1, r.skipped());
        assertFalse(r.isAllPassed());
        assertEquals(460L, r.totalElapsedMs());
    }

    @Test
    @DisplayName("isAllPassed — 모두 PASS 일 때만 true (skipped 무관)")
    void testAllPassed() {
        ScenarioTestResult ok = new ScenarioTestResult("f", "s", "m",
                Arrays.asList(TurnTestResult.pass(1, "", "", 0L)));
        assertTrue(ok.isAllPassed());

        ScenarioTestResult empty = new ScenarioTestResult("f", "s", "m",
                java.util.Collections.emptyList());
        assertFalse(empty.isAllPassed(), "빈 결과는 통과 아님");
    }
}

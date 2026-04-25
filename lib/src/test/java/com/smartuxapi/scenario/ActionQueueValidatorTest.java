package com.smartuxapi.scenario;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionQueueValidator 단위 테스트")
class ActionQueueValidatorTest {

    private static final ObjectMapper M = new ObjectMapper();

    private JsonNode parse(String json) throws Exception {
        return M.readTree(json);
    }

    @Test
    @DisplayName("동일 객체 — exactMatch")
    void testExactMatch() throws Exception {
        JsonNode a = parse("{\"actions\":[{\"type\":\"click\",\"target\":\"x\"}]}");
        JsonNode b = parse("{\"actions\":[{\"type\":\"click\",\"target\":\"x\"}]}");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        assertTrue(r.isExactMatch());
        assertEquals(0, r.diffCount());
    }

    @Test
    @DisplayName("scalar 값 차이 → VALUE_DIFFERS")
    void testValueDiffers() throws Exception {
        JsonNode a = parse("{\"actions\":[{\"type\":\"click\"}]}");
        JsonNode b = parse("{\"actions\":[{\"type\":\"tap\"}]}");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        assertFalse(r.isExactMatch());
        List<ActionDiff> d = r.getDiffs();
        assertEquals(1, d.size());
        assertEquals(ActionDiff.Kind.VALUE_DIFFERS, d.get(0).getKind());
        assertEquals("/actions/0/type", d.get(0).getPath());
    }

    @Test
    @DisplayName("expected 에 있는 key 가 actual 에 없음 → EXPECTED_MISSING")
    void testExpectedMissing() throws Exception {
        JsonNode a = parse("{\"actions\":[{\"type\":\"click\",\"meta\":{\"x\":1}}]}");
        JsonNode b = parse("{\"actions\":[{\"type\":\"click\"}]}");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        assertEquals(1, r.diffCount());
        assertEquals(ActionDiff.Kind.EXPECTED_MISSING, r.getDiffs().get(0).getKind());
        assertEquals("/actions/0/meta", r.getDiffs().get(0).getPath());
    }

    @Test
    @DisplayName("actual 에만 있는 key → ACTUAL_EXTRA")
    void testActualExtra() throws Exception {
        JsonNode a = parse("{\"actions\":[{\"type\":\"click\"}]}");
        JsonNode b = parse("{\"actions\":[{\"type\":\"click\",\"reason\":\"explained\"}]}");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        assertEquals(1, r.diffCount());
        assertEquals(ActionDiff.Kind.ACTUAL_EXTRA, r.getDiffs().get(0).getKind());
    }

    @Test
    @DisplayName("배열 길이 차 — 모자란 쪽은 EXPECTED_MISSING / ACTUAL_EXTRA")
    void testArrayLengthDiff() throws Exception {
        JsonNode a = parse("[1,2,3]");
        JsonNode b = parse("[1,2]");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        assertEquals(1, r.diffCount());
        assertEquals(ActionDiff.Kind.EXPECTED_MISSING, r.getDiffs().get(0).getKind());
        assertEquals("/2", r.getDiffs().get(0).getPath());
    }

    @Test
    @DisplayName("타입 불일치 → VALUE_DIFFERS")
    void testTypeMismatch() throws Exception {
        JsonNode a = parse("{\"x\": [1,2]}");
        JsonNode b = parse("{\"x\": \"text\"}");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        assertEquals(1, r.diffCount());
        assertEquals(ActionDiff.Kind.VALUE_DIFFERS, r.getDiffs().get(0).getKind());
    }

    @Test
    @DisplayName("expected null + actual 값 → ACTUAL_EXTRA at /")
    void testExpectedNullActualValue() throws Exception {
        ValidationResult r = ActionQueueValidator.validate(null, parse("{\"x\":1}"));
        assertEquals(1, r.diffCount());
        assertEquals(ActionDiff.Kind.ACTUAL_EXTRA, r.getDiffs().get(0).getKind());
        assertEquals("/", r.getDiffs().get(0).getPath());
    }

    @Test
    @DisplayName("둘 다 null/missing → exactMatch")
    void testBothNull() {
        assertTrue(ActionQueueValidator.validate(null, null).isExactMatch());
    }

    @Test
    @DisplayName("countByKind 집계")
    void testCountByKind() throws Exception {
        JsonNode a = parse("{\"actions\":[{\"type\":\"click\",\"target\":\"x\",\"extra\":1},{\"type\":\"submit\"}]}");
        JsonNode b = parse("{\"actions\":[{\"type\":\"tap\",\"target\":\"x\"}]}");
        ValidationResult r = ActionQueueValidator.validate(a, b);
        // /actions/0/type: VALUE_DIFFERS
        // /actions/0/extra: EXPECTED_MISSING
        // /actions/1: EXPECTED_MISSING
        assertTrue(r.diffCount() >= 2);
        assertTrue(r.countByKind(ActionDiff.Kind.VALUE_DIFFERS) >= 1);
        assertTrue(r.countByKind(ActionDiff.Kind.EXPECTED_MISSING) >= 1);
    }
}

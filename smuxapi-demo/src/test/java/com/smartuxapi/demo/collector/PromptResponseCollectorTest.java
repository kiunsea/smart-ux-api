package com.smartuxapi.demo.collector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PromptResponseCollector 단위 테스트")
class PromptResponseCollectorTest {

    private PromptResponseCollector newCollector(Path outputDir, boolean enabled) {
        PromptResponseCollector c = new PromptResponseCollector();
        ReflectionTestUtils.setField(c, "collectEnabled", enabled);
        ReflectionTestUtils.setField(c, "outputPath", outputDir.toString());
        ReflectionTestUtils.setField(c, "filePrefix", "unittest");
        return c;
    }

    @Test
    @DisplayName("collect-enabled=false 이면 capture 가 no-op")
    void testDisabled(@TempDir Path dir) throws Exception {
        PromptResponseCollector c = newCollector(dir, false);
        c.initSession("sess1", "chatgpt");
        c.startNewTurn();
        c.captureUiInfo("ui1");
        c.captureUserPrompt("hi");
        c.captureResponse("msg", null);
        assertEquals(0, c.turnCount());
        assertNull(c.saveToFile(), "disabled 상태는 파일 미생성");
    }

    @Test
    @DisplayName("기본 수집 플로우 — 3턴 기록 후 JSON 저장 + 필드 검증")
    void testBasicFlow(@TempDir Path dir) throws Exception {
        PromptResponseCollector c = newCollector(dir, true);
        c.initSession("sessABC", "gemini");

        // Turn 1
        c.startNewTurn();
        c.captureUiInfo("ui-info-1");
        c.captureApiPrompt("api-prompt-{userMsg}");
        c.captureUserPrompt("안녕");
        c.captureResponse("안녕하세요", "{\"action_queue\":[]}");

        // Turn 2
        c.startNewTurn();
        c.captureUserPrompt("주문해줘");
        c.captureResponse("주문 확인", null);

        // Turn 3
        c.startNewTurn();
        c.captureUserPrompt("취소");
        c.captureResponse("취소됨", "{\"x\":1}");

        assertEquals(3, c.turnCount());
        List<ScenarioTurn> snap = c.snapshotTurns();
        assertEquals("안녕", snap.get(0).getUserPrompt());
        assertEquals("ui-info-1", snap.get(0).getUiInfo());
        assertEquals(2, snap.get(1).getTurnNo());
        assertNull(snap.get(1).getActionQueue());

        Path saved = c.saveToFile();
        assertNotNull(saved);
        assertTrue(Files.exists(saved));
        assertTrue(saved.getFileName().toString().startsWith("unittest-sessABC-"));

        JsonNode root = new ObjectMapper().readTree(saved.toFile());
        assertEquals("sessABC", root.get("sessionId").asText());
        assertEquals("gemini", root.get("aiModel").asText());
        assertEquals(3, root.get("turnCount").asInt());
        JsonNode turns = root.get("turns");
        assertEquals(3, turns.size());
        assertEquals(1, turns.get(0).get("turnNo").asInt());
        assertEquals("안녕", turns.get(0).get("userPrompt").asText());
        assertEquals("{userMsg}",
                turns.get(0).get("apiPrompt").asText().substring("api-prompt-".length()));
        // action_queue JSON 파싱 확인
        assertTrue(turns.get(0).get("actionQueue").isObject());
        assertTrue(turns.get(2).get("actionQueue").isObject());
        assertEquals(1, turns.get(2).get("actionQueue").get("x").asInt());
        assertTrue(turns.get(1).get("actionQueue").isNull());
    }

    @Test
    @DisplayName("빈 상태 saveToFile → null (파일 미생성)")
    void testEmpty(@TempDir Path dir) throws IOException {
        PromptResponseCollector c = newCollector(dir, true);
        assertNull(c.saveToFile());
    }

    @Test
    @DisplayName("reset — 상태 초기화")
    void testReset(@TempDir Path dir) {
        PromptResponseCollector c = newCollector(dir, true);
        c.initSession("s", "m");
        c.startNewTurn();
        c.captureUserPrompt("x");
        c.captureResponse("y", null);
        assertEquals(1, c.turnCount());
        c.reset();
        assertEquals(0, c.turnCount());
        assertNull(c.getSessionId());
    }

    @Test
    @DisplayName("sessionId 특수문자 → 안전 문자로 변환")
    void testSessionIdSanitize(@TempDir Path dir) throws Exception {
        PromptResponseCollector c = newCollector(dir, true);
        c.initSession("/session?with:unsafe", "chatgpt");
        c.startNewTurn();
        c.captureUserPrompt("x");
        c.captureResponse("y", null);
        Path saved = c.saveToFile();
        assertNotNull(saved);
        String fname = saved.getFileName().toString();
        assertFalse(fname.contains("/"));
        assertFalse(fname.contains("?"));
        assertFalse(fname.contains(":"));
    }
}

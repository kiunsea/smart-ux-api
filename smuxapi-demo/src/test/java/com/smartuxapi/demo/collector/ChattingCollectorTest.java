package com.smartuxapi.demo.collector;

import java.nio.file.Path;
import java.util.List;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.smartuxapi.ai.Chatting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ChattingCollector Decorator 단위 테스트")
class ChattingCollectorTest {

    private static PromptResponseCollector newCollector(Path dir) {
        PromptResponseCollector c = new PromptResponseCollector();
        ReflectionTestUtils.setField(c, "collectEnabled", true);
        ReflectionTestUtils.setField(c, "outputPath", dir.toString());
        ReflectionTestUtils.setField(c, "filePrefix", "cc");
        return c;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject resp(String msg, Object aq) {
        JSONObject r = new JSONObject();
        r.put("message", msg);
        r.put("action_queue", aq);
        return r;
    }

    @Test
    @DisplayName("sendPrompt — userMsg / message / action_queue 캡처")
    void testSendPromptCapture(@TempDir Path dir) throws Exception {
        PromptResponseCollector collector = newCollector(dir);
        collector.initSession("s1", "openai");

        Chatting delegate = mock(Chatting.class);
        when(delegate.sendPrompt("hi")).thenReturn(resp("hello", "{\"a\":1}"));

        ChattingCollector cc = new ChattingCollector(delegate, collector);
        JSONObject out = cc.sendPrompt("hi");

        assertSame("hello", out.get("message"));
        verify(delegate).sendPrompt("hi");

        List<ScenarioTurn> turns = collector.snapshotTurns();
        assertEquals(1, turns.size());
        assertEquals("hi", turns.get(0).getUserPrompt());
        assertEquals("hello", turns.get(0).getResMsg());
        assertEquals("{\"a\":1}", turns.get(0).getActionQueue());
    }

    @Test
    @DisplayName("collect-enabled=false 상태에서는 호출만 위임, 기록 없음")
    void testDisabledPassthrough(@TempDir Path dir) throws Exception {
        PromptResponseCollector collector = newCollector(dir);
        ReflectionTestUtils.setField(collector, "collectEnabled", false);

        Chatting delegate = mock(Chatting.class);
        when(delegate.sendPrompt("x")).thenReturn(resp("y", null));

        ChattingCollector cc = new ChattingCollector(delegate, collector);
        cc.sendPrompt("x");

        assertEquals(0, collector.turnCount());
        verify(delegate).sendPrompt("x");
    }

    @Test
    @DisplayName("delegate 예외는 그대로 전파 (turn 은 미완성 상태로 남음)")
    void testExceptionPropagation(@TempDir Path dir) throws Exception {
        PromptResponseCollector collector = newCollector(dir);
        collector.initSession("s", "x");

        Chatting delegate = mock(Chatting.class);
        when(delegate.sendPrompt("x")).thenThrow(new RuntimeException("boom"));

        ChattingCollector cc = new ChattingCollector(delegate, collector);
        assertThrows(RuntimeException.class, () -> cc.sendPrompt("x"));
        // 예외 발생 시 captureResponse 는 호출되지 않음 → turns 는 0
        assertEquals(0, collector.turnCount());
    }

    @Test
    @DisplayName("getDelegate — 내부 참조 노출")
    void testGetDelegate(@TempDir Path dir) {
        PromptResponseCollector collector = newCollector(dir);
        Chatting delegate = mock(Chatting.class);
        ChattingCollector cc = new ChattingCollector(delegate, collector);
        assertSame(delegate, cc.getDelegate());
    }
}

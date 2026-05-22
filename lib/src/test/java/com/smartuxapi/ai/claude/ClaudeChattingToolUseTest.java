package com.smartuxapi.ai.claude;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.schema.SchemaBuilder;
import com.smartuxapi.ai.tools.ToolCall;
import com.smartuxapi.ai.tools.ToolDefinition;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolResult;
import com.smartuxapi.ai.tools.ToolTurnResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ClaudeChatting 의 Tool Use 경로 Mockito 단위 테스트. 네트워크 호출은 차단.
 */
@DisplayName("ClaudeChatting — Tool Use 단위 테스트")
class ClaudeChattingToolUseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Anthropic tool_use content 블록 1개를 담은 raw assistant payload. */
    private static String rawToolUse(String id, String name) {
        return "[{\"type\":\"tool_use\",\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"input\":{}}]";
    }

    private ToolDefinition simpleEchoTool(String name) {
        return new ToolDefinition(name, "echo",
                SchemaBuilder.object().stringProperty("value", null).build(),
                call -> {
                    ObjectNode out = MAPPER.createObjectNode();
                    out.put("echo", call.getArguments().path("value").asText("x"));
                    return ToolResult.ok(call.getId(), out);
                });
    }

    @Test
    @DisplayName("tools=null — sendPrompt 로 위임 (tool_calls 키 없음)")
    void testNullToolsDelegates() throws Exception {
        ClaudeAPIConnection mockConn = mock(ClaudeAPIConnection.class);
        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), any()))
                .thenReturn("plain reply");

        ClaudeChatting chatting = new ClaudeChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("hi", null);

        assertEquals("plain reply", res.get("message"));
        assertFalse(res.containsKey("tool_calls"));
    }

    @Test
    @DisplayName("1-round auto loop — LLM 이 바로 최종 응답")
    void testFinalOnFirstRound() throws Exception {
        ClaudeAPIConnection mockConn = mock(ClaudeAPIConnection.class);
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.finalText("answer", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ClaudeChatting chatting = new ClaudeChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("hi", tools);

        assertEquals("answer", res.get("message"));
        org.json.simple.JSONArray calls = (org.json.simple.JSONArray) res.get("tool_calls");
        assertTrue(calls.isEmpty());
        verify(mockConn, times(1))
                .generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class));
    }

    @Test
    @DisplayName("2-round auto loop — tool 호출 실행 후 최종 응답")
    void testOneToolCallThenFinal() throws Exception {
        ClaudeAPIConnection mockConn = mock(ClaudeAPIConnection.class);
        ToolCall call = new ToolCall("toolu_1", "echo", MAPPER.readTree("{\"value\":\"hello\"}"));

        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), rawToolUse("toolu_1", "echo")))
                .thenReturn(ToolTurnResult.finalText("final-after-tool", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ClaudeChatting chatting = new ClaudeChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("please call echo", tools);

        assertEquals("final-after-tool", res.get("message"));
        org.json.simple.JSONArray calls = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, calls.size(), "1 tool 호출이 기록되어야 함");
        verify(mockConn, times(2))
                .generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class));
    }

    @Test
    @DisplayName("등록되지 않은 tool 호출 — error ToolResult 로 LLM 에 피드백")
    void testUnregisteredToolBecomesError() throws Exception {
        ClaudeAPIConnection mockConn = mock(ClaudeAPIConnection.class);
        ToolCall call = new ToolCall("toolu_x", "nonexistent", MAPPER.createObjectNode());

        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), rawToolUse("toolu_x", "nonexistent")))
                .thenReturn(ToolTurnResult.finalText("recovered", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("other"));

        ClaudeChatting chatting = new ClaudeChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("call nonexistent", tools);

        assertEquals("recovered", res.get("message"));
        org.json.simple.JSONArray calls = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, calls.size());
        JSONObject exec = (JSONObject) calls.get(0);
        assertTrue((Boolean) exec.get("isError"), "등록되지 않은 tool 호출은 isError=true");
    }

    @Test
    @DisplayName("Manual mode — sendPromptExpectingToolCalls 는 첫 라운드 tool_calls 그대로 반환")
    void testManualFirstRound() throws Exception {
        ClaudeAPIConnection mockConn = mock(ClaudeAPIConnection.class);
        ToolCall call = new ToolCall("toolu_2", "echo", MAPPER.readTree("{\"value\":\"x\"}"));
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), rawToolUse("toolu_2", "echo")));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ClaudeChatting chatting = new ClaudeChatting(mockConn);
        JSONObject res = chatting.sendPromptExpectingToolCalls("go", tools);

        assertNull(res.get("message"));
        assertEquals(Boolean.TRUE, res.get("pending"));
        org.json.simple.JSONArray arr = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, arr.size());
    }

    @Test
    @DisplayName("Manual mode — continueWithToolResults 가 pending 을 끝내고 최종 응답 반환")
    void testManualContinuation() throws Exception {
        ClaudeAPIConnection mockConn = mock(ClaudeAPIConnection.class);
        ToolCall call = new ToolCall("toolu_3", "echo", MAPPER.readTree("{\"value\":\"x\"}"));
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), rawToolUse("toolu_3", "echo")))
                .thenReturn(ToolTurnResult.finalText("ok!", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ClaudeChatting chatting = new ClaudeChatting(mockConn);
        JSONObject pending = chatting.sendPromptExpectingToolCalls("go", tools);
        assertEquals(Boolean.TRUE, pending.get("pending"));

        ToolResult userResult = ToolResult.ok("toolu_3", MAPPER.readTree("{\"echo\":\"x\"}"));
        JSONObject done = chatting.continueWithToolResults(Arrays.asList(userResult), tools);
        assertEquals("ok!", done.get("message"));
        assertFalse(done.containsKey("pending"));
    }
}

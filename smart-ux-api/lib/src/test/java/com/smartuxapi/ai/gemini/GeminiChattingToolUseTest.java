package com.smartuxapi.ai.gemini;

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
 * Gemini GeminiChatting 의 Tool Use 경로 Mockito 테스트.
 * OpenAI 쪽 {@code ResponsesChattingToolUseTest} 와 대칭.
 */
@DisplayName("GeminiChatting — Tool Use 단위 테스트")
class GeminiChattingToolUseTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), any()))
                .thenReturn("plain reply");

        GeminiChatting chatting = new GeminiChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("hi", null);

        assertEquals("plain reply", res.get("message"));
        assertFalse(res.containsKey("tool_calls"));
    }

    @Test
    @DisplayName("1-round auto loop — LLM 이 바로 최종 응답")
    void testFinalOnFirstRound() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.finalText("answer", "{\"role\":\"model\",\"parts\":[{\"text\":\"answer\"}]}"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        GeminiChatting chatting = new GeminiChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("hi", tools);

        assertEquals("answer", res.get("message"));
        org.json.simple.JSONArray calls = (org.json.simple.JSONArray) res.get("tool_calls");
        assertTrue(calls.isEmpty());
        verify(mockConn, times(1))
                .generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class));
    }

    @Test
    @DisplayName("2-round auto loop — functionCall 실행 후 최종 응답")
    void testOneToolCallThenFinal() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        ToolCall call = new ToolCall("gemini-call-1", "echo",
                MAPPER.readTree("{\"value\":\"hello\"}"));
        String rawRound1 = "{\"role\":\"model\",\"parts\":[{\"functionCall\":{\"name\":\"echo\",\"args\":{\"value\":\"hello\"}}}]}";

        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), rawRound1))
                .thenReturn(ToolTurnResult.finalText("final-after-tool",
                        "{\"role\":\"model\",\"parts\":[{\"text\":\"final-after-tool\"}]}"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        GeminiChatting chatting = new GeminiChatting(mockConn);
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
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        ToolCall call = new ToolCall("gemini-call-x", "nonexistent", MAPPER.createObjectNode());
        String raw = "{\"role\":\"model\",\"parts\":[{\"functionCall\":{\"name\":\"nonexistent\",\"args\":{}}}]}";

        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), raw))
                .thenReturn(ToolTurnResult.finalText("recovered",
                        "{\"role\":\"model\",\"parts\":[{\"text\":\"recovered\"}]}"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("other"));

        GeminiChatting chatting = new GeminiChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("call nonexistent", tools);

        assertEquals("recovered", res.get("message"));
        org.json.simple.JSONArray calls = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, calls.size());
        JSONObject exec = (JSONObject) calls.get(0);
        assertTrue((Boolean) exec.get("isError"), "등록되지 않은 tool 호출은 isError=true");
    }

    @Test
    @DisplayName("Manual mode — sendPromptExpectingToolCalls 가 첫 라운드 tool_calls 그대로 반환")
    void testManualFirstRound() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        ToolCall call = new ToolCall("gemini-call-2", "echo", MAPPER.readTree("{\"value\":\"x\"}"));
        String raw = "{\"role\":\"model\",\"parts\":[{\"functionCall\":{\"name\":\"echo\",\"args\":{\"value\":\"x\"}}}]}";
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), raw));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        GeminiChatting chatting = new GeminiChatting(mockConn);
        JSONObject res = chatting.sendPromptExpectingToolCalls("go", tools);

        assertNull(res.get("message"));
        assertEquals(Boolean.TRUE, res.get("pending"));
        org.json.simple.JSONArray arr = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, arr.size());
    }

    @Test
    @DisplayName("Manual mode — continueWithToolResults 가 pending 을 끝내고 최종 응답 반환")
    void testManualContinuation() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        ToolCall call = new ToolCall("gemini-call-3", "echo", MAPPER.readTree("{\"value\":\"x\"}"));
        String raw = "{\"role\":\"model\",\"parts\":[{\"functionCall\":{\"name\":\"echo\",\"args\":{\"value\":\"x\"}}}]}";
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), raw))
                .thenReturn(ToolTurnResult.finalText("ok!",
                        "{\"role\":\"model\",\"parts\":[{\"text\":\"ok!\"}]}"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        GeminiChatting chatting = new GeminiChatting(mockConn);
        JSONObject pending = chatting.sendPromptExpectingToolCalls("go", tools);
        assertEquals(Boolean.TRUE, pending.get("pending"));

        ToolResult userResult = ToolResult.ok("gemini-call-3", MAPPER.readTree("{\"echo\":\"x\"}"));
        JSONObject done = chatting.continueWithToolResults(Arrays.asList(userResult), tools);
        assertEquals("ok!", done.get("message"));
        assertFalse(done.containsKey("pending"));
    }
}

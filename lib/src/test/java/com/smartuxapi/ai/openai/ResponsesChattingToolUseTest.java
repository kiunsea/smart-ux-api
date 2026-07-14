package com.smartuxapi.ai.openai;

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
 * OpenAI ResponsesChatting 의 Tool Use 경로 Mockito 테스트.
 */
@DisplayName("ResponsesChatting — Tool Use 단위 테스트")
class ResponsesChattingToolUseTest {

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
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), any()))
                .thenReturn("plain reply");

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("hi", null);

        assertEquals("plain reply", res.get("message"));
        assertFalse(res.containsKey("tool_calls"));
    }

    @Test
    @DisplayName("1-round auto loop — LLM 이 바로 최종 응답")
    void testFinalOnFirstRound() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.finalText("answer", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
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
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ToolCall call = new ToolCall("call_1", "echo",
                MAPPER.readTree("{\"value\":\"hello\"}"));
        String rawOutputRound1 = "[{\"type\":\"function_call\",\"call_id\":\"call_1\",\"name\":\"echo\",\"arguments\":\"{\\\"value\\\":\\\"hello\\\"}\"}]";

        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), rawOutputRound1))
                .thenReturn(ToolTurnResult.finalText("final-after-tool", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
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
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ToolCall call = new ToolCall("call_x", "nonexistent", MAPPER.createObjectNode());
        String raw = "[{\"type\":\"function_call\",\"call_id\":\"call_x\",\"name\":\"nonexistent\",\"arguments\":\"{}\"}]";

        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), raw))
                .thenReturn(ToolTurnResult.finalText("recovered", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("other"));

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("call nonexistent", tools);

        assertEquals("recovered", res.get("message"));
        org.json.simple.JSONArray calls = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, calls.size());
        JSONObject exec = (JSONObject) calls.get(0);
        assertTrue((Boolean) exec.get("isError"), "등록되지 않은 tool 호출은 isError=true");
    }

    @Test
    @DisplayName("orphan function_call — 실행되지 않은 function_call 에 오류 출력 보정 (OpenAI 400 방지)")
    void testOrphanFunctionCallGetsErrorOutput() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ToolCall valid = new ToolCall("call_ok", "echo", MAPPER.readTree("{\"value\":\"hi\"}"));
        // rawOutput 에는 실행 대상 call_ok 외에, name 이 비어 파싱 단계에서 스킵된 call_orphan 도 포함.
        // 이는 elandmall 대형 describe 출력에서 관측된 고아 function_call 을 재현한다.
        String rawWithOrphan = "["
                + "{\"type\":\"function_call\",\"call_id\":\"call_ok\",\"name\":\"echo\",\"arguments\":\"{\\\"value\\\":\\\"hi\\\"}\"},"
                + "{\"type\":\"function_call\",\"call_id\":\"call_orphan\",\"name\":\"\",\"arguments\":\"{}\"}"
                + "]";
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(valid), rawWithOrphan))
                .thenReturn(ToolTurnResult.finalText("done", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        JSONObject res = chatting.sendPromptWithTools("go", tools);

        assertEquals("done", res.get("message"));
        // 히스토리에 보존된 두 function_call 모두 대응 function_call_output 을 가져야 한다.
        JSONArray history = chatting.getConversationHistory().getHistory();
        assertTrue(hasToolOutput(history, "call_ok"), "정상 call 은 출력을 보유해야 함");
        assertTrue(hasToolOutput(history, "call_orphan"),
                "고아 call 도 오류 출력으로 보정되어 다음 요청의 400 을 방지해야 함");
    }

    /** 히스토리에 주어진 call_id 의 function_call_output 이 존재하는지 확인. */
    private static boolean hasToolOutput(JSONArray history, String callId) {
        for (int i = 0; i < history.length(); i++) {
            org.json.JSONObject item = history.optJSONObject(i);
            if (item == null) continue;
            if ("function_call_output".equals(item.optString("type", ""))
                    && callId.equals(item.optString("call_id", ""))) {
                return true;
            }
        }
        return false;
    }

    @Test
    @DisplayName("Manual mode — sendPromptExpectingToolCalls 는 첫 라운드 tool_calls 그대로 반환")
    void testManualFirstRound() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ToolCall call = new ToolCall("call_2", "echo", MAPPER.readTree("{\"value\":\"x\"}"));
        String raw = "[{\"type\":\"function_call\",\"call_id\":\"call_2\",\"name\":\"echo\",\"arguments\":\"{}\"}]";
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), raw));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        JSONObject res = chatting.sendPromptExpectingToolCalls("go", tools);

        assertNull(res.get("message"));
        assertEquals(Boolean.TRUE, res.get("pending"));
        org.json.simple.JSONArray arr = (org.json.simple.JSONArray) res.get("tool_calls");
        assertEquals(1, arr.size());
    }

    @Test
    @DisplayName("Manual mode — continueWithToolResults 가 pending 을 끝내고 최종 응답 반환")
    void testManualContinuation() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ToolCall call = new ToolCall("call_3", "echo", MAPPER.readTree("{\"value\":\"x\"}"));
        String raw = "[{\"type\":\"function_call\",\"call_id\":\"call_3\",\"name\":\"echo\",\"arguments\":\"{}\"}]";
        when(mockConn.generateContentWithTools(any(JSONArray.class), any(CacheStrategy.class), any(ToolRegistry.class)))
                .thenReturn(ToolTurnResult.toolCalls(Arrays.asList(call), raw))
                .thenReturn(ToolTurnResult.finalText("ok!", "[]"));

        ToolRegistry tools = new ToolRegistry();
        tools.register(simpleEchoTool("echo"));

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        JSONObject pending = chatting.sendPromptExpectingToolCalls("go", tools);
        assertEquals(Boolean.TRUE, pending.get("pending"));

        ToolResult userResult = ToolResult.ok("call_3", MAPPER.readTree("{\"echo\":\"x\"}"));
        JSONObject done = chatting.continueWithToolResults(Arrays.asList(userResult), tools);
        assertEquals("ok!", done.get("message"));
        assertFalse(done.containsKey("pending"));
    }
}

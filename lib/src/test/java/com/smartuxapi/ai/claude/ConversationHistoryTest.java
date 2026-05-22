package com.smartuxapi.ai.claude;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Claude Conversation History 테스트")
class ConversationHistoryTest {

    private ConversationHistory conversationHistory;

    @BeforeEach
    void setUp() {
        conversationHistory = new ConversationHistory();
    }

    @Test
    @DisplayName("사용자 프롬프트 추가 — role=user, content=문자열")
    void testAddUserPrompt() {
        JSONArray history = conversationHistory.addUserPrompt("hi claude", null);

        assertEquals(1, history.length());
        JSONObject msg = history.getJSONObject(0);
        assertEquals("user", msg.getString("role"));
        assertEquals("hi claude", msg.getString("content"));
    }

    @Test
    @DisplayName("화면 정보 동반 — 반환 배열에는 합쳐진 프롬프트, 내부 기록에는 원본")
    void testAddUserPromptWithViewInfo() {
        JSONArray returned = conversationHistory.addUserPrompt("주문해줘", "현재화면정보");

        assertEquals("주문해줘, 현재화면정보", returned.getJSONObject(0).getString("content"));
        // 내부 기록에는 curView 가 붙지 않은 원본만 누적
        assertEquals("주문해줘", conversationHistory.getHistory().getJSONObject(0).getString("content"));
    }

    @Test
    @DisplayName("모델 응답 추가 — role=assistant")
    void testAddModelResponse() {
        conversationHistory.addUserPrompt("hi", null);
        conversationHistory.addModelResponse("hello!");

        JSONArray history = conversationHistory.getHistory();
        assertEquals(2, history.length());
        JSONObject assistant = history.getJSONObject(1);
        assertEquals("assistant", assistant.getString("role"));
        assertEquals("hello!", assistant.getString("content"));
    }

    @Test
    @DisplayName("assistant content blocks 추가 — tool_use 보존")
    void testAddAssistantContentBlocks() {
        conversationHistory.addUserPrompt("call tool", null);
        JSONArray blocks = new JSONArray()
                .put(new JSONObject().put("type", "tool_use").put("id", "toolu_1")
                        .put("name", "echo").put("input", new JSONObject()));
        conversationHistory.addAssistantContentBlocks(blocks);

        JSONObject assistant = conversationHistory.getHistory().getJSONObject(1);
        assertEquals("assistant", assistant.getString("role"));
        assertTrue(assistant.get("content") instanceof JSONArray);
        assertEquals("tool_use",
                assistant.getJSONArray("content").getJSONObject(0).getString("type"));
    }

    @Test
    @DisplayName("tool_result 들은 단일 user 메시지로 묶인다")
    void testAddToolResults() {
        conversationHistory.addUserPrompt("go", null);
        JSONArray results = new JSONArray()
                .put(new JSONObject().put("type", "tool_result")
                        .put("tool_use_id", "toolu_1").put("content", "ok"));
        conversationHistory.addToolResults(results);

        JSONObject userMsg = conversationHistory.getHistory().getJSONObject(1);
        assertEquals("user", userMsg.getString("role"));
        assertEquals("tool_result",
                userMsg.getJSONArray("content").getJSONObject(0).getString("type"));
    }

    @Test
    @DisplayName("빈 tool_result 배열은 추가되지 않는다")
    void testAddToolResultsEmptyIgnored() {
        conversationHistory.addUserPrompt("go", null);
        conversationHistory.addToolResults(new JSONArray());
        assertEquals(1, conversationHistory.getHistory().length());
    }

    @Test
    @DisplayName("clearHistory 는 기록을 비운다")
    void testClearHistory() {
        conversationHistory.addUserPrompt("hi", null);
        conversationHistory.addModelResponse("bye");
        conversationHistory.clearHistory();
        assertEquals(0, conversationHistory.getHistory().length());
    }
}

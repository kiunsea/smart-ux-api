package com.smartuxapi.ai.openai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAI Conversation History 테스트")
public class ConversationHistoryTest {

    private ConversationHistory conversationHistory;

    @BeforeEach
    void setUp() {
        conversationHistory = new ConversationHistory();
    }

    @Test
    @DisplayName("사용자 프롬프트 추가 테스트")
    public void testAddUserPrompt() {
        String userPrompt = "hi openai, my name is lol";
        JSONArray convHistory = conversationHistory.addUserPrompt(userPrompt, null);
        
        assertNotNull(convHistory, "대화 기록이 null이 아니어야 합니다");
        assertTrue(convHistory.length() > 0, "대화 기록에 항목이 있어야 합니다");
    }

    @Test
    @DisplayName("모델 응답 추가 테스트")
    public void testAddModelResponse() {
        String modelResponse = "Hello! Nice to meet you.";
        conversationHistory.addModelResponse(modelResponse);
        
        JSONArray history = conversationHistory.getHistory();
        assertNotNull(history, "히스토리가 null이 아니어야 합니다");
        assertTrue(history.length() > 0, "히스토리에 항목이 있어야 합니다");
    }

    @Test
    @DisplayName("화면 정보와 함께 프롬프트 추가 테스트")
    public void testAddUserPromptWithViewInfo() {
        String userPrompt = "test prompt";
        String curViewPrompt = "current view info";
        JSONArray convHistory = conversationHistory.addUserPrompt(userPrompt, curViewPrompt);

        assertNotNull(convHistory, "대화 기록이 null이 아니어야 합니다");
    }

    @Test
    @DisplayName("reconcileMissingToolOutputs — 대응 출력이 없는 function_call 을 오류 출력으로 보정")
    public void testReconcileMissingToolOutputs() {
        // 정상 실행된 call_ok + 파싱 단계에서 스킵된 고아 call_orphan(name 비어있음)
        JSONArray assistantOutput = new JSONArray();
        assistantOutput.put(new JSONObject()
                .put("type", "function_call").put("call_id", "call_ok").put("name", "echo").put("arguments", "{}"));
        assistantOutput.put(new JSONObject()
                .put("type", "function_call").put("call_id", "call_orphan").put("name", "").put("arguments", "{}"));
        conversationHistory.addAssistantOutputItems(assistantOutput);
        // call_ok 만 출력을 가진 상태 — call_orphan 은 대응 출력이 없어 다음 요청 시 400 을 유발
        conversationHistory.addToolResult("call_ok", "{\"echo\":\"x\"}");

        int patched = conversationHistory.reconcileMissingToolOutputs();
        assertEquals(1, patched, "고아 function_call 1건이 보정되어야 함");
        assertTrue(hasToolOutput(conversationHistory.getHistory(), "call_orphan"),
                "call_orphan 에 대한 function_call_output 이 추가되어야 함");

        // 멱등 — 이미 보정된 상태에서는 아무것도 추가하지 않음
        assertEquals(0, conversationHistory.reconcileMissingToolOutputs(),
                "이미 모든 call 이 출력을 가지면 추가 없음");
    }

    @Test
    @DisplayName("reconcileMissingToolOutputs — 모든 function_call 이 출력을 가지면 no-op")
    public void testReconcileNoopWhenComplete() {
        JSONArray assistantOutput = new JSONArray();
        assistantOutput.put(new JSONObject()
                .put("type", "function_call").put("call_id", "call_1").put("name", "echo").put("arguments", "{}"));
        conversationHistory.addAssistantOutputItems(assistantOutput);
        conversationHistory.addToolResult("call_1", "{}");

        assertEquals(0, conversationHistory.reconcileMissingToolOutputs(), "유효 흐름은 훼손하지 않음");
    }

    /** 히스토리에 주어진 call_id 의 function_call_output 이 존재하는지 확인. */
    private static boolean hasToolOutput(JSONArray history, String callId) {
        for (int i = 0; i < history.length(); i++) {
            JSONObject item = history.optJSONObject(i);
            if (item == null) continue;
            if ("function_call_output".equals(item.optString("type", ""))
                    && callId.equals(item.optString("call_id", ""))) {
                return true;
            }
        }
        return false;
    }
}

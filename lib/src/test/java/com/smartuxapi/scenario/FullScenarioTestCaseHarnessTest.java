package com.smartuxapi.scenario;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * FullScenarioTestCase 의 harness 동작을 Mockito 로 검증 — 실제 LLM 호출 없이.
 * 각 시나리오 turn 의 expected vs mock-response 비교가 정상 작동하는지 확인.
 */
@DisplayName("FullScenarioTestCase harness 단위 테스트 (Mockito)")
class FullScenarioTestCaseHarnessTest {

    private static final ObjectMapper M = new ObjectMapper();

    @SuppressWarnings("unchecked")
    private static JSONObject reply(String aqJson) throws Exception {
        JSONObject r = new JSONObject();
        r.put("message", "...");
        r.put("action_queue", aqJson == null ? null : M.readTree(aqJson));
        return r;
    }

    private static ScenarioData fixture() throws Exception {
        return ScenarioDataLoader.loadFromClasspath("scenarios/sample-2turn.json");
    }

    @Test
    @DisplayName("모든 턴 응답이 expected 와 정확히 일치 → isAllPassed=true")
    void testAllPass() throws Exception {
        ScenarioData scenario = fixture();
        ChatRoom mockRoom = mock(ChatRoom.class);
        Chatting mockChatting = mock(Chatting.class);
        when(mockRoom.getChatting()).thenReturn(mockChatting);

        // 두 턴 모두 expected 와 동일하게 응답
        when(mockChatting.sendPrompt("주문하기 눌러줘"))
                .thenReturn(reply("{\"actions\":[{\"type\":\"click\",\"target\":\"order\"}]}"));
        when(mockChatting.sendPrompt("취소"))
                .thenReturn(reply("{\"actions\":[{\"type\":\"click\",\"target\":\"cancel\"}]}"));

        FullScenarioTestCase runner = new FullScenarioTestCase(s -> mockRoom, false);
        ScenarioTestResult result = runner.run(scenario, "fixture");

        assertEquals(2, result.total());
        assertEquals(2, result.passed());
        assertTrue(result.isAllPassed());
    }

    @Test
    @DisplayName("한 턴이 다른 응답 → fail 1, pass 1")
    void testPartialFail() throws Exception {
        ScenarioData scenario = fixture();
        ChatRoom mockRoom = mock(ChatRoom.class);
        Chatting mockChatting = mock(Chatting.class);
        when(mockRoom.getChatting()).thenReturn(mockChatting);

        when(mockChatting.sendPrompt("주문하기 눌러줘"))
                .thenReturn(reply("{\"actions\":[{\"type\":\"tap\",\"target\":\"order\"}]}")); // tap ≠ click
        when(mockChatting.sendPrompt("취소"))
                .thenReturn(reply("{\"actions\":[{\"type\":\"click\",\"target\":\"cancel\"}]}"));

        FullScenarioTestCase runner = new FullScenarioTestCase(s -> mockRoom, false);
        ScenarioTestResult result = runner.run(scenario, "fixture");
        assertEquals(1, result.passed());
        assertEquals(1, result.failed());
        assertFalse(result.isAllPassed());

        TurnTestResult fail = result.getTurnResults().get(0);
        assertEquals(TurnTestResult.Status.FAIL, fail.getStatus());
        assertEquals(ActionDiff.Kind.VALUE_DIFFERS,
                fail.getValidationResult().getDiffs().get(0).getKind());
    }

    @Test
    @DisplayName("재현 도중 예외 → ERROR_RUNTIME")
    void testRuntimeError() throws Exception {
        ScenarioData scenario = fixture();
        ChatRoom mockRoom = mock(ChatRoom.class);
        Chatting mockChatting = mock(Chatting.class);
        when(mockRoom.getChatting()).thenReturn(mockChatting);
        when(mockChatting.sendPrompt(anyString())).thenThrow(new RuntimeException("network"));

        FullScenarioTestCase runner = new FullScenarioTestCase(s -> mockRoom, false);
        ScenarioTestResult result = runner.run(scenario, "fixture");
        assertEquals(2, result.errored());
        assertEquals(0, result.passed());
        assertFalse(result.isAllPassed());
        assertTrue(result.getTurnResults().get(0).getErrorMessage().contains("network"));
    }

    @Test
    @DisplayName("ChatRoomFactory 가 null 반환 → 모든 턴 SKIPPED")
    void testFactoryReturnsNull() throws Exception {
        ScenarioData scenario = fixture();
        FullScenarioTestCase runner = new FullScenarioTestCase(s -> null, false);
        ScenarioTestResult result = runner.run(scenario, "fixture");
        assertEquals(2, result.skipped());
        assertEquals(0, result.passed());
    }
}

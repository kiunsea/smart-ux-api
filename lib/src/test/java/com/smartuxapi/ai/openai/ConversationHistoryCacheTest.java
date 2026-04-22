package com.smartuxapi.ai.openai;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAI ConversationHistory 캐시 프리픽스 테스트")
class ConversationHistoryCacheTest {

    @Test
    @DisplayName("프리픽스가 설정되지 않으면 기존 동작과 동일")
    void testNoPrefixUnchangedBehavior() {
        ConversationHistory h = new ConversationHistory();
        JSONArray arr = h.addUserPrompt("hi", null);

        assertEquals(1, arr.length(), "메시지 1개만 있어야 한다");
        JSONObject msg = arr.getJSONObject(0);
        assertEquals("user", msg.getString("role"));
        assertEquals("hi", msg.getString("content"));
    }

    @Test
    @DisplayName("프리픽스 설정 시 system 메시지가 맨 앞에 붙는다")
    void testPrefixPrepended() {
        ConversationHistory h = new ConversationHistory();
        h.setCacheablePrefix("STABLE-UI-MAP");
        JSONArray arr = h.addUserPrompt("hi", null);

        assertEquals(2, arr.length(), "system + user = 2 개");
        assertEquals("system", arr.getJSONObject(0).getString("role"));
        assertEquals("STABLE-UI-MAP", arr.getJSONObject(0).getString("content"));
        assertEquals("user", arr.getJSONObject(1).getString("role"));
        assertEquals("hi", arr.getJSONObject(1).getString("content"));
    }

    @Test
    @DisplayName("프리픽스는 내부 convHistory 에는 추가되지 않아 누적되지 않는다")
    void testPrefixDoesNotAccumulate() {
        ConversationHistory h = new ConversationHistory();
        h.setCacheablePrefix("PREFIX");

        JSONArray first = h.addUserPrompt("msg1", null);
        h.addModelResponse("resp1");
        JSONArray second = h.addUserPrompt("msg2", null);

        // 두 번째 호출도 system 메시지는 정확히 1개
        long systemCount = 0;
        for (int i = 0; i < second.length(); i++) {
            if ("system".equals(second.getJSONObject(i).optString("role"))) systemCount++;
        }
        assertEquals(1, systemCount, "system 메시지는 매 호출에 1개만 존재해야 한다");

        // 내부 history 에도 system 이 없어야 한다
        JSONArray internal = h.getHistory();
        for (int i = 0; i < internal.length(); i++) {
            assertNotEquals("system", internal.getJSONObject(i).optString("role"),
                    "내부 history 에 system 메시지가 누적되면 안 된다");
        }
    }

    @Test
    @DisplayName("curViewPrompt 와 프리픽스가 함께 있을 때 순서: system → history → 최종 user(증강)")
    void testPrefixWithCurView() {
        ConversationHistory h = new ConversationHistory();
        h.setCacheablePrefix("PFX");
        JSONArray arr = h.addUserPrompt("hi", "VIEW");

        assertEquals(2, arr.length());
        assertEquals("system", arr.getJSONObject(0).getString("role"));
        assertEquals("PFX", arr.getJSONObject(0).getString("content"));
        assertEquals("user", arr.getJSONObject(1).getString("role"));
        assertEquals("hi, VIEW", arr.getJSONObject(1).getString("content"));
    }

    @Test
    @DisplayName("프리픽스를 null 로 해제하면 이후 호출은 프리픽스 없이 동작")
    void testClearPrefix() {
        ConversationHistory h = new ConversationHistory();
        h.setCacheablePrefix("PFX");
        h.addUserPrompt("msg1", null);

        h.setCacheablePrefix(null);
        JSONArray arr = h.addUserPrompt("msg2", null);

        for (int i = 0; i < arr.length(); i++) {
            assertNotEquals("system", arr.getJSONObject(i).optString("role"));
        }
    }

    @Test
    @DisplayName("빈 문자열 프리픽스는 무시된다")
    void testEmptyPrefixIgnored() {
        ConversationHistory h = new ConversationHistory();
        h.setCacheablePrefix("");
        JSONArray arr = h.addUserPrompt("msg", null);

        for (int i = 0; i < arr.length(); i++) {
            assertNotEquals("system", arr.getJSONObject(i).optString("role"));
        }
    }
}

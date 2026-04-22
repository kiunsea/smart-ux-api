package com.smartuxapi.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionQueueUtil 테스트")
public class ActionQueueUtilTest {

    @Test
    @DisplayName("null 입력 시 null 반환")
    void extractFromNull() {
        assertThrows(Exception.class, () -> {
            ActionQueueUtil.extractActionQueue(null);
        }, "null 입력 시 예외가 발생해야 합니다");
    }

    @Test
    @DisplayName("빈 문자열 입력 시 null 반환")
    void extractFromEmptyString() {
        JsonNode result = ActionQueueUtil.extractActionQueue("");
        assertNull(result, "빈 문자열에서는 null을 반환해야 합니다");
    }

    @Test
    @DisplayName("JSON이 없는 일반 텍스트 입력 시 null 반환")
    void extractFromPlainText() {
        String plainText = "이것은 JSON이 포함되지 않은 일반 텍스트입니다.";
        JsonNode result = ActionQueueUtil.extractActionQueue(plainText);
        assertNull(result, "일반 텍스트에서는 null을 반환해야 합니다");
    }

    @Test
    @DisplayName("순수 JSON 객체 추출 성공")
    void extractPureJsonObject() {
        String message = "{\"action_queue\":[{\"type\":\"click\",\"id\":\"btn1\"}]}";
        JsonNode result = ActionQueueUtil.extractActionQueue(message);

        assertNotNull(result, "유효한 JSON 객체를 추출해야 합니다");
        assertTrue(result.has("action_queue"), "action_queue 필드가 있어야 합니다");
    }

    @Test
    @DisplayName("텍스트에 포함된 JSON 객체 추출")
    void extractJsonEmbeddedInText() {
        String message = "응답입니다. {\"action_queue\":[{\"type\":\"click\",\"id\":\"btn1\"}]} 이상입니다.";
        JsonNode result = ActionQueueUtil.extractActionQueue(message);

        assertNotNull(result, "텍스트에 포함된 JSON을 추출해야 합니다");
    }

    @Test
    @DisplayName("마크다운 코드 블록 내 JSON 추출")
    void extractJsonFromCodeBlock() {
        String message = "결과는 다음과 같습니다:\n```json\n{\"action_queue\":[{\"type\":\"click\",\"id\":\"btn1\"}]}\n```\n";
        JsonNode result = ActionQueueUtil.extractActionQueue(message);

        assertNotNull(result, "코드 블록 내 JSON을 추출해야 합니다");
        assertTrue(result.has("action_queue"), "action_queue 필드가 있어야 합니다");
    }

    @Test
    @DisplayName("JSON 배열 추출")
    void extractJsonArray() {
        String message = "액션 목록: [{\"type\":\"click\",\"id\":\"btn1\"},{\"type\":\"click\",\"id\":\"btn2\"}]";
        JsonNode result = ActionQueueUtil.extractActionQueue(message);

        assertNotNull(result, "JSON 배열을 추출해야 합니다");
        assertTrue(result.isArray(), "결과가 배열이어야 합니다");
        assertEquals(2, result.size(), "배열 요소가 2개여야 합니다");
    }

    @Test
    @DisplayName("중첩 JSON 객체 추출")
    void extractNestedJson() {
        String message = "{\"message\":\"주문 완료\",\"action_queue\":[{\"type\":\"click\",\"target\":{\"id\":\"btn1\",\"selector\":\"#btn1\"}}]}";
        JsonNode result = ActionQueueUtil.extractActionQueue(message);

        assertNotNull(result, "중첩 JSON을 추출해야 합니다");
        assertTrue(result.has("action_queue"), "action_queue 필드가 있어야 합니다");
    }

    @Test
    @DisplayName("깨진 JSON 입력 시 null 반환")
    void extractFromBrokenJson() {
        String message = "{\"action_queue\": [{ broken json without closing";
        JsonNode result = ActionQueueUtil.extractActionQueue(message);

        assertNull(result, "깨진 JSON에서는 null을 반환해야 합니다");
    }
}

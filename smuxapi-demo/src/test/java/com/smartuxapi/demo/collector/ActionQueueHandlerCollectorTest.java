package com.smartuxapi.demo.collector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionQueueHandlerCollector 단위 테스트")
class ActionQueueHandlerCollectorTest {

    @Test
    @DisplayName("extractApiPromptTemplate — userMsg 를 {userMsg} 로 치환")
    void testExtractTemplate() {
        String full = "UI Info: {...} / User: 안녕하세요";
        String template = ActionQueueHandlerCollector.extractApiPromptTemplate(full, "안녕하세요");
        assertEquals("UI Info: {...} / User: {userMsg}", template);
    }

    @Test
    @DisplayName("userMsg 가 없으면 원본 그대로")
    void testNoMatch() {
        String full = "UI Info: xxx";
        assertEquals("UI Info: xxx",
                ActionQueueHandlerCollector.extractApiPromptTemplate(full, "미등장문자열"));
    }

    @Test
    @DisplayName("userMsg null/empty → 원본 반환")
    void testEmptyUser() {
        assertEquals("x", ActionQueueHandlerCollector.extractApiPromptTemplate("x", null));
        assertEquals("x", ActionQueueHandlerCollector.extractApiPromptTemplate("x", ""));
    }

    @Test
    @DisplayName("fullPrompt null → null")
    void testNullFull() {
        assertNull(ActionQueueHandlerCollector.extractApiPromptTemplate(null, "x"));
    }
}

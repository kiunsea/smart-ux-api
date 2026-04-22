package com.smartuxapi.ai.gemini;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.json.JSONArray;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gemini Conversation History 테스트")
public class ConversationHistoryTest {

    private ConversationHistory conversationHistory;

    @BeforeEach
    void setUp() {
        conversationHistory = new ConversationHistory();
    }

    @Test
    @DisplayName("사용자 프롬프트 추가 테스트")
    public void testAddUserPrompt() {
        String userPrompt = "hi gemini, my name is 티니핑";
        JSONArray convHistory = conversationHistory.addUserPrompt(userPrompt, null);
        
        assertNotNull(convHistory, "대화 기록이 null이 아니어야 합니다");
        assertTrue(convHistory.length() > 0, "대화 기록에 항목이 있어야 합니다");
    }

    @Test
    @DisplayName("모델 응답 추가 테스트")
    public void testAddModelResponse() {
        String modelResponse = "Hello! Nice to meet you, 티니핑.";
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
}

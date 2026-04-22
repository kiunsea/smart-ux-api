package com.smartuxapi.ai.openai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.json.JSONArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAI Responses API Connection 테스트")
public class ResponsesAPIConnectionTest {

    @Test
    @DisplayName("OpenAI API 연결 및 응답 테스트")
    @Disabled("API 키가 필요하므로 기본적으로 비활성화")
    public void testGenerateContent() throws Exception {
        ConversationHistory conversationHistory = new ConversationHistory();
        
        // when: ResponsesAPIConnection 메서드 호출
        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        if (config == null) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "dev.apikey.json 파일이 클래스패스에 없습니다. 테스트를 건너뜁니다.");
            return;
        }
        
        String apiKey = config.get("OPENAI_API_KEY").asText();
        String model = config.get("OPENAI_MODEL").asText();
        ResponsesAPIConnection conn = new ResponsesAPIConnection(apiKey, model);
        
        String userPrompt = "hi openai";
        JSONArray convHistory = conversationHistory.addUserPrompt(userPrompt, null);
        String response = conn.generateContent(convHistory);

        // then: 결과 검증
        assertNotNull(response, "응답이 null이 아니어야 합니다");
        assertFalse(response.isEmpty(), "응답이 비어있지 않아야 합니다");
    }
}

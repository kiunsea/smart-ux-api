package com.smartuxapi.ai.claude;

import org.json.JSONArray;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Claude API Connection 테스트")
class ClaudeAPIConnectionTest {

    @Test
    @DisplayName("Claude API 연결 및 응답 테스트")
    @Disabled("API 키가 필요하므로 기본적으로 비활성화")
    void testGenerateContent() throws Exception {
        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        if (config == null) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false,
                    "dev.apikey.json 파일이 클래스패스에 없습니다. 테스트를 건너뜁니다.");
            return;
        }

        String apiKey = config.get("CLAUDE_API_KEY").asText();
        String model = config.get("CLAUDE_MODEL").asText();
        ClaudeAPIConnection conn = new ClaudeAPIConnection(apiKey, model);

        ConversationHistory history = new ConversationHistory();
        JSONArray messages = history.addUserPrompt("hi claude", null);
        String response = conn.generateContent(messages);

        assertNotNull(response, "응답이 null이 아니어야 합니다");
        assertFalse(response.isEmpty(), "응답이 비어있지 않아야 합니다");
    }
}

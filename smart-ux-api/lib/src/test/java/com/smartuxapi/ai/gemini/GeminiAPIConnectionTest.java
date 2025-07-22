package com.smartuxapi.ai.gemini;

import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

public class GeminiAPIConnectionTest {

    public static void main(String args[]) throws Exception {
        // given: Gemini API 응답 JSON 객체
        JSONObject content = new JSONObject();
        content.put("parts", new JSONArray() {{
            put("Hello from Gemini!");
        }});

        JSONObject candidate = new JSONObject();
        candidate.put("content", content);

        JSONArray candidates = new JSONArray();
        candidates.put(candidate);

        JSONObject response = new JSONObject();
        response.put("candidates", candidates);

        com.smartuxapi.ai.gemini.ChatManager chatManager = new com.smartuxapi.ai.gemini.ChatManager();
        String currentSessionId = UUID.randomUUID().toString();
        
        // when: GeminiAPIConnection 메서드 호출
        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String geminiApiKey = config.get("GEMINI_API_KEY").asText();
        GeminiAPIConnection conn = new GeminiAPIConnection(geminiApiKey, "gemini-2.5-flash");
        
        List<JSONObject> conversationHistory = chatManager.addUserMessage(currentSessionId, "hi gemini");
        String geminiResponse = conn.generateContent(conversationHistory);

        // then: 결과 검증
        System.out.println("Gemini response : " + geminiResponse);
    }
}

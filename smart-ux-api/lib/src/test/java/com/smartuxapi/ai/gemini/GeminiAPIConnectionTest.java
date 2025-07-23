package com.smartuxapi.ai.gemini;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

import com.smartuxapi.ai.ConfigLoader;
import com.smartuxapi.ai.MessageHistory;

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

        MessageHistory msgHistory = new MessageHistory();
        
        // when: GeminiAPIConnection 메서드 호출
        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String geminiApiKey = config.get("GEMINI_API_KEY").asText();
        GeminiAPIConnection conn = new GeminiAPIConnection(geminiApiKey, "gemini-2.5-flash");
        
        String userPrompt = "hi gemini";
        System.out.println("User prompt : "+userPrompt);
        List<JSONObject> conversationHistory = msgHistory.addUserMessage(userPrompt);
        String geminiResponse = conn.generateContent(conversationHistory);

        // then: 결과 검증
        System.out.println("Gemini response : " + geminiResponse);
    }
}

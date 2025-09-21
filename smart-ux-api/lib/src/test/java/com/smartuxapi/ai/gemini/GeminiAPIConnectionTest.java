package com.smartuxapi.ai.gemini;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

public class GeminiAPIConnectionTest {

    public static void main(String args[]) throws Exception {

        ConversationHistory conversationHistory = new ConversationHistory();
        
        // when: GeminiAPIConnection 메서드 호출
        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String apiKey = config.get("GEMINI_API_KEY").asText();
        String model = config.get("GEMINI_MODEL").asText();
        GeminiAPIConnection conn = new GeminiAPIConnection(apiKey, model);
        
        String userPrompt = "hi gemini";
        System.out.println("User prompt : "+userPrompt);
        JSONArray convHistory = conversationHistory.addUserPrompt(userPrompt, null);
        String geminiResponse = conn.generateContent(convHistory);

        // then: 결과 검증
        System.out.println("Gemini response : " + geminiResponse);
    }
}

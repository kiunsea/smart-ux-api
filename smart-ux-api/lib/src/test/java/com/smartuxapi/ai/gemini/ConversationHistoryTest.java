package com.smartuxapi.ai.gemini;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

public class ConversationHistoryTest {

    public static void main(String args[]) throws Exception {
        
        ConversationHistory conversationHistory = new ConversationHistory();

        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String apiKey = config.get("GEMINI_API_KEY").asText();
        String model = config.get("GEMINI_MODEL").asText();
        GeminiAPIConnection conn = new GeminiAPIConnection(apiKey, model);

        String userPrompt = "hi gemini, my name is 티니핑";
        System.out.println("User prompt : " + userPrompt +" <- 내 이름을 티니핑 이라고 알려줌");
        JSONArray convHistory = conversationHistory.addUserPrompt(userPrompt, null);
        String geminiResponse = conn.generateContent(convHistory);
        
        // then: 결과 검증
        System.out.println("Gemini response : " + geminiResponse);
        conversationHistory.addModelResponse(geminiResponse);
        
        userPrompt = "What is my name?";
        System.out.println("User prompt : " + userPrompt);
        convHistory = conversationHistory.addUserPrompt(userPrompt, null);
        geminiResponse = conn.generateContent(convHistory);
        System.out.println("Gemini response : " + geminiResponse +" <- 내 이름이 티니핑 임을 아는지 확인함");
    }
}

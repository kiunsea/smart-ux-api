package com.smartuxapi.ai.openai;

import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

public class ConversationHistoryTest {

    public static void main(String args[]) throws Exception {
        
        ConversationHistory conversationHistory = new ConversationHistory();

        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String apiKey = config.get("OPENAI_API_KEY").asText();
        String model = config.get("OPENAI_MODEL").asText();
        ResponsesAPIConnection conn = new ResponsesAPIConnection(apiKey, model);

        String userPrompt = "hi openai, my name is lol";
        System.out.println("User prompt : " + userPrompt);
        List<JSONObject> convHistory = conversationHistory.addUserMessage(userPrompt);
        String geminiResponse = conn.generateContent(convHistory);
        
        // then: 결과 검증
        System.out.println("OpenAI response : " + geminiResponse);
        conversationHistory.addModelResponse(geminiResponse);
        
        userPrompt = "What is my name?";
        System.out.println("User prompt : " + userPrompt);
        convHistory = conversationHistory.addUserMessage(userPrompt);
        geminiResponse = conn.generateContent(convHistory);
        System.out.println("OpenAI response : " + geminiResponse);
    }
}

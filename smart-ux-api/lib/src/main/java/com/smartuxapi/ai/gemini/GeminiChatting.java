package com.smartuxapi.ai.gemini;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.json.JSONObject;

import com.smartuxapi.ai.ChatManager;
import com.smartuxapi.ai.Chatting;

/**
 * 
 */
public class GeminiChatting implements Chatting {
    
    private GeminiAPIConnection connApi = null;
    private ChatManager chatManager = null;
    private String currentSessionId = null;
    
    public GeminiChatting(GeminiAPIConnection connApi) {
        this.connApi = connApi;
        this.chatManager = new ChatManager();
        this.currentSessionId = UUID.randomUUID().toString();
    }
    
    @Override
    public org.json.simple.JSONObject sendMessage(String userMsg) throws Exception {

        // TODO ChatApplication.java 를 참고해서 함수를 완성해야 한다.
        // 대화 히스토리를 관리하고 gemini에게 메세지 전송시 마지막에 현재 화면 정보도 함께 전달하도록 해야 한다.
        
        // 1. 사용자 메시지를 대화 기록에 추가하고, Gemini에 보낼 전체 기록을 가져옴
        List<JSONObject> conversationHistory = this.chatManager.addUserMessage(this.currentSessionId, userMsg);

        // 2. Gemini API 호출 (전체 대화 기록 전송)
        String geminiResponse = this.connApi.generateContent(conversationHistory);

        // 3. Gemini 응답을 대화 기록에 추가
        chatManager.addModelResponse(currentSessionId, geminiResponse);

        return null;
    }

    /**
     * Dummy
     * Gemini API에서는 불필요한 함수이다.
     */
    public Set<String> getMessageIdSet() {
        return null;
    }
}
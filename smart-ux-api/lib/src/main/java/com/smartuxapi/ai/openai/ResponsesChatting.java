package com.smartuxapi.ai.openai;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.openai.ConversationHistory;

/**
 * 
 */
public class ResponsesChatting implements Chatting {
    
    private ResponsesAPIConnection connApi = null;
    private ConversationHistory conversationHistory = null;
    private ActionQueueHandler aqHandler = null;
    
    public ResponsesChatting(ResponsesAPIConnection connApi) {
        this.connApi = connApi;
        this.conversationHistory = new ConversationHistory();
    }
    
    /**
     * 대화 히스토리를 관리하고 gemini에게 메세지 전송시 마지막에 현재 화면 정보도 함께 전달
     */
    @Override
    public org.json.simple.JSONObject sendPrompt(String userMsg) throws Exception {
        
        // Action Queue 요청 Prompt 작성 및 전달
        String aqPrompt = null;
        if (this.aqHandler != null) {
            aqPrompt = this.aqHandler.decoratePrompt(userMsg);
        } else {
            aqPrompt = userMsg;
        }
        
        // 1. 사용자 메시지를 대화 기록에 추가하고, AI에 보낼 전체 기록을 가져옴
        List<org.json.JSONObject> convHistory = this.conversationHistory.addUserMessage(aqPrompt);

        // 2. Responses API 호출 (전체 대화 기록 전송)
        String aiResponse = this.connApi.generateContent(convHistory);

        // 3. AI 응답을 대화 기록에 추가
        this.conversationHistory.addModelResponse(aiResponse);
        
        // Action Queue 메세지 전달
        org.json.simple.JSONObject resJson = new org.json.simple.JSONObject();
        resJson.put("message", aiResponse);
        
        if (this.aqHandler != null) {
            JsonNode aqObj = this.aqHandler.getActionQueue(aiResponse);
            if (aqObj != null && aqObj.hasNonNull("action_queue")) {
                resJson.put("action_queue", aqObj.get("action_queue"));
            } else {
                resJson.put("action_queue", aqObj);
            }
        }

        return resJson;
    }

    /**
     * Dummy
     * Gemini API에서는 불필요한 함수이다.
     */
    @Override
    public Set<String> getMessageIdSet() {
        return null;
    }

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
    }
}
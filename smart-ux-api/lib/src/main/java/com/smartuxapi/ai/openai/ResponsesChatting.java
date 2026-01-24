package com.smartuxapi.ai.openai;

import java.util.Set;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.debug.DebugLogger;

/**
 * OpenAI Responses API를 사용하는 Chatting 구현체
 */
public class ResponsesChatting implements Chatting {

    private ResponsesAPIConnection connApi = null;
    private ConversationHistory conversationHistory = null;
    private ActionQueueHandler aqHandler = null;
    private DebugLogger debugLogger = null;
    private String chatRoomId = null;

    public ResponsesChatting(ResponsesAPIConnection connApi) {
        this.connApi = connApi;
        this.conversationHistory = new ConversationHistory();
    }

    /**
     * 디버그 로거 설정
     */
    public void setDebugLogger(DebugLogger debugLogger, String chatRoomId) {
        this.debugLogger = debugLogger;
        this.chatRoomId = chatRoomId;
    }

    /**
     * 대화 히스토리를 관리하고 AI에게 메세지 전송시 마지막에 현재 화면 정보도 함께 전달
     */
    @Override
    public org.json.simple.JSONObject sendPrompt(String userMsg) throws Exception {

        boolean reqActionQueue = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();

        String usrPrompt, curViewPrompt = null;

        // Action Queue 요청 Prompt 작성 및 전달
        if (reqActionQueue) {
            usrPrompt = this.aqHandler.getActionQueuePrompt(userMsg);
            curViewPrompt = this.aqHandler.getCurViewPrompt();
        } else {
            usrPrompt = userMsg;
        }

        // 디버그 로깅: 턴 시작
        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.startTurn(chatRoomId, userMsg, usrPrompt, curViewPrompt);
        }

        // 1. 사용자 메시지를 대화 기록에 추가하고, AI에 보낼 전체 기록을 가져옴
        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);

        // 2. Responses API 호출 (전체 대화 기록 전송)
        String aiResponse = this.connApi.generateContent(convHistory);

        // 3. AI 응답을 대화 기록에 추가
        this.conversationHistory.addModelResponse(aiResponse);

        // 4. 화면 정보가 변경되어 프롬프트에 포함된 경우, 전송 완료로 표시
        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        // Action Queue 메세지 전달
        org.json.simple.JSONObject resJson = new org.json.simple.JSONObject();
        resJson.put("message", aiResponse);

        JsonNode actionQueue = null;
        if (this.aqHandler != null) {
            JsonNode aqObj = this.aqHandler.getActionQueue(aiResponse);
            if (aqObj != null && aqObj.hasNonNull("action_queue")) {
                actionQueue = aqObj.get("action_queue");
                resJson.put("action_queue", actionQueue);
            } else {
                actionQueue = aqObj;
                resJson.put("action_queue", aqObj);
            }
        }

        // 디버그 로깅: 턴 완료
        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.completeTurn(chatRoomId, aiResponse, actionQueue);
        }

        return resJson;
    }

    /**
     * Dummy
     * OpenAI Responses API에서는 불필요한 함수이다.
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
package com.smartuxapi.ai.debug.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 대화 턴별 데이터 모델
 * 하나의 요청-응답 쌍을 나타냅니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationTurn {

    @JsonProperty("turn")
    private int turn;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("user_message")
    private String userMessage;

    @JsonProperty("full_prompt")
    private String fullPrompt;

    @JsonProperty("view_info")
    private String viewInfo;

    @JsonProperty("ai_response")
    private String aiResponse;

    @JsonProperty("action_queue")
    private JsonNode actionQueue;

    public ConversationTurn() {
    }

    public ConversationTurn(int turn) {
        this.turn = turn;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getFullPrompt() {
        return fullPrompt;
    }

    public void setFullPrompt(String fullPrompt) {
        this.fullPrompt = fullPrompt;
    }

    public String getViewInfo() {
        return viewInfo;
    }

    public void setViewInfo(String viewInfo) {
        this.viewInfo = viewInfo;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public JsonNode getActionQueue() {
        return actionQueue;
    }

    public void setActionQueue(JsonNode actionQueue) {
        this.actionQueue = actionQueue;
    }
}

package com.smartuxapi.ai.debug.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ChatRoom 전체 대화 데이터 모델
 * 하나의 ChatRoom 세션의 모든 대화 내용을 담습니다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationData {

    @JsonProperty("chatRoomId")
    private String chatRoomId;

    @JsonProperty("aiProvider")
    private String aiProvider;

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("lastUpdatedAt")
    private String lastUpdatedAt;

    @JsonProperty("totalTurns")
    private int totalTurns;

    @JsonProperty("conversations")
    private List<ConversationTurn> conversations;

    public ConversationData() {
    }

    public ConversationData(String chatRoomId, String aiProvider, String modelName) {
        this.chatRoomId = chatRoomId;
        this.aiProvider = aiProvider;
        this.modelName = modelName;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public String getAiProvider() {
        return aiProvider;
    }

    public void setAiProvider(String aiProvider) {
        this.aiProvider = aiProvider;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public int getTotalTurns() {
        return totalTurns;
    }

    public void setTotalTurns(int totalTurns) {
        this.totalTurns = totalTurns;
    }

    public List<ConversationTurn> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationTurn> conversations) {
        this.conversations = conversations;
        if (conversations != null) {
            this.totalTurns = conversations.size();
        }
    }
}

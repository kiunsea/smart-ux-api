package com.smartuxapi.ai.debug;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.debug.model.ConversationData;
import com.smartuxapi.ai.debug.model.ConversationTurn;

/**
 * 디버그 로거 클래스
 * ChatRoom의 대화 내용을 파일로 저장합니다.
 */
public class DebugLogger {

    private static final Logger logger = LogManager.getLogger(DebugLogger.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // ChatRoom ID별 대화 데이터 저장
    private final Map<String, ChatRoomDebugData> chatRoomDataMap = new ConcurrentHashMap<>();

    private final DebugConfig config;
    private final ObjectMapper objectMapper;

    /**
     * 기본 생성자 (싱글톤 DebugConfig 사용)
     */
    public DebugLogger() {
        this.config = DebugConfig.getInstance();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 지정된 설정으로 생성
     */
    public DebugLogger(DebugConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 디버그 모드 활성화 여부 확인
     */
    public boolean isEnabled() {
        return config.isDebugMode();
    }

    /**
     * ChatRoom 세션 시작 (새 ChatRoom 생성 시 호출)
     */
    public void startSession(String chatRoomId, String aiProvider, String modelName) {
        if (!isEnabled()) return;

        ChatRoomDebugData data = new ChatRoomDebugData(chatRoomId, aiProvider, modelName);
        data.createdAt = LocalDateTime.now().format(ISO_FORMATTER);
        chatRoomDataMap.put(chatRoomId, data);

        logger.debug("Debug session started for ChatRoom: {}", chatRoomId);
    }

    /**
     * 새 대화 턴 시작
     */
    public void startTurn(String chatRoomId, String userMessage, String fullPrompt, String viewInfo) {
        if (!isEnabled()) return;

        ChatRoomDebugData data = chatRoomDataMap.get(chatRoomId);
        if (data == null) {
            logger.warn("ChatRoom not found for debug logging: {}", chatRoomId);
            return;
        }

        ConversationTurn turn = new ConversationTurn(data.turns.size() + 1);
        turn.setTimestamp(LocalDateTime.now().format(ISO_FORMATTER));
        turn.setUserMessage(userMessage);
        turn.setFullPrompt(fullPrompt);
        turn.setViewInfo(viewInfo);

        data.currentTurn = turn;
        logger.debug("New turn {} started for ChatRoom: {}", turn.getTurn(), chatRoomId);
    }

    /**
     * 대화 턴 완료 (AI 응답 수신 후 호출)
     */
    public void completeTurn(String chatRoomId, String aiResponse, JsonNode actionQueue) {
        if (!isEnabled()) return;

        ChatRoomDebugData data = chatRoomDataMap.get(chatRoomId);
        if (data == null || data.currentTurn == null) {
            logger.warn("No active turn found for ChatRoom: {}", chatRoomId);
            return;
        }

        data.currentTurn.setAiResponse(aiResponse);
        data.currentTurn.setActionQueue(actionQueue);

        // 턴 완료 - 리스트에 추가
        data.turns.add(data.currentTurn);
        data.lastUpdatedAt = LocalDateTime.now().format(ISO_FORMATTER);

        int turnNumber = data.currentTurn.getTurn();
        data.currentTurn = null;

        logger.debug("Turn {} completed for ChatRoom: {}", turnNumber, chatRoomId);

        // 자동 저장
        try {
            saveToFile(chatRoomId);
        } catch (IOException e) {
            logger.error("Failed to auto-save debug data for ChatRoom: {}", chatRoomId, e);
        }
    }

    /**
     * 대화 턴 완료 (JSONObject 응답 사용)
     */
    public void completeTurn(String chatRoomId, JSONObject response) {
        if (!isEnabled()) return;

        String aiResponse = null;
        JsonNode actionQueue = null;

        // 응답에서 데이터 추출
        if (response != null) {
            Object messageObj = response.get("message");
            if (messageObj != null) {
                aiResponse = messageObj.toString();
            }

            Object actionQueueObj = response.get("action_queue");
            if (actionQueueObj != null) {
                try {
                    actionQueue = objectMapper.valueToTree(actionQueueObj);
                } catch (Exception e) {
                    logger.warn("Failed to convert action_queue to JsonNode", e);
                }
            }
        }

        completeTurn(chatRoomId, aiResponse, actionQueue);
    }

    /**
     * ChatRoom 데이터를 파일로 저장
     */
    public void saveToFile(String chatRoomId) throws IOException {
        if (!isEnabled()) return;

        ChatRoomDebugData data = chatRoomDataMap.get(chatRoomId);
        if (data == null || data.turns.isEmpty()) {
            logger.debug("No data to save for ChatRoom: {}", chatRoomId);
            return;
        }

        // 출력 디렉터리 생성
        String absoluteOutputPath = config.getAbsoluteOutputPath();
        File outputDir = new File(absoluteOutputPath);
        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                logger.warn("Failed to create output directory: {}", absoluteOutputPath);
            }
        }

        // 파일명 생성
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String shortId = chatRoomId.length() > 8 ? chatRoomId.substring(0, 8) : chatRoomId;
        String fileName = String.format("%s-%s-%s.json", config.getFilePrefix(), timestamp, shortId);

        String filePath = absoluteOutputPath;
        if (!filePath.endsWith("/") && !filePath.endsWith("\\") && !filePath.endsWith(File.separator)) {
            filePath += File.separator;
        }
        filePath += fileName;

        // ConversationData 생성
        ConversationData conversationData = new ConversationData(
            data.chatRoomId,
            data.aiProvider,
            data.modelName
        );
        conversationData.setCreatedAt(data.createdAt);
        conversationData.setLastUpdatedAt(data.lastUpdatedAt);
        conversationData.setConversations(new ArrayList<>(data.turns));

        // JSON 파일로 저장
        File outputFile = new File(filePath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, conversationData);

        logger.info("=".repeat(80));
        logger.info("Debug data saved");
        logger.info("  ChatRoom: {}", chatRoomId);
        logger.info("  File: {}", outputFile.getAbsolutePath());
        logger.info("  Size: {} bytes", outputFile.length());
        logger.info("  Turns: {}", data.turns.size());
        logger.info("=".repeat(80));
    }

    /**
     * ChatRoom 세션 종료 (ChatRoom.close() 시 호출)
     */
    public void endSession(String chatRoomId) {
        if (!isEnabled()) return;

        try {
            saveToFile(chatRoomId);
        } catch (IOException e) {
            logger.error("Failed to save debug data on session end for ChatRoom: {}", chatRoomId, e);
        }

        chatRoomDataMap.remove(chatRoomId);
        logger.debug("Debug session ended for ChatRoom: {}", chatRoomId);
    }

    /**
     * 모든 세션 데이터 클리어 (테스트용)
     */
    public void clearAll() {
        chatRoomDataMap.clear();
    }

    /**
     * ChatRoom별 디버그 데이터를 저장하는 내부 클래스
     */
    private static class ChatRoomDebugData {
        final String chatRoomId;
        final String aiProvider;
        final String modelName;
        String createdAt;
        String lastUpdatedAt;
        final List<ConversationTurn> turns = new ArrayList<>();
        ConversationTurn currentTurn;

        ChatRoomDebugData(String chatRoomId, String aiProvider, String modelName) {
            this.chatRoomId = chatRoomId;
            this.aiProvider = aiProvider;
            this.modelName = modelName;
        }
    }
}

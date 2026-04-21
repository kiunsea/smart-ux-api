package com.smartuxapi.ai.debug;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DebugLogger 테스트")
public class DebugLoggerTest {

    private DebugConfig config;
    private DebugLogger logger;

    @BeforeEach
    void setUp() {
        config = DebugConfig.getInstance();
        logger = new DebugLogger(config);
        logger.clearAll();
    }

    @Test
    @DisplayName("디버그 모드 비활성화 시 메서드 무시 확인")
    void disabledModeSkipsAllOperations() {
        boolean original = config.isDebugMode();
        try {
            config.setDebugMode(false);
            DebugLogger disabledLogger = new DebugLogger(config);

            // 예외 없이 실행되어야 함
            assertDoesNotThrow(() -> {
                disabledLogger.startSession("test-room", "TestProvider", "test-model");
                disabledLogger.startTurn("test-room", "hello", "full prompt", null);
                disabledLogger.completeTurn("test-room", "response", null);
                disabledLogger.endSession("test-room");
            });

            assertFalse(disabledLogger.isEnabled(), "비활성화 시 isEnabled()는 false여야 합니다");
        } finally {
            config.setDebugMode(original);
        }
    }

    @Test
    @DisplayName("전체 세션 흐름 테스트 - startSession -> startTurn -> completeTurn -> saveToFile")
    void fullSessionFlow(@TempDir Path tempDir) throws IOException {
        boolean originalMode = config.isDebugMode();
        String originalPath = config.getOutputPath();
        try {
            config.setDebugMode(true);
            config.setOutputPath(tempDir.toString());

            String chatRoomId = "test-room-001";

            logger.startSession(chatRoomId, "OpenAI", "gpt-4");
            logger.startTurn(chatRoomId, "안녕하세요", "full prompt: 안녕하세요", null);
            logger.completeTurn(chatRoomId, "안녕하세요! 무엇을 도와드릴까요?", null);

            // 파일이 생성되었는지 확인
            File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            assertNotNull(files, "파일 목록이 null이 아니어야 합니다");
            assertTrue(files.length > 0, "JSON 파일이 생성되어야 합니다");

            // JSON 파일 내용 검증
            ObjectMapper mapper = new ObjectMapper();
            JsonNode saved = mapper.readTree(files[0]);
            assertEquals(chatRoomId, saved.get("chatRoomId").asText(), "chatRoomId가 일치해야 합니다");
            assertEquals("OpenAI", saved.get("aiProvider").asText(), "aiProvider가 일치해야 합니다");
            assertTrue(saved.has("conversations"), "conversations 필드가 있어야 합니다");
            assertEquals(1, saved.get("conversations").size(), "대화 턴이 1개여야 합니다");
        } finally {
            config.setDebugMode(originalMode);
            config.setOutputPath(originalPath);
        }
    }

    @Test
    @DisplayName("존재하지 않는 chatRoomId로 startTurn 호출 시 에러 없이 처리")
    void startTurnWithInvalidChatRoomId() {
        boolean original = config.isDebugMode();
        try {
            config.setDebugMode(true);

            // 예외 없이 실행되어야 함 (경고 로그만 출력)
            assertDoesNotThrow(() -> {
                logger.startTurn("nonexistent-room", "hello", "prompt", null);
            });
        } finally {
            config.setDebugMode(original);
        }
    }

    @Test
    @DisplayName("endSession 후 데이터 정리 확인")
    void endSessionClearsData(@TempDir Path tempDir) {
        boolean originalMode = config.isDebugMode();
        String originalPath = config.getOutputPath();
        try {
            config.setDebugMode(true);
            config.setOutputPath(tempDir.toString());

            String chatRoomId = "test-room-cleanup";
            logger.startSession(chatRoomId, "Gemini", "gemini-pro");
            logger.startTurn(chatRoomId, "test", "prompt", null);
            logger.completeTurn(chatRoomId, "response", null);
            logger.endSession(chatRoomId);

            // endSession 후 다시 startTurn 호출 시 경고만 발생 (데이터 없음)
            assertDoesNotThrow(() -> {
                logger.startTurn(chatRoomId, "after end", "prompt", null);
            });
        } finally {
            config.setDebugMode(originalMode);
            config.setOutputPath(originalPath);
        }
    }

    @Test
    @DisplayName("JSONObject 응답으로 completeTurn 테스트")
    @SuppressWarnings("unchecked")
    void completeTurnWithJSONObject(@TempDir Path tempDir) throws IOException {
        boolean originalMode = config.isDebugMode();
        String originalPath = config.getOutputPath();
        try {
            config.setDebugMode(true);
            config.setOutputPath(tempDir.toString());

            String chatRoomId = "test-room-json";
            logger.startSession(chatRoomId, "OpenAI", "gpt-4");
            logger.startTurn(chatRoomId, "주문해줘", "full prompt", "{\"viewInfo\":[]}");

            JSONObject response = new JSONObject();
            response.put("message", "주문을 도와드리겠습니다.");
            response.put("action_queue", "[{\"type\":\"click\",\"id\":\"btn1\"}]");

            logger.completeTurn(chatRoomId, response);

            // 파일 검증
            File[] files = tempDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            assertNotNull(files);
            assertTrue(files.length > 0, "JSON 파일이 생성되어야 합니다");
        } finally {
            config.setDebugMode(originalMode);
            config.setOutputPath(originalPath);
        }
    }

    @Test
    @DisplayName("null 응답으로 completeTurn 테스트")
    void completeTurnWithNullResponse(@TempDir Path tempDir) {
        boolean originalMode = config.isDebugMode();
        String originalPath = config.getOutputPath();
        try {
            config.setDebugMode(true);
            config.setOutputPath(tempDir.toString());

            String chatRoomId = "test-room-null";
            logger.startSession(chatRoomId, "OpenAI", "gpt-4");
            logger.startTurn(chatRoomId, "test", "prompt", null);

            assertDoesNotThrow(() -> {
                logger.completeTurn(chatRoomId, (JSONObject) null);
            });
        } finally {
            config.setDebugMode(originalMode);
            config.setOutputPath(originalPath);
        }
    }
}

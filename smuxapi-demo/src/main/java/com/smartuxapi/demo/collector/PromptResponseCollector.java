package com.smartuxapi.demo.collector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 대화 턴별 프롬프트/응답 데이터 수집기 (session-scoped 는 호출자 판단 — 본 Component 는 singleton).
 *
 * <p>Decorator 들이 capture* 메서드를 호출하고, Controller 경계에서 {@link #saveToFile()} 로
 * JSON 파일을 생성한다.
 *
 * <p>Thread-safety: 동일 세션에서 Controller → Service → ChatRoom 호출 흐름은 직렬이므로
 * 일반적으로 안전. 다만 명시적 세션 격리 필요 시 호출자가 별도 인스턴스 생성.
 *
 * @since smuxapi-demo 0.10.0
 */
@Component
public class PromptResponseCollector {

    private static final Logger log = LogManager.getLogger(PromptResponseCollector.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Value("${smuxapi.scenario.collect-enabled:false}")
    private boolean collectEnabled;

    @Value("${smuxapi.scenario.output-path:./scenarios/}")
    private String outputPath;

    @Value("${smuxapi.scenario.file-prefix:test-scenario}")
    private String filePrefix;

    private String sessionId;
    private String aiModel;
    private final List<ScenarioTurn> turns = Collections.synchronizedList(new ArrayList<>());
    private ScenarioTurn currentTurn;

    public boolean isEnabled() {
        return collectEnabled;
    }

    /** 세션 메타데이터 설정 — ChatRoomService 에서 첫 wrapping 시 1회 호출. */
    public synchronized void initSession(String sessionId, String aiModel) {
        this.sessionId = sessionId;
        this.aiModel = aiModel;
    }

    public String getSessionId() { return sessionId; }
    public String getAiModel() { return aiModel; }

    public int turnCount() { return turns.size(); }
    public List<ScenarioTurn> snapshotTurns() {
        return Collections.unmodifiableList(new ArrayList<>(turns));
    }

    /**
     * 새 턴 시작. ChattingCollector.sendPrompt 시작부에서 호출.
     */
    public synchronized void startNewTurn() {
        if (!collectEnabled) return;
        currentTurn = new ScenarioTurn(turns.size() + 1);
    }

    public synchronized void captureUiInfo(String uiInfo) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setUiInfo(uiInfo);
    }

    public synchronized void captureUserPrompt(String userPrompt) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setUserPrompt(userPrompt);
    }

    public synchronized void captureApiPrompt(String apiPrompt) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setApiPrompt(apiPrompt);
    }

    /**
     * 응답 캡처 + 턴 종료. message, action_queue 필드 추출.
     *
     * @param messageField "message" 값
     * @param actionQueueField "action_queue" 값 (JsonNode 또는 null)
     */
    public synchronized void captureResponse(String messageField, Object actionQueueField) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setResMsg(messageField);
        currentTurn.setActionQueue(actionQueueField);
        turns.add(currentTurn);
        currentTurn = null;
    }

    /**
     * 현재까지 수집된 턴들을 JSON 파일로 저장.
     *
     * @return 저장된 파일 경로 (비활성/빈 상태에서는 null)
     */
    public synchronized Path saveToFile() throws IOException {
        if (!collectEnabled || turns.isEmpty()) return null;

        Path dir = Paths.get(outputPath == null || outputPath.isEmpty() ? "./scenarios/" : outputPath);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String ts = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String safeSession = sessionId == null ? "nosession" : sessionId.replaceAll("[^a-zA-Z0-9._-]", "_");
        String prefix = filePrefix == null || filePrefix.isEmpty() ? "test-scenario" : filePrefix;
        String fileName = String.format("%s-%s-%s.json", prefix, safeSession, ts);
        Path file = dir.resolve(fileName);

        ObjectNode root = MAPPER.createObjectNode();
        root.put("schemaVersion", 1);
        root.put("sessionId", sessionId);
        root.put("aiModel", aiModel);
        root.put("savedAt", ZonedDateTime.now().toString());
        root.put("turnCount", turns.size());

        ArrayNode arr = MAPPER.createArrayNode();
        for (ScenarioTurn t : turns) {
            ObjectNode n = MAPPER.createObjectNode();
            n.put("turnNo", t.getTurnNo());
            n.put("uiInfo", t.getUiInfo());
            n.put("userPrompt", t.getUserPrompt());
            n.put("apiPrompt", t.getApiPrompt());
            n.put("resMsg", t.getResMsg());
            // action_queue 는 String / JsonNode / JSONObject 등 다양한 타입이 올 수 있으므로 toString → readTree
            if (t.getActionQueue() != null) {
                try {
                    n.set("actionQueue", MAPPER.readTree(t.getActionQueue().toString()));
                } catch (Exception e) {
                    n.put("actionQueue", t.getActionQueue().toString());
                }
            } else {
                n.putNull("actionQueue");
            }
            arr.add(n);
        }
        root.set("turns", arr);

        MAPPER.writeValue(file.toFile(), root);
        log.info("scenario file saved: {} ({} turns)", file.toAbsolutePath(), turns.size());
        return file;
    }

    /**
     * 세션 상태 초기화 — 테스트 및 재시작 용.
     */
    public synchronized void reset() {
        turns.clear();
        currentTurn = null;
        sessionId = null;
        aiModel = null;
    }
}

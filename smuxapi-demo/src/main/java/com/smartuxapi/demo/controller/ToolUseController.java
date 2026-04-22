package com.smartuxapi.demo.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.schema.SchemaBuilder;
import com.smartuxapi.ai.tools.ToolDefinition;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolResult;
import com.smartuxapi.ai.vision.VisionService;
import com.smartuxapi.ai.vision.VisionServiceFactory;
import com.smartuxapi.ai.vision.VisionTools;
import com.smartuxapi.demo.service.ChatRoomService;
import com.smartuxapi.util.PropertiesUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Tool Use 데모 컨트롤러 (smart-ux-api v0.8.0 T2-b).
 *
 * <p>POST /demo/tools
 * <pre>{
 *   "ai_model": "chatgpt" | "gemini",
 *   "user_msg": "...",
 *   "enable_vision": true|false   // 기본 true, VisionTools 자동 등록
 * }</pre>
 *
 * <p>항상 등록되는 기본 tool:
 * <ul>
 *   <li><b>getTime</b> — 현재 서버 시각 반환 (네트워크 호출 없음, 데모 안전)</li>
 *   <li><b>scanImage</b> — enable_vision=true 일 때. OpenAI Vision 기반.</li>
 * </ul>
 *
 * <p>반환: smart-ux-api 의 sendPromptWithTools 결과 그대로.
 * <pre>{
 *   "message": "<최종 텍스트>",
 *   "action_queue": {...},
 *   "tool_calls": [ { id, toolName, arguments, result, isError } ]
 * }</pre>
 */
@RestController
@RequestMapping("/demo/tools")
public class ToolUseController {

    private static final Logger log = LogManager.getLogger(ToolUseController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ChatRoomService chatRoomService;

    public ToolUseController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> handlePost(@RequestBody String body, HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");

        JsonNode root;
        try {
            root = MAPPER.readTree(body == null ? "{}" : body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("Invalid JSON: " + e.getMessage()));
        }

        String userMsg = root.path("user_msg").asText("");
        String aiModel = root.path("ai_model").asText(null);
        boolean enableVision = root.path("enable_vision").asBoolean(true);

        if (userMsg.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("user_msg is required"));
        }

        HttpSession sess = req.getSession(true);
        ChatRoom chatRoom = chatRoomService.getChatRoom(aiModel, sess);
        if (chatRoom == null || chatRoom.getChatting() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("No chat room — check ai_model"));
        }

        ToolRegistry tools = new ToolRegistry();
        tools.register(getTimeTool());

        if (enableVision) {
            ToolDefinition visionTool = tryBuildVisionTool(chatRoom);
            if (visionTool != null) tools.register(visionTool);
        }

        log.info("[tools] model={}, tools={}, msg='{}'", aiModel, tools.size(), userMsg);

        try {
            JSONObject res = chatRoom.getChatting().sendPromptWithTools(userMsg, tools);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("sendPromptWithTools 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /**
     * 가장 단순한 데모 tool — 현재 서버 시각 반환. 네트워크 호출 없음.
     */
    private ToolDefinition getTimeTool() {
        return new ToolDefinition(
                "getTime",
                "현재 서버 시각을 ISO-8601 문자열로 반환한다. 시간 관련 질문일 때 호출.",
                SchemaBuilder.object().build(),
                call -> {
                    ObjectNode out = MAPPER.createObjectNode();
                    out.put("iso8601", java.time.Instant.now().toString());
                    out.put("epochSec", java.time.Instant.now().getEpochSecond());
                    return ToolResult.ok(call.getId(), out);
                });
    }

    /**
     * OpenAI API 키가 있으면 scanImage Tool 등록. 없으면 null.
     */
    private ToolDefinition tryBuildVisionTool(ChatRoom chatRoom) {
        String key = PropertiesUtil.get("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            log.warn("OPENAI_API_KEY 미설정 — scanImage tool 등록 생략");
            return null;
        }
        VisionService vision = VisionServiceFactory.createOpenAI(key);
        return VisionTools.scanImageTool(vision, chatRoom.getActionQueueHandler());
    }

    @SuppressWarnings("unchecked")
    private JSONObject err(String msg) {
        JSONObject o = new JSONObject();
        o.put("status", "error");
        o.put("message", msg);
        return o;
    }
}

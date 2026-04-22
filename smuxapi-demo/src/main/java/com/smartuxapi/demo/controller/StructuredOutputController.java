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
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.schema.SchemaBuilder;
import com.smartuxapi.demo.service.ChatRoomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Structured Output 데모 컨트롤러 (smart-ux-api v0.8.0 T2-a).
 *
 * <p>POST /demo/structured
 * <pre>{
 *   "ai_model": "chatgpt" | "gemini",
 *   "user_msg": "...",
 *   "schema": "userProfile" | "uiIntent"   // 사전 정의된 스키마 키
 * }</pre>
 *
 * <p>반환: smart-ux-api 의 sendPromptWithSchema 결과 그대로.
 * <pre>{
 *   "message": "<JSON 문자열 원문>",
 *   "action_queue": {...},
 *   "structured": { ... 파싱된 JsonNode ... } | null
 * }</pre>
 */
@RestController
@RequestMapping("/demo/structured")
public class StructuredOutputController {

    private static final Logger log = LogManager.getLogger(StructuredOutputController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ChatRoomService chatRoomService;

    public StructuredOutputController(ChatRoomService chatRoomService) {
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
        String schemaKey = root.path("schema").asText("userProfile");

        if (userMsg.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("user_msg is required"));
        }

        HttpSession sess = req.getSession(true);
        ChatRoom chatRoom = chatRoomService.getChatRoom(aiModel, sess);
        if (chatRoom == null || chatRoom.getChatting() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("No chat room — check ai_model"));
        }

        ResponseSchema schema = pickSchema(schemaKey);
        log.info("[structured] model={}, schema={}, msg='{}'", aiModel, schema.getName(), userMsg);

        try {
            JSONObject res = chatRoom.getChatting().sendPromptWithSchema(userMsg, schema);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("sendPromptWithSchema 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    /**
     * 데모용 사전 정의 스키마.
     */
    private ResponseSchema pickSchema(String key) {
        switch (key) {
            case "uiIntent":
                return SchemaBuilder.object()
                        .description("사용자 메시지에서 UI 의도를 추출")
                        .stringProperty("intent", "click / search / navigate / unknown 중 하나")
                        .stringProperty("target", "대상 UI 요소 설명 (없으면 unknown)")
                        .stringArrayProperty("keywords", "메시지의 핵심 키워드 3개 이하")
                        .required("intent", "target", "keywords")
                        .asResponse("UiIntent");

            case "userProfile":
            default:
                return SchemaBuilder.object()
                        .description("사용자 프로필 정보 추출")
                        .stringProperty("name", "사용자 이름")
                        .integerProperty("age", "나이")
                        .stringArrayProperty("hobbies", "취미 목록 (없으면 빈 배열)")
                        .required("name", "age", "hobbies")
                        .asResponse("UserProfile");
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject err(String msg) {
        JSONObject o = new JSONObject();
        o.put("status", "error");
        o.put("message", msg);
        return o;
    }
}

package com.smartuxapi.demo.controller;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.demo.service.ChatRoomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * UI 정보 수집 컨트롤러
 * 
 * @author KIUNSEA
 */
@RestController
@RequestMapping("/collect")
public class UXInfoController {
    
    private static final Logger log = LogManager.getLogger(UXInfoController.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ChatRoomService chatRoomService;
    
    public UXInfoController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> handlePost(@RequestBody String requestBody, HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        return handleGet(requestBody, req);
    }
    
    @GetMapping
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONObject> handleGet(@RequestBody(required = false) String requestBody, HttpServletRequest req) throws IOException {
        
        // 요청 본문이 없으면 에러
        if (requestBody == null || requestBody.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Request body is required"));
        }

        // JSON 파싱
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(requestBody);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("Invalid JSON: " + e.getMessage()));
        }

        // 요소 추출 (예: timestamp와 elements 배열)
        JsonNode timestampNode = rootNode.get("timestamp");
        JsonNode elementsNode = rootNode.get("elements");

        log.debug("📦 Timestamp: " + (timestampNode != null ? timestampNode.toString() : "null"));
        log.debug("📦 Elements JSON: " + elementsNode);

        // 필요 시 저장 또는 DB 처리 추가 가능
        try {
            String aiModel = rootNode.has("ai_model") ? rootNode.get("ai_model").asText() : null;
            HttpSession sess = req.getSession(true);
            ChatRoom chatRoom = chatRoomService.getChatRoom(aiModel, sess);
            if (chatRoom != null) {
                chatRoom.getActionQueueHandler().setCurrentViewInfo(elementsNode.toString());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 응답 반환
        JSONObject resJson = new JSONObject();
        resJson.put("status", "ok");
        return ResponseEntity.ok(resJson);
    }
    
    private JSONObject createErrorResponse(String message) {
        JSONObject error = new JSONObject();
        error.put("status", "error");
        error.put("message", message);
        return error;
    }
}

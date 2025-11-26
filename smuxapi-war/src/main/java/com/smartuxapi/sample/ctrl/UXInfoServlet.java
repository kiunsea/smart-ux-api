package com.smartuxapi.sample.ctrl;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.sample.ChatRoomServ;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author KIUNSEA
 */
@WebServlet("/collect")
@MultipartConfig
public class UXInfoServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private Logger log = LogManager.getLogger(UXInfoServlet.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	doGet(req, res);
    }
    
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        // 1. 요청의 Content-Type 확인
        if (!"application/json".equalsIgnoreCase(req.getContentType())) {
        	res.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Only application/json supported.");
            return;
        }

        // 2. 요청 본문 읽기
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        // 3. JSON 파싱
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(sb.toString());
        } catch (Exception e) {
        	res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON: " + e.getMessage());
            return;
        }

        // 4. 요소 추출 (예: timestamp와 elements 배열)
        JsonNode timestampNode = rootNode.get("timestamp");
        JsonNode elementsNode = rootNode.get("elements");

        log.debug("📦 Timestamp: " + (timestampNode != null ? timestampNode.toString() : "null"));
        log.debug("📦 Elements JSON: " + elementsNode);

        // 5. 필요 시 저장 또는 DB 처리 추가 가능
        try {
            String aiModel = rootNode.get("ai_model").asText();
            ChatRoom chatRoom = ChatRoomServ.getInstance().getChatRoom(aiModel, req.getSession(true), this);
            if (chatRoom != null) {
                chatRoom.getActionQueueHandler().setCurrentViewInfo(elementsNode.toString());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 6. 응답 반환
        JSONObject resJson = new JSONObject();
        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);
        resJson.put("status", "ok");
        res.getWriter().write(resJson.toJSONString());
	}
}

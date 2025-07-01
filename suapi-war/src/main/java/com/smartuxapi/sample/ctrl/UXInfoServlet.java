package com.smartuxapi.sample.ctrl;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.openai.OpenAIChatRoom;
import com.smartuxapi.ai.openai.assistants.Assistant;
import com.smartuxapi.util.PropertiesUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author KIUNSEA
 */
@WebServlet("/collect")
@MultipartConfig
public class UXInfoServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private Logger log = LogManager.getLogger(UXInfoServlet.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public void init() {
        PropertiesUtil.USER_PROPERTIES_PATH = this.getServletContext().getRealPath("/")
                + "WEB-INF/classes/resources/suapi.properties";
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	doGet(req, res);
    }
    
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

        System.out.println("📦 Timestamp: " + (timestampNode != null ? timestampNode.toString() : "null"));
        System.out.println("📦 Elements JSON: " + elementsNode);

        // 5. 필요 시 저장 또는 DB 처리 추가 가능
        HttpSession sess = req.getSession(true);
        Object usObj = sess.getAttribute("CHAT_ROOM");
        
	    String openaiApiKey = PropertiesUtil.get("OPENAI_API_KEY");
	    String openaiAssistId = PropertiesUtil.get("OPENAI_ASSIST_ID");

	    JSONObject resJson = new JSONObject();
		try {
			OpenAIChatRoom cr = null;
			if (usObj != null) {
				cr = (OpenAIChatRoom) usObj;
			} else {
				Assistant assist = new Assistant(openaiAssistId);
				assist.setApiKey(openaiApiKey);
				cr = new OpenAIChatRoom(assist);
			}
			cr.setCurrentViewInfo(elementsNode.toString());
			sess.setAttribute("CHAT_ROOM", cr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

        // 6. 응답 반환
        res.setContentType("application/json");
        res.setStatus(HttpServletResponse.SC_OK);
        resJson.put("status", "ok");
        res.getWriter().write(resJson.toJSONString());
	}
}

package com.smartuxapi.sample;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.gemini.GeminiChatRoom;
import com.smartuxapi.ai.openai.assistants.Assistants;
import com.smartuxapi.ai.openai.assistants.AssistantsThread;
import com.smartuxapi.util.FileUtil;
import com.smartuxapi.util.PropertiesUtil;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class ChatRoomServ {
    
    private Logger log = LogManager.getLogger(ChatRoomServ.class);
    private static ChatRoomServ instance;
    
    private ChatRoomServ() {
        ;
    }
    
    public static synchronized ChatRoomServ getInstance() {
        if (instance == null) {
            System.out.println("[ChatRoomServ.java] Create New ChatRoomServ !!");
            instance = new ChatRoomServ();
        }
        return instance;
    }

    /**
     * ChatRoom을 생성하고 세션에 저장 (Query Parameter)
     * @param req
     * @return
     */
    public ChatRoom getChatRoom(HttpServlet serv, HttpServletRequest req) {
        
        HttpSession sess = req.getSession(true);
        
        Object amObj = sess.getAttribute("AI_MODEL");
        String aiModel = null;
        if (amObj != null) {
            aiModel = amObj.toString();
        } else {
            aiModel = req.getParameter("ai_model");
            sess.setAttribute("AI_MODEL", aiModel);
        }
        
        if (aiModel == null) {
            log.info("[Warning] 선택된 AI모델이 없습니다. 진행을 중지합니다.");
            return null;
        }
        
        Object crObj = sess.getAttribute("CHAT_ROOM");
        ChatRoom chatRoom = null;
        if (crObj != null) {
            chatRoom = (ChatRoom) crObj;
        } else {
            
            if ("chatgpt".equals(aiModel)) {
                String openaiApiKey = PropertiesUtil.get("OPENAI_API_KEY");
                String openaiAssistId = PropertiesUtil.get("OPENAI_ASSIST_ID");
                Assistants assist = new Assistants(openaiAssistId);
                assist.setApiKey(openaiApiKey);
                try {
                    chatRoom = new AssistantsThread(assist);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                log.info("[" + sess.getId() + "] OpenAI Assistants API를 사용합니다");
            } else {
                String apiKey = PropertiesUtil.get("GEMINI_API_KEY");
                String model = PropertiesUtil.get("GEMINI_API_MODEL");
                chatRoom = new GeminiChatRoom(apiKey, model);
                log.info("[" + sess.getId() + "] Gemini API를 사용합니다");
            }
            
            try {
                StringBuilder sb = FileUtil.readFile(
                        serv.getServletContext().getRealPath("/") + "WEB-INF/classes/resources/easy_kiosc_uif.json", null);
                chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + sb);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sess.setAttribute("CHAT_ROOM", chatRoom);
        }
        
        return chatRoom;
    }
    
    /**
     * ChatRoom을 생성하고 세션에 저장 (JSON Body Parameter)
     * 
     * @param aiModel
     * @param sess
     * @param serv
     * @return
     */
    public ChatRoom getChatRoom(String aiModel, HttpSession sess, HttpServlet serv) {
        
        if (aiModel == null || aiModel.trim().length() < 1) {
            log.info("[Warning] 선택된 AI모델이 없습니다. 진행을 중지합니다.");
            return null;
        }
        
        Object crObj = sess.getAttribute("CHAT_ROOM");
        ChatRoom chatRoom = null;
        if (crObj != null) {
            chatRoom = (ChatRoom) crObj;
        } else {
            
            if ("chatgpt".equals(aiModel)) {
                String openaiApiKey = PropertiesUtil.get("OPENAI_API_KEY");
                String openaiAssistId = PropertiesUtil.get("OPENAI_ASSIST_ID");
                Assistants assist = new Assistants(openaiAssistId);
                assist.setApiKey(openaiApiKey);
                try {
                    chatRoom = new AssistantsThread(assist);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                log.info("[" + sess.getId() + "] OpenAI Assistants API를 사용합니다");
            } else {
                String apiKey = PropertiesUtil.get("GEMINI_API_KEY");
                String model = PropertiesUtil.get("GEMINI_API_MODEL");
                chatRoom = new GeminiChatRoom(apiKey, model);
                log.info("[" + sess.getId() + "] Gemini API를 사용합니다");
            }
            
            try {
                StringBuilder sb = FileUtil.readFile(
                        serv.getServletContext().getRealPath("/") + "WEB-INF/classes/resources/easy_kiosc_uif.json", null);
                chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + sb);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sess.setAttribute("CHAT_ROOM", chatRoom);
        }
        
        return chatRoom;
    }
    
}

package com.smartuxapi.sample;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.ConfigLoader;
import com.smartuxapi.ai.gemini.GeminiChatRoom;
import com.smartuxapi.ai.openai.ResponsesChatRoom;
import com.smartuxapi.ai.openai.assistants.Assistants;
import com.smartuxapi.ai.openai.assistants.AssistantsThread;
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
            
            chatRoom = createChatRoom(aiModel);
            
            try {
                JsonNode uifJson = ConfigLoader.loadConfigFromClasspath("easy_kiosc_uif.json");
                chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + uifJson);
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
            log.info("[Warning] 사용할 AI모델을 선택해야 합니다.");
            return null;
        }
        
        Object crObj = sess.getAttribute("CHAT_ROOM");
        ChatRoom chatRoom = null;
        if (crObj != null) {
            
            chatRoom = (ChatRoom) crObj;
            
        } else {
            
            chatRoom = createChatRoom(aiModel);
            
            try {
                JsonNode uifJson = ConfigLoader.loadConfigFromClasspath("easy_kiosc_uif.json");
                chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + uifJson);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sess.setAttribute("CHAT_ROOM", chatRoom);
        }
        
        return chatRoom;
    }
    
    private ChatRoom createChatRoom(String aiModel) {
        
        ChatRoom chatRoom = null;
        
        if ("chatgpt".equals(aiModel)) {
            String apiKey = PropertiesUtil.get("OPENAI_API_KEY");
            String model = PropertiesUtil.get("OPENAI_MODEL");
            chatRoom = new ResponsesChatRoom(apiKey, model);
            log.info("[" + chatRoom.getId() + "] OpenAI Responses API를 사용합니다");
        } else if ("gemini".equals(aiModel)){
            String apiKey = PropertiesUtil.get("GEMINI_API_KEY");
            String model = PropertiesUtil.get("GEMINI_MODEL");
            chatRoom = new GeminiChatRoom(apiKey, model);
            log.info("[" + chatRoom.getId() + "] Gemini API를 사용합니다");
        } else {
            String apiKey = PropertiesUtil.get("OPENAI_API_KEY");
            String assistId = PropertiesUtil.get("OPENAI_ASSIST_ID");
            Assistants assist = new Assistants(assistId);
            assist.setApiKey(apiKey);
            try {
                chatRoom = new AssistantsThread(assist);
                if (chatRoom != null) {
                    log.info("[" + chatRoom.getId() + "] OpenAI Assistants API를 사용합니다");
                }
            } catch (ParseException e) {
                e.printStackTrace();
                log.error("OpenAI Assistants Thread 생성 실패", e);
            }
        }
        
        if (chatRoom != null) {
            chatRoom.setActionQueueHandler(new ActionQueueHandler());
        }
        
        return chatRoom;
    }
}

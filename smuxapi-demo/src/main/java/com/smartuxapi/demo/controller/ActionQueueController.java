package com.smartuxapi.demo.controller;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.demo.service.ChatRoomService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Action Queue 처리 컨트롤러
 * 
 * @author KIUNSEA
 */
@RestController
@RequestMapping("/action")
public class ActionQueueController {

    private static final Logger log = LogManager.getLogger(ActionQueueController.class);
    private final ChatRoomService chatRoomService;

    public ActionQueueController(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }
    
    @PostMapping
    public JSONObject handlePost(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");
        return handleGet(req);
    }
    
    @GetMapping
    @SuppressWarnings("unchecked")
    public JSONObject handleGet(HttpServletRequest req) {

        ChatRoom chatRoom = chatRoomService.getChatRoom(req);

        JSONObject resObj = new JSONObject();
        String userMsg = req.getParameter("user_msg");
        log.debug("UserMsg : " + userMsg);
        JSONObject resJson = null;
        try {
            if (chatRoom != null && chatRoom.getChatting() != null) {
                resJson = chatRoom.getChatting().sendPrompt((userMsg != null ? userMsg : ""));
                log.debug("======================================================================");
                log.debug("resJson - " + resJson.toJSONString());
                resJson.get("message").toString();
                if (resJson.containsKey("action_queue")) {
                    resObj.put("action_queue", resJson.get("action_queue"));
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        log.debug("======================================================================");
        log.debug(resObj.toJSONString());

        return resObj;
    }
}

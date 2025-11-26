package com.smartuxapi.sample.ctrl;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

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
@WebServlet("/action")
@MultipartConfig
public class ActionQueueServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private Logger log = LogManager.getLogger(ActionQueueServlet.class);
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	doGet(req, res);
    }
    
    @SuppressWarnings("unchecked")
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        
        ChatRoom chatRoom = ChatRoomServ.getInstance().getChatRoom(this, req);
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

        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache");
        PrintWriter out = res.getWriter();
        out.println(resObj);
        out.flush();
        out.close();
        
	}
    
}

package com.omnibuscode.ai;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.ChatManager;
import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.ProcessFunction;
import com.omnibuscode.ai.openai.Assistant;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 사용자 발화를 처리하기 위한 servlet
 * @author KIUNSEA
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        
    	JSONObject resObj = new JSONObject();

    	ChatManager cm = new ChatManager();
		ChatRoom cr = null;
		
		HttpSession sess = req.getSession(true);
		Object usObj = sess.getAttribute("CHAT_ROOM");
		if (usObj != null) {
			cr = (ChatRoom) usObj;
		}
		if (cr == null) {
			String assistantId = "OPENAI_ASSIST_ID"; //TODO 키값을 가져오게 해야 한다.(하드코딩은 X)
			Assistant assist = new Assistant(assistantId);
			String apiKey = "OPENAI_API_KEY";
			assist.setApiKey(apiKey);

			cm.setAssistant(assist); //assistant 등록
			JSONObject jo;
			try {
				jo = cm.createChatRoom(ChatManager.AI_NAME_OPENAI);
				cr = (ChatRoom) jo.get("instance");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sess.setAttribute("CHAT_ROOM", cr);
		}
    	
		String userMsg = req.getParameter("user_msg");
		String resMsg = null;
		JSONObject resJson;
		try {
			resJson = cr.sendMessage((userMsg != null ? userMsg : ""));
			resMsg = resJson.get("message").toString();
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //메세지 전송
		
		resObj.put("assist_msg", resMsg);
    	if (resObj == null) resObj = new JSONObject();
    	
    	res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-cache");
        PrintWriter out = res.getWriter();
        out.println(resObj);
        out.flush();
        out.close();
    }

}

package com.omnibuscode.ai;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.manager.ChatManager;
import com.omnibuscode.ai.openai.Assistant;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 2025.04.15 사용 안하기로 결정했다.
 * 어차피 설계 복잡도만 높아질뿐 효용성이 그리 높아 보이지 않아서이다.
 * 그냥 기존처럼 패키지의 클래스들을 이용해서 ai response message 를 취하고
 * 직접 처리하면 그게 더 간단해 보인다.
 * 이 클래스는 당분간 유지하다가 코드 재사용도 없어 보이면 삭제하도록 한다.
 * 
 * ========================================================
 * 사용자 발화를 처리하기 위한 Servlet
 * 이 클래스를 상속받아 super.doPost() 를 호출하게 되면 AI에게 발화하고 응답을 받을 수 있다.
 * defaultPrint 값을 true로 설정하면 기본 출력이 가능하며
 * doPost() 함수 호출 이후 ChatManager, ChatRoom, Assistant Message 를 취할 수 있다. 
 * @author KIUNSEA
 */
@WebServlet("/chat")
public class ChatServletBack extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private String openaiAssistId = null;
    private String openaiApiKey = null;
    
    private ChatManager cm = null;
    private ChatRoom cr = null;
    private String assistMsg = null;
    
    private boolean defaultPrint = false;
    
    public void setOpenaiAssistId(String id) {
    	this.openaiAssistId = id;
    }
    
    public void setOpenaiApiKey(String key) {
    	this.openaiApiKey = key;
    }
    
	public void setDefaultPrint(boolean defPrint) {
		this.defaultPrint = defPrint;
	}
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        doGet(req, res);
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        
    	JSONObject resObj = new JSONObject();

		if (this.openaiAssistId == null || this.openaiApiKey == null) {
			resObj.put("sys_msg", "ai 사용권한이 없습니다.");
		} else {
			this.cm = new ChatManager();

			HttpSession sess = req.getSession(true);
			Object usObj = sess.getAttribute("CHAT_ROOM");
			if (usObj != null) {
				this.cr = (ChatRoom) usObj;
			}
			if (this.cr == null) {

				Assistant assist = new Assistant(this.openaiAssistId);
				assist.setApiKey(this.openaiApiKey);

				this.cm.setAssistant(assist); // assistant 등록
				JSONObject jo;
				try {
					jo = this.cm.createChatRoom(ChatManager.AI_NAME_OPENAI);
					this.cr = (ChatRoom) jo.get("instance");
				} catch (Exception e) {
					e.printStackTrace();
				}
				sess.setAttribute("CHAT_ROOM", this.cr);
			}

			String userMsg = req.getParameter("user_msg");
			String assistMsg = null;
//			JSONObject resJson;
//			try {
//				resJson = this.cr.sendMessage((userMsg != null ? userMsg : ""));
//				assistMsg = resJson.get("message").toString();
//			} catch (IOException | ParseException e) {
//				e.printStackTrace();
//			} // 메세지 전송

			resObj.put("assist_msg", assistMsg);
		}
    	
		if (this.defaultPrint) {
			res.setCharacterEncoding("UTF-8");
			res.setContentType("application/json;charset=UTF-8");
			res.setHeader("Cache-Control", "no-cache");
			PrintWriter out = res.getWriter();
			out.println(resObj);
			out.flush();
			out.close();
		}
    }

	public ChatManager getCm() {
		return this.cm;
	}

	public ChatRoom getCr() {
		return this.cr;
	}

	public String getAssistMsg() {
		return this.assistMsg;
	}
}

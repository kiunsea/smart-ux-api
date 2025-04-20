package ctrl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.manager.ActionQueueManager;
import com.omnibuscode.ai.manager.ChatManager;
import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.Assistant;

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
@WebServlet("/action")
@MultipartConfig
public class ActionServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	doGet(req, res);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String openaiAssistId = "asst_6T4VCQSWs0R6WrBZRxsiXiFJ";
	    String openaiApiKey = "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";
		
		JSONObject resObj = new JSONObject();

		if (openaiAssistId == null || openaiApiKey == null) {
			resObj.put("sys_msg", "ai 사용권한이 없습니다.");
		} else {
			ChatManager cm = new ChatManager();

			HttpSession sess = req.getSession(true);
			Object usObj = sess.getAttribute("CHAT_ROOM");
			ChatRoom cr = null;
			Chatting chat = null;
			if (usObj != null) {
				cr = (ChatRoom) usObj;
			}
			if (cr == null) {

				Assistant assist = new Assistant(openaiAssistId);
				assist.setApiKey(openaiApiKey);

				cm.setAssistant(assist); // assistant 등록
				JSONObject jo;
				try {
					jo = cm.createChatRoom(ChatManager.AI_NAME_OPENAI);
					cr = (ChatRoom) jo.get("instance");
					chat = cr.createChatting(1);
				} catch (Exception e) {
					e.printStackTrace();
				}
				sess.setAttribute("CHAT_ROOM", cr);
			}

			String userMsg = req.getParameter("user_msg");
			String assistMsg = null;
			JSONObject resJson = null;
			if (chat != null) {
				try {
					resJson = chat.sendMessage((userMsg != null ? userMsg : ""));
					assistMsg = resJson.get("message").toString();
					if (resJson.containsKey("actionQueue")) {
						ActionQueueManager aqm = new ActionQueueManager(
								req.getSession(),
								(JSONArray) resJson.get("actionQueue"));
						if (!aqm.isEmpty()) {
							resObj.put("action_queue", aqm.currentQueue());
						}
					}
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
			
			resObj.put("assist_msg", assistMsg);
		}
		
		System.out.println("======================================================================");
		System.out.println(resObj.toJSONString());

		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/json;charset=UTF-8");
		res.setHeader("Cache-Control", "no-cache");
		PrintWriter out = res.getWriter();
		out.println(resObj);
		out.flush();
		out.close();
	}
}

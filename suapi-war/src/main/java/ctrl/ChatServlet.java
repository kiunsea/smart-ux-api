package ctrl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.ChatManager;
import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.openai.Assistant;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @author KIUNSEA
 */
@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    private String openaiAssistId = null;
    private String openaiApiKey = null;

    public void setOpenaiAssistId(String id) {
    	this.openaiAssistId = id;
    }
    
    public void setOpenaiApiKey(String key) {
    	this.openaiApiKey = key;
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
			ChatManager cm = new ChatManager();

			HttpSession sess = req.getSession(true);
			Object usObj = sess.getAttribute("CHAT_ROOM");
			ChatRoom cr = null;
			if (usObj != null) {
				cr = (ChatRoom) usObj;
			}
			if (cr == null) {

				Assistant assist = new Assistant(this.openaiAssistId);
				assist.setApiKey(this.openaiApiKey);

				cm.setAssistant(assist); // assistant 등록
				JSONObject jo;
				try {
					jo = cm.createChatRoom(ChatManager.AI_NAME_OPENAI);
					cr = (ChatRoom) jo.get("instance");
				} catch (Exception e) {
					e.printStackTrace();
				}
				sess.setAttribute("CHAT_ROOM", cr);
			}

			String userMsg = req.getParameter("user_msg");
			String assistMsg = null;
			JSONObject resJson;
			try {
				resJson = cr.sendMessage((userMsg != null ? userMsg : ""));
				assistMsg = resJson.get("message").toString();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			
			resObj.put("assist_msg", assistMsg);
		}

		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/json;charset=UTF-8");
		res.setHeader("Cache-Control", "no-cache");
		PrintWriter out = res.getWriter();
		out.println(resObj);
		out.flush();
		out.close();
	}
}

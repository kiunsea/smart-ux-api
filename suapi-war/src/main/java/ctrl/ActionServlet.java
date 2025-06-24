package ctrl;

import java.io.IOException;
import java.io.PrintWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.OpenAIChatRoom;
import com.omnibuscode.ai.openai.assistants.Assistant;
import com.omnibuscode.ai.openai.decorator.ActionQueueDecorator;

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
			HttpSession sess = req.getSession(true);
			Object usObj = sess.getAttribute("CHAT_ROOM");
			ChatRoom cr = null;
			Chatting chat = null;
			
			Assistant assist = new Assistant(openaiAssistId);
			assist.setApiKey(openaiApiKey);

			JSONObject jo;
			try {
				if (usObj != null) {
					cr = (ChatRoom) usObj;
				} else {
					cr = new OpenAIChatRoom(assist); // ChatRoom 을 세션에 담아 재사용했더니 자꾸 이전 명령어에 맞춰 응답한다. 
														// 프롬프트 작성하기 귀찮아서 그냥 매번 새로 생성하도록 정했다.
				}
				chat = cr.createChatting();
				chat = cr.decorateActionQueue(chat);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String userMsg = req.getParameter("user_msg");
			System.out.println("um="+userMsg);
			String assistMsg = null;
			JSONObject resJson = null;
			try {
				if (chat != null) {
					resJson = chat.sendMessage((userMsg != null ? userMsg : ""));
//					System.out.println("======================================================================");
//					System.out.println("resJson - " + resJson.toJSONString());
					assistMsg = resJson.get("message").toString();
					if (resJson.containsKey("action_queue")) {
						resObj.put("action_queue", resJson.get("action_queue"));
					}
				}
				cr.closeChat();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
			
			//resObj.put("assist_msg", assistMsg);
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

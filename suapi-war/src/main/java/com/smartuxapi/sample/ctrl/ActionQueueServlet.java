package com.smartuxapi.sample.ctrl;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.openai.OpenAIChatRoom;
import com.smartuxapi.ai.openai.assistants.Assistant;
import com.smartuxapi.util.FileUtil;
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
@WebServlet("/action")
@MultipartConfig
public class ActionQueueServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private Logger log = LogManager.getLogger(ActionQueueServlet.class);
    
    public void init() {
        PropertiesUtil.USER_PROPERTIES_PATH = this.getServletContext().getRealPath("/")
                + "WEB-INF/classes/resources/suapi.properties";
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	req.setCharacterEncoding("UTF-8");
    	doGet(req, res);
    }
    
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    	
	    String openaiApiKey = PropertiesUtil.get("OPENAI_API_KEY");
	    String openaiAssistId = PropertiesUtil.get("OPENAI_ASSIST_ID");
	    
		Assistant assist = new Assistant(openaiAssistId);
		assist.setApiKey(openaiApiKey);
		HttpSession sess = req.getSession(true);
		Object usObj = sess.getAttribute("CHAT_ROOM");
		ChatRoom cr = null;
		
		JSONObject resObj = new JSONObject();

		if (openaiAssistId == null || openaiApiKey == null) {
			resObj.put("sys_msg", "ai 사용권한이 없습니다.");
		} else {
			String procType = req.getParameter("proc_type");
			if (procType != null && "dynamic".equals(procType)) {//동적으로 화면 전환

				JSONObject jo;
				try {
					if (usObj != null) {
						cr = (ChatRoom) usObj;
					} else {
						cr = new OpenAIChatRoom(assist);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				String userMsg = req.getParameter("user_msg");
				log.debug("UserMsg : " + userMsg);
				String assistMsg = null;
				JSONObject resJson = null;
				try {
					if (cr.getChatting() != null) {
						resJson = cr.getChatting().sendMessage((userMsg != null ? userMsg : ""));
						log.debug("======================================================================");
						log.debug("resJson - " + resJson.toJSONString());
						assistMsg = resJson.get("message").toString();
						if (resJson.containsKey("action_queue")) {
							resObj.put("action_queue", resJson.get("action_queue"));
						}
					}
				} catch (IOException | ParseException e) {
					e.printStackTrace();
				}
			}
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

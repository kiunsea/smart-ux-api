package com.smartuxapi.ai.openai;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.ConfigLoader;
import com.smartuxapi.ai.openai.assistants.APIConnection;
import com.smartuxapi.util.ActionQueueUtil;

public class ActionQueueChatting extends OpenAIChatting {
	
	private Logger log = LogManager.getLogger(ActionQueueChatting.class);
	
    private String curViewInfo = null; //CHATROOM_TYPE_UXINFO 인 경우 현재 화면 정보
    private boolean updateCurView = false;
	
	public ActionQueueChatting(Chatting chatting, APIConnection connApi, String idThread) {
		super(chatting, connApi, idThread);
	}
	
	protected ActionQueueChatting(APIConnection connApi, String idThread) {
		super(connApi, idThread);
	}
	
	/**
	 * 현재 화면 정보 저장
	 * @param curViewInfo(json string)
	 */
	public void setCurViewInfo(String curViewInfo) {
		this.curViewInfo = curViewInfo;
		this.updateCurView = true;
		log.debug("현재 화면 정보 저장 : " + curViewInfo);
	}
	
	public JSONObject sendMessage(String userMsg) throws IOException, ParseException {
		
		StringBuffer aqPromptSb = new StringBuffer();
		JsonNode config = ConfigLoader.loadConfigFromClasspath();
		
		Map<String, String> valueMap = new HashMap<>();
		valueMap.put("CurViewInfo", this.curViewInfo);
		valueMap.put("UserMsg", userMsg);
		StrSubstitutor sub = new StrSubstitutor(valueMap);
		
		Iterator<JsonNode> elements = null;
		if (config.get("prompt").get("cur_view_info").isArray()) {
			elements = config.get("prompt").get("cur_view_info").elements();
			while (elements.hasNext()) {
				JsonNode elementNode = elements.next();
				aqPromptSb.append(" " + sub.replace(elementNode));
			}
		}
		if (config.get("prompt").get("cur_view_info").isArray()) {
			elements = config.get("prompt").get("action_queue").elements();
			while (elements.hasNext()) {
				JsonNode elementNode = elements.next();
				aqPromptSb.append(" " + sub.replace(elementNode));
			}
		}
		
		log.debug("Action Queue Prompt : " + aqPromptSb);
		
		JSONObject resJson = super.sendMessage(aqPromptSb.toString());
		
		String resMsg = resJson.containsKey("message") ? resJson.get("message").toString() : null;
		JsonNode aqObj = ActionQueueUtil.extractActionQueue(resMsg);
		if (aqObj.hasNonNull("actionQueue")) {
			resJson.put("action_queue", aqObj.get("actionQueue"));
		} else {
			resJson.put("action_queue", aqObj);
		}
		
		return resJson;
	}
}

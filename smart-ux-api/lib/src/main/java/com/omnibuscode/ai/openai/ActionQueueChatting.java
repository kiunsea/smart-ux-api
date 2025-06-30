package com.omnibuscode.ai.openai;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.assistants.APIConnection;

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
		
		String userPrompt = "사용자의 \"" + userMsg + "\""
				+ " 명령을 수행 할 수 있도록 id를 actionQueue 로 해서 actionQueue JSON 내용을 만들어줘, 그런데 actionQueue 의 내용은 내가 현재 화면 정보의 dom element structure 를 참고해서 작성해줘";
		if (this.updateCurView) {
			userPrompt = "다음은 사용자가 현재 보고있는 화면에서 사용자 액션이 가능한 dom element structure야. 이전에 저장한 사용자 화면의 내용을 이것으로 교체해줘. 이 화면 정보를 이용해서 actionQueue 의 내용을 작성해야해. "
					+ this.curViewInfo + " 그리고 " + userPrompt;
			this.updateCurView = false;
		}

		JSONObject resJson = super.sendMessage(userPrompt);
		
		String resMsg = resJson.containsKey("message") ? resJson.get("message").toString() : null;
		if (resMsg != null) {
			JsonNode jObj = this.extractJsonBlock(resMsg);
			JsonNode aqArr = null;
			if (jObj != null) {
				aqArr = jObj.get("actionQueue");
			} else {
				aqArr = this.extractActionQueue(resMsg);
			}
			resJson.put("action_queue", aqArr);
		}
		
		return resJson;
	}
}

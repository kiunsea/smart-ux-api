package com.omnibuscode.ai.openai.decorator;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.OpenAIChatting;
import com.omnibuscode.ai.openai.assistants.APIConnection;

public class ActionQueueDecorator extends OpenAIChatting {

	public ActionQueueDecorator(Chatting chatting, APIConnection connApi, String idThread) {
		super(chatting, connApi, idThread);
	}
	
	protected ActionQueueDecorator(APIConnection connApi, String idThread) {
		super(connApi, idThread);
	}
	
	public JSONObject sendMessage(String userMsg) throws IOException, ParseException {
		
		String reqActions = 
                "\"" + userMsg + "\""
                + " 명령을 수행하기 위한 actionQueue JSON 을 작성해서 JSON 의 내용만 응답 메세지로 출력해줘, 그리고 JSON 내용에는 id 에 해당하는 selector 와 xpath 도 포함해줘";
		
		JSONObject resJson = super.sendMessage(reqActions);
		
		String resMsg = resJson.containsKey("message") ? resJson.get("message").toString() : null;
		if (resMsg != null) {
			JsonNode jObj = this.findJsonBlock(resMsg);
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

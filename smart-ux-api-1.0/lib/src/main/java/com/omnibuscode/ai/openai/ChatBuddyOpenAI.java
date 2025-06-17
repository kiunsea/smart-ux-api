package com.omnibuscode.ai.openai;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnibuscode.ai.ChatBuddy;
import com.omnibuscode.ai.ProcessFunction;
import com.omnibuscode.ai.manager.ChatManager;
import com.omnibuscode.ai.openai.connection.AssistantsConnection;

public class ChatBuddyOpenAI extends ChatBuddy {

	private Logger log = LogManager.getLogger(ChatBuddyOpenAI.class);
	ObjectMapper objMapper = new ObjectMapper();
	
	private Assistant assistInfo = null;
	private AssistantsConnection conn = null;
	private String threadId = null; // thread id
	private Map<String, JsonNode> messages = null; // 대화방에서의 대화 목록
	
	public ChatBuddyOpenAI(Assistant assistInfo, AssistantsConnection conn, String threadId) {
		this.messages = new HashMap<String, JsonNode>();
		this.assistInfo = assistInfo;
		this.conn = conn;
		this.threadId = threadId;
	}

	@Override
	public JSONObject sendMessage(String userMsg) throws IOException, ParseException {

		JSONObject resJson = new JSONObject();
		
		this.conn.createMessage(this.threadId, userMsg); //메세지 전달
		String runId = this.conn.createRun(this.threadId); //메세지 분석
		
		String runStatus = null;
		do {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			JsonNode runInfo = this.conn.retrieveRun(this.threadId, runId);
			runStatus = runInfo.get("status").asText();
			if ("requires_action".equals(runStatus)) {
				JsonNode toolCalls = runInfo.get("required_action").get("submit_tool_outputs").get("tool_calls");
				JSONObject usrFuncsRst = null;
				for (JsonNode tc : toolCalls) {
					if ("function".equals(tc.get("type").asText())) {
						JsonNode fJson = tc.get("function");
						String fName = fJson.get("name").asText();
						String args = fJson.get("arguments").asText();
						
						Map<String, ProcessFunction> usrFuncMap = this.assistInfo.getFunctions();
						if (usrFuncMap.containsKey(fName)) {
							ProcessFunction usrFunc = usrFuncMap.get(fName);
							JSONObject result = usrFunc.execFunction(fName, this.objMapper.readTree(args));
							
							if (usrFuncsRst == null) usrFuncsRst = new JSONObject();
							usrFuncsRst.put(fName, result);
							resJson.put(ChatManager.USER_FUNCTIONS_RESULT, usrFuncsRst);
						}
					}
				}
				this.conn.submitToolOutputs(toolCalls, this.threadId, runId);
			}
		} while (runStatus == null || !"completed".equals(runStatus));

		JsonNode msgArr = this.conn.listMessages(this.threadId);
		
		String resMsg = null;
		// 배열 노드 확인
		if (msgArr.isArray()) {
			// 배열 노드 순회
			String msg_id = null;
			for (JsonNode message : msgArr) {
				// 각 객체 노드의 값 출력
				msg_id = message.get("id").asText();
				if (!this.messages.containsKey(msg_id)) {
					this.messages.put(msg_id, message);
					if ("assistant".equals(message.get("role").asText())) {
						resMsg = message.get("content").get(0).get("text").get("value").asText();
					}
				}
			}
		} else {
			log.error("배열 형식이 아닙니다.");
		}
		
		resJson.put("message", resMsg);
		
		return resJson;
	}

}

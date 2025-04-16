package com.omnibuscode.ai.openai;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnibuscode.ai.ChatAction;
import com.omnibuscode.ai.openai.connection.ChatConnection;

public class ChatActionOpenAI extends ChatAction {

	private Logger log = LogManager.getLogger(ChatActionOpenAI.class);
	ObjectMapper objMapper = new ObjectMapper();
	
	private Assistant assistInfo = null;
	private ChatConnection conn = null;
	private String threadId = null; // thread id
	private Map<String, JsonNode> messages = null; // 대화방에서의 대화 목록
	
	public ChatActionOpenAI(Assistant assistInfo, ChatConnection conn, String threadId) {
		this.messages = new HashMap<String, JsonNode>();
		
		this.assistInfo = assistInfo;
		this.conn = conn;
		this.threadId = threadId;
	}
	
	@Override
	public JSONObject sendMessage(String userMsg) {
		return null;

	}

}

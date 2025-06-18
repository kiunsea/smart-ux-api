package com.omnibuscode.ai.openai;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.assistants.APIConnection;
import com.omnibuscode.ai.openai.assistants.OpenAIAssistant;
import com.omnibuscode.ai.openai.assistants.OpenAIThread;

public class OpenAIChatRoom implements ChatRoom {

	private OpenAIThread thread = null;
    private APIConnection conn = null;
    private Map<String, JsonNode> messages = null; // 대화방에서의 대화 목록
	
    public OpenAIChatRoom(APIConnection conn, OpenAIThread thread) {
        this.conn = conn;
        this.thread = thread;
    	this.messages = new HashMap<String, JsonNode>();
    }
    
	@Override
	public Chatting createChatting() {
		return new OpenAIChatting();
	}

	@Override
	public boolean closeChat() {
		// TODO Auto-generated method stub
		return false;
	}

}

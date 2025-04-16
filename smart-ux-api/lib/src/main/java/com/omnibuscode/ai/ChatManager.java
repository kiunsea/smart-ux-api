package com.omnibuscode.ai;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.openai.Assistant;

/**
 * AI 를 선택하여 Chatting Room 을 개설하고 AI 와 대화하는 인터페이스를 제공한다.
 */
public class ChatManager {

	private Logger log = LogManager.getLogger(ChatManager.class);
	
	public static String AI_NAME_OPENAI = "OPENAI";
	public static String AI_NAME_GEMINI = "GEMINI";
	public static String USER_FUNCTIONS_RESULT = "USR_FUNCS_RST";
	
	private Assistant assistInfo = null;
	private ChatRoom chatRoom = null; //채팅 스레드
	
	/**
	 * OpenAI Assistants API 접속시 필요한 정보 객체(Assistant)를 저장한다.
	 * @param aiInfo
	 */
	public void setAssistant(Assistant aiInfo) {
		this.assistInfo = aiInfo;
	}
	
	/**
	 * 채팅방 생성
	 * @param aiName : openai or gemini
	 * @return {result:실행결과, message:결과메세지, instance:ChatRoom}
	 * @throws Exception 
	 */
	public JSONObject createChatRoom(String aiName) throws Exception {
		
		JSONObject rtnInfo = new JSONObject();
		
		if (ChatManager.AI_NAME_OPENAI.equals(aiName)) {
			
		    /**
		     * validation
		     */
			if (this.assistInfo == null) {
				rtnInfo.put("message", "접속 정보(Assistants info)가 필요합니다. Assistant instance를 설정해 주세요.");
				rtnInfo.put("result", "false");
				return rtnInfo;
			}
		    if (this.assistInfo.getAssistantId() == null) {
		    	rtnInfo.put("message", "assistant_id 가 필요합니다.");
		    	rtnInfo.put("result", "false");
		    	return rtnInfo;
		    }
		    if (this.assistInfo.getApiKey() == null) {
		    	rtnInfo.put("message", "api_key 가 필요합니다.");
		    	rtnInfo.put("result", "false");
		    	return rtnInfo;
		    }
		    
			this.chatRoom = new com.omnibuscode.ai.openai.ChatThread(this.assistInfo);
			rtnInfo.put("result", "true");
			rtnInfo.put("instance", this.chatRoom);
			return rtnInfo;
		}
		
		return null;
	}
	
	/**
	 * 채팅방에 메세지를 전달하고 응답 메세지를 반환한다.
	 * @param userMsg
	 * @return
	 * @throws Exception 
	 */
	public JSONObject sendMessage(String userMsg) throws Exception {
		JSONObject resJson = null;

		if (userMsg == null || userMsg.trim().length() < 1) {
			resJson = new JSONObject();
			resJson.put("message", "사용자 메세지가 없습니다.");
			return resJson;
		}

		if (this.chatRoom == null) {
			resJson = new JSONObject();
			resJson.put("message", "ai가 선택되어야 합니다.");
			return resJson;
		}
		
		Chatting chat = this.chatRoom.createChatting();

		return chat.sendMessage(userMsg);
	}
	
	/**
	 * 채팅방 닫기
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public boolean closeChatRoom() throws IOException, ParseException {
		boolean closed = false;
		closed = this.chatRoom.closeChat();
		return closed;
	}

}

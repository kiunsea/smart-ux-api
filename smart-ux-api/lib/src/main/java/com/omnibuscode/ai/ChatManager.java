package com.omnibuscode.ai;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.openai.Assistant;

public class ChatManager {

	private Logger log = LogManager.getLogger(ChatManager.class);
	
	public static String AINAME_OPENAI = "OPENAI";
	public static String AINAME_GEMINI = "GEMINI";
	
	private Assistant assistInfo = null;
	private ChatRoom chatRoom = null; //채팅 스레드
	
	public void setAssistInfo(Assistant aiInfo) {
		this.assistInfo = aiInfo;
	}
	
	/**
	 * 채팅방 생성
	 * @param aiName : openai or gemini
	 * @return {result:실행결과, message:결과메세지, instance:ChatRoom}
	 */
	public JSONObject createChatRoom(String aiName) {
		
		JSONObject rtnInfo = new JSONObject();
		
		if (ChatManager.AINAME_OPENAI.equals(aiName)) {
			
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
	 */
	public JSONObject sendMessage(String userMsg) {
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
		
		try {
			resJson = this.chatRoom.sendMessage(userMsg);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		return resJson;
	}
	
	/**
	 * 채팅방 닫기
	 * @return
	 */
	public boolean closeChatRoom() {
		boolean closed = false;
		try {
			closed = this.chatRoom.closeChat();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		return closed;
	}

}

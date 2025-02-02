package com.omnibuscode.ai;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class ChatManager {

	private Logger log = LogManager.getLogger(ChatManager.class);
	
	public static String AINAME_OPENAI = "OPENAI";
	public static String AINAME_GEMINI = "GEMINI";
	
	private ChatRoom chatRoom = null;
	
	/**
	 * 채팅방 생성
	 * @param aiName : openai or gemini
	 * @param aiInfo
	 * @return
	 */
	public JSONObject createChatRoom(String aiName, JSONObject aiInfo) {
		
		JSONObject rtnInfo = new JSONObject();
		
		if (ChatManager.AINAME_OPENAI.equals(aiName)) {
			
			//TODO id와 key는 사용자가 소유해야하고 api에서의 기본값은 null 로 초기화 되어 있어야 한다. (테스트이므로 임시로 설정해놓음)
		    String assistantId = "asst_hsP6560JM3JiFi0HlU4gR8hZ";
		    String apiKey = "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";

		    /**
		     * validation
		     */
		    if (aiInfo == null) {
		    	rtnInfo.put("message", "접속 정보가 필요합니다.");
		    	rtnInfo.put("result", "false");
		    	return rtnInfo;
		    }
		    if (aiInfo.containsKey("assistant_id")) {
		    	assistantId = aiInfo.get("assistant_id").toString();
		    } else {
		    	rtnInfo.put("message", "assistant_id 가 필요합니다.");
		    	rtnInfo.put("result", "false");
		    	return rtnInfo;
		    }
		    if (aiInfo.containsKey("api_key")) {
		    	apiKey = aiInfo.get("api_key").toString();
		    } else {
		    	rtnInfo.put("message", "api_key 가 필요합니다.");
		    	rtnInfo.put("result", "false");
		    	return rtnInfo;
		    }
		    
			this.chatRoom = new com.omnibuscode.ai.openai.ChatThread(assistantId, apiKey);
			rtnInfo.put("result", "true");
			return rtnInfo;
		}
		
		return null;
	}
	
	/**
	 * 채팅방에 메세지를 전달하고 응답 메세지를 반환한다.
	 * @param userMsg
	 * @return
	 */
	public String sendMessage(String userMsg) {
		if (userMsg == null || userMsg.trim().length() < 1) {
			return "사용자 메세지가 없습니다.";
		}
		
		if (this.chatRoom == null) {
			return "ai가 선택되어야 합니다.";
		}
		
		String resMsg = null;
		try {
			resMsg = this.chatRoom.sendMessage(userMsg);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		try {
			this.chatRoom.closeChat();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		return resMsg;
	}
}

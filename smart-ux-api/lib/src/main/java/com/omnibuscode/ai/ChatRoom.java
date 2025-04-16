package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 대화방
 */
public abstract class ChatRoom {
	
	/**
	 * 채팅방 아이디
	 * @return
	 */
	public abstract String getId();
	
	/**
	 * 대화방 나가기 (openai 에서는 thread 삭제)
	 * @throws IOException
	 * @throws ParseException
	 */
	public abstract boolean closeChat() throws IOException, ParseException;

	/**
	 * 기본 채팅
	 * @return
	 */
	public Chatting createChatting() {
		return this.createChatting(0);
	}
	/**
	 * 0:ChatBuddy, 1:ChatAction
	 * @param chatmode
	 * @return
	 */
	public abstract Chatting createChatting(int chatmode);
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
//	/**
//	 * 사용자 메세지를 전달후 응답메세지를 반환한다.
//	 * @param userMsg
//	 * @return {"message":String, ChatManager.USER_FUNCTIONS_RESULT:JSONObject}
//	 */
//	public abstract JSONObject sendMessage(String userMsg) throws IOException, ParseException;
	
//	/**
//	 * 사용자 메세지를 전달후 Action Queue를 반환한다.
//	 * @param userMsg
//	 * @return
//	 * @throws IOException
//	 * @throws ParseException
//	 */
//	public JSONObject reqActionQueue(String userMsg) throws IOException, ParseException {
//		
//		String reqMsg = "\""+userMsg+"\" 라는 명령을 수행하기 위한 action queue 의 json 을 만드는데 id 에 해당하는 selector 와 xpath 도 포함시켜줘";
//		JSONObject rtnJson = this.sendMessage(reqMsg);
//		
//		return rtnJson;
//	}

}
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
	 * 기본 채팅 (ChatBuddy)
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

}
package com.smartuxapi.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

/**
 * 대화방
 */
public interface ChatRoom {

	/**
	 * Chatting instance 를 반환
	 * @return
	 */
	public abstract Chatting getChatting();
	
	/**
	 * 대화방 나가기 (openai 에서는 thread 삭제)
	 * @return
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public abstract boolean closeChat() throws IOException, ParseException;
	
	/**
	 * UX Info 처리를 위한 Prompt 추가
	 * @param chat
	 * @return
	 */
	public abstract Chatting decorateUXInfo(Chatting chat);
}

package com.smartuxapi.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

/**
 * 대화방 (하나의 ChatRoom 에서는 하나의 Chatting instance 만 사용 가능)
 */
public interface ChatRoom {
	
	public String getId();

	/**
	 * Chatting instance 를 반환
	 * 
	 * @return
	 */
	public Chatting getChatting();
	
	/**
	 * 대화방 나가기 (openai 에서는 thread 삭제)
	 * 
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean closeChat() throws IOException, ParseException;
	
	/**
	 * Decorate Prompt for Current UX Information
	 * API 내부용으로 사용되는 함수로써 현재 화면 정보를 갱신하는 프롬프트를 추가한다.
	 * 
	 * @param chat
	 * @return
	 */
	public Chatting decorateUXInfo(Chatting chat);
	
	/**
	 * 현재 화면 정보를 AI에게 전달한다.
	 * 
	 * @param viewInfoJson
	 */
	public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException;
}

package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

public interface ChatRoom {

	/**
	 * 사용자 메세지를 전달후 응답메세지를 반환한다.
	 * @param userMsg
	 * @return
	 */
	public String sendMessage(String userMsg) throws IOException, ParseException;
	
	/**
	 * 대화방 나가기
	 * @throws IOException
	 * @throws ParseException
	 */
	public void closeChat() throws IOException, ParseException;
	
}

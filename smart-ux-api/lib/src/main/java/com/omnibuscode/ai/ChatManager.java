package com.omnibuscode.ai;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

public class ChatManager {

	private Logger log = LogManager.getLogger(ChatManager.class);
	
	/**
	 * 채팅방에 메세지를 전달하고 응답 메세지를 반환한다.
	 * @param aiName : openai or gemini
	 * @param userMsg
	 * @return
	 */
	public String sendMessage(String aiName, String userMsg) {
		if (userMsg == null || userMsg.trim().length() < 1) {
			log.info("사용자 메세지가 없습니다.");
			return null;
		}
		
		ChatRoom room = null;
		if ("openai".equals(userMsg)) {
			room = new com.omnibuscode.ai.openai.ChatThread();
		}
		
		String resMsg = null;
		try {
			resMsg = room.sendMessage(userMsg);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		try {
			room.closeChat();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		
		return resMsg;
	}
}

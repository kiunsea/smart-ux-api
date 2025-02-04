package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 대화방
 */
public interface ChatRoom {
	
	/**
	 * 채팅방 아이디
	 * @return
	 */
	public String getId();

	/**
	 * 사용자 메세지를 전달후 응답메세지를 반환한다.
	 * @param userMsg
	 * @return {"message":String, "userFunctionsResult":JSONObject}
	 */
	public JSONObject sendMessage(String userMsg) throws IOException, ParseException;
	
	/**
	 * 대화방 나가기 (openai 에서는 thread 삭제)
	 * @throws IOException
	 * @throws ParseException
	 */
	public boolean closeChat() throws IOException, ParseException;
	
}

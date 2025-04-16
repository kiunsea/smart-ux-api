package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public interface Chatting {
	
	/**
	 * 입력한 사용자 메세지를 thread 에 추가하고 run 한다
	 * @param userMsg
	 * @return assistant message - {"message":String, "userFunctionsResult":JSONObject}
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public JSONObject sendMessage(String userMsg) throws IOException, ParseException;
	
}

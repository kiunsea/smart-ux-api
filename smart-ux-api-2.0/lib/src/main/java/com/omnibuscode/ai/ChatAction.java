package com.omnibuscode.ai;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * 메세지 대화와 Action Queue 요청
 */
public abstract class ChatAction implements Chatting {

	/**
	 * 일반 메세지 추출
	 * @param paragraph
	 * @return
	 */
	abstract protected JSONObject findJsonBlock(String paragraph);
	
	/**
	 * ActionQueue 추출
	 * @param paragraph
	 * @return
	 * @throws ParseException
	 */
	abstract protected JSONArray extractActionQueue(String paragraph) throws ParseException;
}

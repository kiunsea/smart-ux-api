package com.omnibuscode.ai;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 사용자 정의 함수 인터페이스
 */
public interface UserFunction {

	/**
	 * function 을 실행
	 * @param funcName
	 * @param argsJson
	 * @return API에서는 실행결과를 ChatRoom.sendMessage()의 반환값(JSONObject)에 "ChatManager.USER_FUNCTIONS_RESULT"로 저장한다.
	 */
	public JSONObject execFunction(String funcName, JsonNode argsJson);
	
	/**
	 * function 의 json 내용을 반환 (이 내용을 assistants/tools/functions 에 등록하게 된다)
	 * @return null 인 경우에는 등록을 스킵한다.
	 */
	public abstract JSONObject getFunctionJson();
}

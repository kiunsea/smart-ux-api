package com.omnibuscode.ai;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 사용자 정의 함수 인터페이스<br/>
 * function 을 assistants/tools/functions 에 등록하고 ai가 사용자 질의 해석시 필요에 따라 function 을 호출하면 su-api 에서 해당 함수를 실행하게 한다.
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
	 * assistant function 의 json 내용을 반환한다.<br/>
	 * 이 함수를 호출하여 실행 결과로 반환된 내용을 assistants/tools/functions 에 등록하게 된다.
	 * @return null 인 경우에는 등록을 스킵한다.
	 */
	public abstract JSONObject getFunctionJson();
}

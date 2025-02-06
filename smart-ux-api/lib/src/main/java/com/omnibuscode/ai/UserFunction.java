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
	 * @return 실행결과를 ChatRoom.sendMessage()의 반환값(JSONObject)에 "userFunctionsResult"에 저장
	 */
	public JSONObject execFunction(String funcName, JsonNode argsJson);
}

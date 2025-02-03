package com.omnibuscode.ai;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 사용자 정의 함수 인터페이스
 */
public interface UserFunction {

	/**
	 * function 을 실행
	 * @param funcName
	 * @param argsJson
	 * @return
	 */
	public boolean execFunction(JsonNode argsJson);
}

package com.omnibuscode.ai;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class DummyUserFunction implements UserFunction {

	@Override
	public JSONObject execFunction(String funcName, JsonNode argsJson) {
		System.out.println("* [DummyUserFunction] execute user function - [" + funcName + "] args -> " + argsJson);
		return null;
	}

}

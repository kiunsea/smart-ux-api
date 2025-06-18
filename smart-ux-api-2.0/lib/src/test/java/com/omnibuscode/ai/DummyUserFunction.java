package com.omnibuscode.ai;

import java.io.UnsupportedEncodingException;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.legacy.ProcessFunction;

public class DummyUserFunction implements ProcessFunction {

	@Override
	public JSONObject execFunction(String funcName, JsonNode argsJson) {
		System.out.println("* [DummyUserFunction] execute user function - [" + funcName + "] args -> " + argsJson);
		String link = "https://www.google.com";
		try {
			JSONObject resJson = new JSONObject();
			resJson.put("user_link", java.net.URLEncoder.encode(link, "UTF-8"));
			return resJson;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JSONObject getFunctionJson() {
		return null;
	}

}

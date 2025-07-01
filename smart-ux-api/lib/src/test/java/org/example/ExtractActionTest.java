package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.smartuxapi.util.JSONUtil;

public class ExtractActionTest {

	@Test
	public void test() throws ParseException {
		String sample = "{\"assist_msg\":\"```json\\n{\\n  \\\"actionQueue\\\": [\\n    {\\n      \\\"id\\\": \\\"ice_아메리카노\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#ice_아메리카노\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='ice_아메리카노']\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"녹차프라페\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#녹차프라페\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='녹차프라페']\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"total_price\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#total_price\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='total_price']\\\"\\n    }\\n  ]\\n}\\n```\"}";
		System.out.println(extractActionQueue(sample).toJSONString());
	}

	private JSONArray extractActionQueue(String paragraph) throws ParseException {
		
		Object msgObj = JSONUtil.parseJSONObject(paragraph).get("assist_msg");
		
		if (msgObj == null) {
			return null;
		}
		String input = msgObj.toString();
		
		// 문자열에서 JSON 블록만 추출
		int jsonStart = input.indexOf("```json");
		int jsonEnd = input.lastIndexOf("```");

		if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
			throw new IllegalArgumentException("JSON block not found in input");
		}

		// JSON 문자열만 추출
		String jsonString = input.substring(jsonStart + 7, jsonEnd).trim();

		// JSONObject 생성 및 actionQueue 추출
		JSONObject jsonObject = JSONUtil.parseJSONObject(jsonString);
		Object obj = jsonObject.get("actionQueue");
		return obj != null ? (JSONArray) obj : null;
	}
	
}

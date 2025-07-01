package org.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.smartuxapi.util.ActionQueueUtil;
import com.smartuxapi.util.JSONUtil;

public class ExtractActionTest {

	public static void main(String args[]) throws ParseException {
//		String sample1 = "{\"assist_msg\":\"```json\\n{\\n  \\\"actionQueue\\\": [\\n    {\\n      \\\"id\\\": \\\"ice_아메리카노\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#ice_아메리카노\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='ice_아메리카노']\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"녹차프라페\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#녹차프라페\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='녹차프라페']\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"total_price\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#total_price\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='total_price']\\\"\\n    }\\n  ]\\n}\\n```\"}";
//		System.out.println(extractActionQueue(sample1).toJSONString());
//		String sample2 = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";
//		System.out.println(ActionQueueUtil.extractActionQueue(sample2).toPrettyString());
		String sample3 = "사용자가 요청한 주문을 처리하기 위해 `actionQueue` 배열에 포함할 항목들은 요청된 음료의 ID를 기반으로 작성하면 됩니다. 사용자가 주문한 음료 목록은 다음과 같습니다: 아메리카노, 커피프라페, ice 사과유자차, hot 레몬차.\n\n제공된 정보에서 각각의 음료에 해당하는 요소들을 찾아 `actionQueue` 내용을 작성하겠습니다:\n\n```json\n{\n  \"actionQueue\": [\n    {\n      \"id\": \"ice_아메리카노\",\n      \"type\": \"click\",\n      \"label\": \"ice_아메리카노\",\n      \"selector\": \"#ice_아메리카노\",\n      \"xpath\": \"//*[@id='ice_아메리카노']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    },\n    {\n      \"id\": \"커피프라페\",\n      \"type\": \"click\",\n      \"label\": \"커피프라페\",\n      \"selector\": \"#커피프라페\",\n      \"xpath\": \"//*[@id='커피프라페']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    },\n    {\n      \"id\": \"ice_사과유자차\",\n      \"type\": \"click\",\n      \"label\": \"ice_사과유자차\",\n      \"selector\": \"#ice_사과유자차\",\n      \"xpath\": \"//*[@id='ice_사과유자차']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    },\n    {\n      \"id\": \"hot_레몬차\",\n      \"type\": \"click\",\n      \"label\": \"hot_레몬차\",\n      \"selector\": \"#hot_레몬차\",\n      \"xpath\": \"//*[@id='hot_레몬차']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    }\n  ]\n}\n```\n\n각 항목의 `id`, `selector`, `xpath`는 제공된 사용자 액션 가능 요소 정보에서 발췌한 것이며, 이 정보를 바탕으로 사용자의 명령을 수행할 수 있습니다.";
		System.out.println(ActionQueueUtil.extractActionQueue(sample3).toPrettyString());
	}

	private static JSONArray extractActionQueue(String paragraph) throws ParseException {
		
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

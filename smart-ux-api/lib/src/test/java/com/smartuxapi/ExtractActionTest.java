package com.smartuxapi;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.smartuxapi.util.ActionQueueUtil;
import com.smartuxapi.util.JSONUtil;

public class ExtractActionTest {

	public static void main(String args[]) throws ParseException {

	    //String sample1 = "{\"assist_msg\":\"```json\\n{\\n  \\\"actionQueue\\\": [\\n    {\\n      \\\"id\\\": \\\"ice_아메리카노\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#ice_아메리카노\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='ice_아메리카노']\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"녹차프라페\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#녹차프라페\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='녹차프라페']\\\"\\n    },\\n    {\\n      \\\"id\\\": \\\"total_price\\\",\\n      \\\"type\\\": \\\"click\\\",\\n      \\\"selector\\\": \\\"#total_price\\\",\\n      \\\"xpath\\\": \\\"\\/\\/*[@id='total_price']\\\"\\n    }\\n  ]\\n}\\n```\"}";
	    //System.out.println(extractActionQueue(sample1).toJSONString());

	    //String sample2 = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";
	    //System.out.println(ActionQueueUtil.extractActionQueue(sample2).toPrettyString());

	    //String sample3 = "사용자가 요청한 주문을 처리하기 위해 `actionQueue` 배열에 포함할 항목들은 요청된 음료의 ID를 기반으로 작성하면 됩니다. 사용자가 주문한 음료 목록은 다음과 같습니다: 아메리카노, 커피프라페, ice 사과유자차, hot 레몬차.\n\n제공된 정보에서 각각의 음료에 해당하는 요소들을 찾아 `actionQueue` 내용을 작성하겠습니다:\n\n```json\n{\n  \"actionQueue\": [\n    {\n      \"id\": \"ice_아메리카노\",\n      \"type\": \"click\",\n      \"label\": \"ice_아메리카노\",\n      \"selector\": \"#ice_아메리카노\",\n      \"xpath\": \"//*[@id='ice_아메리카노']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    },\n    {\n      \"id\": \"커피프라페\",\n      \"type\": \"click\",\n      \"label\": \"커피프라페\",\n      \"selector\": \"#커피프라페\",\n      \"xpath\": \"//*[@id='커피프라페']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    },\n    {\n      \"id\": \"ice_사과유자차\",\n      \"type\": \"click\",\n      \"label\": \"ice_사과유자차\",\n      \"selector\": \"#ice_사과유자차\",\n      \"xpath\": \"//*[@id='ice_사과유자차']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    },\n    {\n      \"id\": \"hot_레몬차\",\n      \"type\": \"click\",\n      \"label\": \"hot_레몬차\",\n      \"selector\": \"#hot_레몬차\",\n      \"xpath\": \"//*[@id='hot_레몬차']\",\n      \"properties\": {\n        \"enabled\": true,\n        \"visible\": true\n      }\n    }\n  ]\n}\n```\n\n각 항목의 `id`, `selector`, `xpath`는 제공된 사용자 액션 가능 요소 정보에서 발췌한 것이며, 이 정보를 바탕으로 사용자의 명령을 수행할 수 있습니다.";
	    //System.out.println(ActionQueueUtil.extractActionQueue(sample3).toPrettyString());
		
//		String sample4 = "사용자의 명령에 따라 지정된 제품들을 주문하고 결제하기 위해 필요한 `actionQueue`를 작성하려면, 사용자가 클릭해야 하는 요소들을 순서대로 나열해야 합니다. 이 경우, 사용자 요청은 \"리얼초코프라페\", \"커피프라페\", \"민트프라페\"를 주문하고 결제하는 것입니다.\n\n아래는 요청에 대한 `actionQueue`의 JSON 구조입니다:\n\n```json\n[\n    {\n        \"id\": \"리얼초코프라페\",\n        \"type\": \"click\",\n        \"label\": \"리얼초코프라페\",\n        \"selector\": \"#리얼초코프라페\",\n        \"xpath\": \"//*[@id='리얼초코프라페']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    },\n    {\n        \"id\": \"민트프라페\",\n        \"type\": \"click\",\n        \"label\": \"민트프라페\",\n        \"selector\": \"#민트프라페\",\n        \"xpath\": \"//*[@id='민트프라페']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    }\n]\n```\n\n이 `actionQueue`는 사용자가 '리얼초코프라페', '커피프라페', '민트프라페'를 클릭 후 '결제하기' 버튼을 클릭하여 주문을 완료할 수 있도록 만들어졌습니다. 이 순서로 순차적으로 클릭 이벤트가 발생합니다.";
//        System.out.println(ActionQueueUtil.extractActionQueue(sample4).toPrettyString());
        
        String sample5 = "사용자의 명령에 따라 \"시원한 아메리카노\"와 \"커피프라페\"를 주문하고 결제하기 위해 필요한 `actionQueue`를 작성해 보겠습니다. 사용자가 요청한 \"시원한 아메리카노\"는 \"ice_아메리카노\"로 해석할 수 있으며, 이전에 제공된 화면 정보에는 \"커피프라페\"에 대한 정보가 없지만, 우리는 사용자의 요청을 완수하기 위해 이 부분을 무시하고 제공된 정보에 기반하여 \"아메리카노\"만 주문하고 결제하는 과정을 작성합니다.\n\n여기서는 제공된 DOM 구조를 기반으로 가능한 아이템들을 주문하고 결제하는 것을 나타냅니다. 실제 상황에서는 모든 항목이 있는지 확인해야 하겠지만, 여기서는 제공된 정보 내에서 요청을 처리합니다:\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    }\n]\n```\n\n위 `actionQueue`는 사용자가 'ice_아메리카노'를 클릭 후 '결제하기' 버튼을 클릭하는 과정을 자동으로 수행하도록 디자인되었습니다. 만약 \"커피프라페\"가 현재 DOM 구조 상황에 포함되어 있지 않다면, 구체적 명칭 및 구조를 얻은 후 추가로 업데이트가 필요할 수 있습니다.";
        System.out.println(ActionQueueUtil.extractActionQueue(sample5).toPrettyString());
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

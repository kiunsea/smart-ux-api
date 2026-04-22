package com.smartuxapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.util.ActionQueueUtil;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Action Queue 추출 테스트")
public class ExtractActionTest {

	@Test
	@DisplayName("Action Queue 추출 테스트 - JSON 배열 형식")
	public void testExtractActionQueueFromArray() throws ParseException {
		String sample = "사용자의 명령에 따라 \"시원한 아메리카노\"와 \"커피프라페\"를 주문하고 결제하기 위해 필요한 `actionQueue`를 작성해 보겠습니다. 사용자가 요청한 \"시원한 아메리카노\"는 \"ice_아메리카노\"로 해석할 수 있으며, 이전에 제공된 화면 정보에는 \"커피프라페\"에 대한 정보가 없지만, 우리는 사용자의 요청을 완수하기 위해 이 부분을 무시하고 제공된 정보에 기반하여 \"아메리카노\"만 주문하고 결제하는 과정을 작성합니다.\n\n여기서는 제공된 DOM 구조를 기반으로 가능한 아이템들을 주문하고 결제하는 것을 나타냅니다. 실제 상황에서는 모든 항목이 있는지 확인해야 하겠지만, 여기서는 제공된 정보 내에서 요청을 처리합니다:\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\"enabled\": true, \"visible\": true}\n    }\n]\n```\n\n위 `actionQueue`는 사용자가 'ice_아메리카노'를 클릭 후 '결제하기' 버튼을 클릭하는 과정을 자동으로 수행하도록 디자인되었습니다. 만약 \"커피프라페\"가 현재 DOM 구조 상황에 포함되어 있지 않다면, 구체적 명칭 및 구조를 얻은 후 추가로 업데이트가 필요할 수 있습니다.";
		
		JsonNode result = ActionQueueUtil.extractActionQueue(sample);
		
		assertNotNull(result, "Action Queue가 추출되어야 합니다");
		assertTrue(result.isArray() || (result.has("action_queue") && result.get("action_queue").isArray()), 
			"결과는 배열이거나 action_queue 필드를 가진 객체여야 합니다");
	}

}

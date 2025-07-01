package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public class JsonExtractorTest {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static void main(String[] args) {
		chatGPT1();
		chatGPT2();
		gemini1();
		gemini2();
		gemini3();
	}

	public static void chatGPT2() {
		String inputString = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";

		String extractedJson = extractJsonBlock(inputString);

		if (extractedJson != null) {
			System.out.println("추출된 JSON:");
			System.out.println(extractedJson);

			// 추출된 JSON 문자열을 JsonNode로 파싱하여 유효성 검사 및 사용 예시
			try {
				JsonNode jsonNode = objectMapper.readTree(extractedJson.trim());
				System.out.println("\n--- JsonNode로 파싱된 내용 (Pretty Print) ---");
				System.out.println(jsonNode.toPrettyString());
				System.out.println("\nJSON 파싱 성공: 추출된 내용이 유효한 JSON입니다.");
			} catch (IOException e) {
				System.err.println("\n오류: 추출된 내용이 유효한 JSON이 아닙니다: " + e.getMessage());
			}
		} else {
			System.out.println("JSON을 찾을 수 없습니다.");
		}
	}

	public static void chatGPT1() {
		String inputString = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";

		String extractedJson = extractJson(inputString);

		if (extractedJson != null) {
			System.out.println("추출된 JSON:");
			System.out.println(extractedJson);

			// 추출된 JSON 문자열을 JsonNode로 파싱하여 유효성 검사 및 사용 예시
			try {
				JsonNode jsonNode = objectMapper.readTree(extractedJson.trim());
				System.out.println("\n--- JsonNode로 파싱된 내용 (Pretty Print) ---");
				System.out.println(jsonNode.toPrettyString());
				System.out.println("\nJSON 파싱 성공: 추출된 내용이 유효한 JSON입니다.");
			} catch (IOException e) {
				System.err.println("\n오류: 추출된 내용이 유효한 JSON이 아닙니다: " + e.getMessage());
			}
		} else {
			System.out.println("JSON을 찾을 수 없습니다.");
		}
	}

	/**
	 * 부분 문자열 시도 및 파싱 (Try-and-Error Parsing) 방식
	 */
	public static void gemini3() {
		String inputString = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";

		System.out.println("--- 파싱 시도 방식으로 JSON 추출 ---");
		String extractedJson = extractAndParseJson(inputString);

		if (extractedJson != null) {
			System.out.println("추출된 JSON:\n" + extractedJson);
			try {
				JsonNode jsonNode = objectMapper.readTree(extractedJson);
				System.out.println("\n--- 파싱된 JSON (Pretty Print) ---");
				System.out.println(jsonNode.toPrettyString());
			} catch (Exception e) {
				System.err.println("\n오류: 추출된 내용이 유효한 JSON이 아닙니다: " + e.getMessage());
			}
		} else {
			System.out.println("문자열에서 유효한 JSON을 찾을 수 없습니다.");
		}
	}

	/**
	 * 문자열에서 첫 번째 유효한 JSON 객체 또는 배열을 파싱 시도 방식으로 추출합니다.
	 *
	 * @param text 원본 문자열
	 * @return 추출된 유효한 JSON 문자열 또는 찾지 못했을 경우 null
	 */
	public static String extractAndParseJson(String text) {
		// 가능한 JSON 시작 위치를 찾습니다.
		for (int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if (currentChar == '{' || currentChar == '[') {
				// 시작 괄호부터 끝까지의 부분 문자열을 가져옵니다.
				// 이 부분을 최적화하여 괄호 카운팅을 먼저 적용할 수도 있습니다.
				String potentialJson = text.substring(i);

				// 유효한 JSON이 될 때까지 부분 문자열을 짧게 자르면서 시도합니다.
				// 여기서 괄호 카운팅 로직을 삽입하여 endIndex를 더 효율적으로 찾을 수 있습니다.
				for (int j = potentialJson.length(); j >= 0; j--) {
					String subString = potentialJson.substring(0, j);
					try {
						// \u00A0 문자를 일반 공백으로 치환
						String normalizedJson = subString.replace('\u00A0', ' ');

						// ObjectMapper로 파싱을 시도합니다.
						// 파싱에 성공하면 유효한 JSON으로 간주하고 반환합니다.
						objectMapper.readTree(normalizedJson);
						return normalizedJson; // 유효한 JSON을 찾았으므로 반환
					} catch (Exception e) {
						// 파싱 오류 발생 시 (즉, 유효한 JSON이 아님) 다음 짧은 부분 문자열로 시도
					}
				}
			}
		}
		return null; // 유효한 JSON을 찾지 못함
	}

	/**
	 * 괄호 카운팅 (Bracket Counting) 방식
	 */
	public static void gemini2() {
		String inputString = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";

		System.out.println("--- 괄호 카운팅 방식으로 JSON 추출 ---");
		String extractedJson = extractJsonByBracketCounting(inputString);

		if (extractedJson != null) {
			System.out.println("추출된 JSON:\n" + extractedJson);
			try {
				JsonNode jsonNode = objectMapper.readTree(extractedJson);
				System.out.println("\n--- 파싱된 JSON (Pretty Print) ---");
				System.out.println(jsonNode.toPrettyString());
			} catch (Exception e) {
				System.err.println("\n오류: 추출된 내용이 유효한 JSON이 아닙니다: " + e.getMessage());
			}
		} else {
			System.out.println("문자열에서 JSON을 찾을 수 없습니다.");
		}
	}

	/**
	 * 문자열에서 첫 번째 완전한 JSON 객체 또는 배열을 괄호 카운팅 방식으로 추출합니다.
	 *
	 * @param text 원본 문자열
	 * @return 추출된 JSON 문자열 또는 찾지 못했을 경우 null
	 */
	public static String extractJsonByBracketCounting(String text) {
		int startIndex = -1;
		char startChar = ' ';
		char endChar = ' ';

		// 첫 번째 '{' 또는 '['의 위치를 찾습니다.
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '{') {
				startIndex = i;
				startChar = '{';
				endChar = '}';
				break;
			} else if (c == '[') {
				startIndex = i;
				startChar = '[';
				endChar = ']';
				break;
			}
		}

		if (startIndex == -1) {
			return null; // JSON 시작 괄호를 찾지 못함
		}

		int bracketCount = 0;
		int endIndex = -1;
		boolean inQuote = false; // 문자열 내부인지 추적
		char escapeChar = '\\'; // 이스케이프 문자

		for (int i = startIndex; i < text.length(); i++) {
			char c = text.charAt(i);

			// 문자열 내부에서는 괄호를 무시
			if (c == '"' && (i == 0 || text.charAt(i - 1) != escapeChar)) {
				inQuote = !inQuote;
			}

			if (!inQuote) {
				if (c == startChar) {
					bracketCount++;
				} else if (c == endChar) {
					bracketCount--;
				}
			}

			// 괄호의 균형이 맞춰졌으면 JSON 블록의 끝입니다.
			if (bracketCount == 0 && startIndex != -1) {
				endIndex = i;
				break;
			}
		}

		if (endIndex != -1) {
			// \u00A0 문자를 일반 공백으로 치환
			String normalizedJson = text.substring(startIndex, endIndex + 1).replace('\u00A0', ' ');
			return normalizedJson;
		} else {
			return null; // 완전한 JSON 블록을 찾지 못함 (괄호 균형이 맞지 않음)
		}
	}

	public static void gemini1() {
		String inputString = "사용자가 요청한 대로 `아메리카노`, `커피프라페`, `ice 사과유자차`, `hot 레몬차`를 주문하기 위한 `actionQueue`를 구성하려면, 요청과 현재 화면에 있는 정보를 기반으로 클릭 액션을 정해야 합니다. 아래는 이 요청을 수행할 `actionQueue`의 예입니다.\n\n```json\n[\n    {\n        \"id\": \"ice_아메리카노\",\n        \"type\": \"click\",\n        \"label\": \"ice_아메리카노\",\n        \"selector\": \"#ice_아메리카노\",\n        \"xpath\": \"//*[@id='ice_아메리카노']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"커피프라페\",\n        \"type\": \"click\",\n        \"label\": \"커피프라페\",\n        \"selector\": \"#커피프라페\",\n        \"xpath\": \"//*[@id='커피프라페']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"ice_사과유자차\",\n        \"type\": \"click\",\n        \"label\": \"ice_사과유자차\",\n        \"selector\": \"#ice_사과유자차\",\n        \"xpath\": \"//*[@id='ice_사과유자차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"hot_레몬차\",\n        \"type\": \"click\",\n        \"label\": \"hot_레몬차\",\n        \"selector\": \"#hot_레몬차\",\n        \"xpath\": \"//*[@id='hot_레몬차']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    },\n    {\n        \"id\": \"total_price\",\n        \"type\": \"click\",\n        \"label\": \"0원\\n결제하기\",\n        \"selector\": \"#total_price\",\n        \"xpath\": \"//*[@id='total_price']\",\n        \"properties\": {\n            \"enabled\": true,\n            \"visible\": true\n        }\n    }\n]\n```\n\n이 `actionQueue` 구조는 각 항목에 대해 `id`, `type`, `label`, `selector`, `xpath`, 및 `properties`를 지정하여 클릭 가능한 요소들이 정의됩니다. 사용자가 요청한 아이템을 포함하며 최종적으로 총액을 결제하기 위한 액션이 포함되어 있습니다.";

		String extractedJson = extractJsonContent(inputString);

		if (extractedJson != null) {
			System.out.println("--- 추출된 JSON 내용 ---");
			System.out.println(extractedJson);

			// 추출된 JSON 문자열을 JsonNode로 파싱하여 유효성 검사 및 사용 예시
			try {
				JsonNode jsonNode = objectMapper.readTree(extractedJson.trim());
				System.out.println("\n--- JsonNode로 파싱된 내용 (Pretty Print) ---");
				System.out.println(jsonNode.toPrettyString());
				System.out.println("\nJSON 파싱 성공: 추출된 내용이 유효한 JSON입니다.");
			} catch (IOException e) {
				System.err.println("\n오류: 추출된 내용이 유효한 JSON이 아닙니다: " + e.getMessage());
			}
		} else {
			System.out.println("JSON 내용을 찾을 수 없습니다.");
		}
	}

	/**
	 * 주어진 문자열에서 코드 블록(```json\n...\n```) 내의 완전한 JSON 내용을 추출합니다.
	 *
	 * @param text JSON을 포함할 수 있는 입력 문자열
	 * @return 추출된 JSON 문자열. JSON 블록을 찾지 못하면 null.
	 */
	public static String extractJsonContent(String text) {
		String startMarker = "```json\n";
		String endMarker = "\n```\n";

		int startIndex = text.indexOf(startMarker);
		if (startIndex == -1) {
			return null; // 시작 마커를 찾지 못함
		}

		// JSON 시작 위치 조정 (마커 길이만큼 더함)
		startIndex += startMarker.length();

		int endIndex = text.indexOf(endMarker, startIndex);
		if (endIndex == -1) {
			// 끝 마커를 찾지 못했지만 시작 마커는 찾은 경우
			// 이 경우 JSON이 문자열의 끝까지 이어지거나, 끝 마커가 생략된 것으로 간주할 수 있습니다.
			// 여기서는 단순화를 위해 끝 마커가 없으면 JSON을 추출하지 않습니다.
			// 실제 사용 시에는 이 부분을 어떻게 처리할지 정책을 정해야 합니다.
			System.err.println("경고: JSON 시작 마커는 찾았으나, 끝 마커를 찾을 수 없습니다.");
			return null;
		}

		// substring으로 JSON 내용 추출
		String jsonContent = text.substring(startIndex, endIndex);

		// \u00A0 문자를 일반 공백으로 치환
		String normalizedJson = jsonContent.replace('\u00A0', ' ');

		// JSON 문자열의 앞뒤 공백(특히 유니코드 공백) 제거
		return normalizedJson.trim();
	}

	public static String extractJson(String input) {
		// 시작과 끝 패턴 설정
		String startTag = "```json";
		String endTag = "```";

		// 시작 위치 찾기
		int startIndex = input.indexOf(startTag);
		if (startIndex == -1) {
			return null; // JSON 시작 태그 없음
		}
		startIndex += startTag.length();

		// 종료 위치 찾기
		int endIndex = input.indexOf(endTag, startIndex);
		if (endIndex == -1) {
			return null; // JSON 종료 태그 없음
		}

		// JSON 부분 추출 및 앞뒤 공백 제거
		String json = input.substring(startIndex, endIndex).trim();

		// \u00A0 문자를 일반 공백으로 치환
		String normalizedJson = json.replace('\u00A0', ' ');

		return normalizedJson;
	}

	/**
	 * 중괄호({})나 대괄호([])를 이용한 균형 검사
	 * 
	 * @param text
	 * @return
	 */
	public static String extractJsonBlock(String text) {
		int startIndex = -1;
		int braceCount = 0;
		char startChar = 0;
		char endChar = 0;

		// 문자열을 한 글자씩 검사
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);

			// JSON 배열 시작
			if (c == '[' || c == '{') {
				if (startIndex == -1) {
					startIndex = i;
					startChar = c;
					endChar = (c == '[') ? ']' : '}';
					braceCount = 1;
				} else if (c == startChar) {
					braceCount++;
				}
			}
			// JSON 배열 종료
			else if (c == endChar) {
				braceCount--;
				if (braceCount == 0 && startIndex != -1) {

					// \u00A0 문자를 일반 공백으로 치환
					String normalizedJson = text.substring(startIndex, i + 1).replace('\u00A0', ' ');
					return normalizedJson;
				}
			}
		}

		// 못 찾으면 null
		return null;
	}
}
package com.smartuxapi.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final String CONFIG_FILE_NAME = "config.json"; // 클래스패스에서 찾을 설정 파일 이름
	
	/**
	 * 기본 파일명으로 load (config.json)
	 * 
	 * @return JsonNode
	 */
	public static JsonNode loadConfigFromClasspath() {
		return loadConfigFromClasspath(null);
	}

	/**
	 * 클래스패스 루트에서 JSON 설정 파일을 로드하고 JsonNode로 반환합니다.
	 *
	 * @return 로드된 설정 파일의 JsonNode 객체, 파일을 찾지 못하거나 파싱 오류 시 null
	 */
	public static JsonNode loadConfigFromClasspath(String confFileName) {
		
		if (confFileName == null) {
			confFileName = CONFIG_FILE_NAME;
		}
		
		// ClassLoader를 사용하여 클래스패스에서 리소스를 InputStream으로 얻습니다.
		// 현재 클래스의 ClassLoader를 사용하면 해당 클래스 위치 기준으로 리소스를 찾습니다.
		// "/"로 시작하면 클래스패스의 루트를 의미합니다.
		try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(confFileName)) {

			if (inputStream == null) {
				System.err.println("경고: 설정 파일 '" + confFileName + "'을(를) 클래스패스에서 찾을 수 없습니다.");
				System.err.println("src/main/resources 또는 컴파일된 클래스패스 루트에 있는지 확인하세요.");
				return null;
			}

			// InputStream을 UTF-8로 인코딩된 문자열로 변환합니다.
			// Scanner를 사용하는 것이 InputStream을 전체 문자열로 읽는 간단한 방법입니다.
			String jsonContent = new Scanner(inputStream, StandardCharsets.UTF_8.name()).useDelimiter("\\A").next();

			System.out.println("설정 파일 '" + confFileName + "' 내용을 클래스패스에서 로드했습니다:\n" + jsonContent);

			// 읽어온 JSON 문자열을 JsonNode로 파싱합니다.
			try {
				return objectMapper.readTree(jsonContent);
			} catch (IOException e) {
				System.err.println("오류: 설정 파일 '" + confFileName + "' JSON 파싱 중 오류 발생: " + e.getMessage());
				return null;
			}

		} catch (IOException e) {
			System.err.println("오류: 설정 파일 '" + confFileName + "' 읽기 중 오류 발생: " + e.getMessage());
			return null;
		}
	}

	
	/**
	 * Class Test
	 * @param args
	 */
	/**
	public static void main(String[] args) {
		JsonNode config = loadConfigFromClasspath();

		if (config != null) {
			System.out.println("\n--- 파싱된 설정 내용 ---");
			System.out.println(config.toPrettyString()); // JsonNode를 보기 좋게 출력

			// 특정 설정 값 접근 예시
			if (config.has("setting1")) {
				System.out.println("\nsetting1: " + config.get("setting1").asText());
			}
			if (config.hasNonNull("featureEnabled")) {
				System.out.println("featureEnabled: " + config.get("featureEnabled").asBoolean());
			}
		} else {
			System.out.println("\n설정 파일을 로드하지 못했습니다. 애플리케이션을 기본 설정으로 실행합니다.");
			// 여기서 기본 설정 로직을 구현하거나, 오류 처리를 진행할 수 있습니다.
		}
		
		if (config.get("prompt").get("cur_view_info").isArray()) {
			System.out.println("\nprompt.cur_view_info: " + config.get("prompt").get("cur_view_info"));

			Iterator<JsonNode> elements = config.get("prompt").get("cur_view_info").elements();
			while (elements.hasNext()) {
				JsonNode elementNode = elements.next();
				// 4. 각 요소가 문자열 노드인지 확인하고 asText()로 문자열 값을 추출합니다.
				if (elementNode.isTextual()) { // 문자열 타입인지 확인
					System.out.println("cur_view_info >> " + elementNode.asText());
				} else if (elementNode.isNull()) {
					// 배열 내에 null 값이 있는 경우 (예: ["a", null, "b"])
					// 또는 포함하지 않도록 선택 가능
				} else {
					// 배열 내에 문자열이 아닌 다른 타입의 값(숫자, 객체 등)이 있는 경우
					// 이 경우 경고를 출력하거나 해당 값을 건너뛸 수 있습니다.
					System.err.println("경고: 배열 내에 문자열이 아닌 요소가 발견되었습니다: " + elementNode.getNodeType() + " for value: "
							+ elementNode.toString());
				}
			}
		}
	}
	*/
}

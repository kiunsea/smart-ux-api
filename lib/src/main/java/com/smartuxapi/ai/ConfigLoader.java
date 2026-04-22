package com.smartuxapi.ai;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigLoader {

	private static final Logger logger = LogManager.getLogger(ConfigLoader.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();
	private static final String CONFIG_FILE_NAME = "config.json"; // 기본 설정 파일 이름

	/**
	 * 기본 파일명으로 load (config.json)
	 *
	 * @return JsonNode
	 */
	public static JsonNode loadConfigFromClasspath() {
		return loadConfigFromClasspath(null);
	}

	/**
	 * 설정 파일을 로드합니다.
	 * 로딩 우선순위:
	 * 1. JAR 실행 디렉터리 (배포 환경)
	 * 2. classpath (개발 환경, JAR 내부 리소스)
	 *
	 * @param confFileName 설정 파일 이름 (null이면 기본값 config.json 사용)
	 * @return 로드된 설정 파일의 JsonNode 객체, 파일을 찾지 못하거나 파싱 오류 시 null
	 */
	public static JsonNode loadConfigFromClasspath(String confFileName) {

		if (confFileName == null) {
			confFileName = CONFIG_FILE_NAME;
		}

		// 1. JAR 실행 디렉터리에서 먼저 파일 찾기
		JsonNode configFromJarDir = loadFromJarDirectory(confFileName);
		if (configFromJarDir != null) {
			return configFromJarDir;
		}

		// 2. classpath에서 파일 찾기
		return loadFromClasspath(confFileName);
	}

	/**
	 * JAR 실행 디렉터리에서 설정 파일을 로드합니다.
	 *
	 * @param confFileName 설정 파일 이름
	 * @return JsonNode 또는 null
	 */
	private static JsonNode loadFromJarDirectory(String confFileName) {
		try {
			String jarDir = getJarDirectory();
			File configFile = new File(jarDir, confFileName);

			if (configFile.exists() && configFile.isFile()) {
				logger.info("설정 파일 '{}' 을(를) JAR 디렉터리에서 로드합니다: {}", confFileName, configFile.getAbsolutePath());

				try (InputStream inputStream = new FileInputStream(configFile)) {
					String jsonContent = new Scanner(inputStream, StandardCharsets.UTF_8.name())
							.useDelimiter("\\A").next();
					return objectMapper.readTree(jsonContent);
				}
			}
		} catch (Exception e) {
			logger.debug("JAR 디렉터리에서 설정 파일 '{}' 로드 실패: {}", confFileName, e.getMessage());
		}
		return null;
	}

	/**
	 * classpath에서 설정 파일을 로드합니다.
	 *
	 * @param confFileName 설정 파일 이름
	 * @return JsonNode 또는 null
	 */
	private static JsonNode loadFromClasspath(String confFileName) {
		try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(confFileName)) {

			if (inputStream == null) {
				logger.warn("설정 파일 '{}' 을(를) JAR 디렉터리 및 classpath에서 찾을 수 없습니다.", confFileName);
				return null;
			}

			String jsonContent = new Scanner(inputStream, StandardCharsets.UTF_8.name())
					.useDelimiter("\\A").next();

			logger.info("설정 파일 '{}' 을(를) classpath에서 로드했습니다", confFileName);

			return objectMapper.readTree(jsonContent);

		} catch (IOException e) {
			logger.error("설정 파일 '{}' 읽기 중 오류 발생: {}", confFileName, e.getMessage());
			return null;
		}
	}

	/**
	 * JAR 디렉터리 또는 프로젝트 루트 디렉터리를 반환합니다.
	 *
	 * @return JAR이 위치한 디렉터리 경로
	 */
	private static String getJarDirectory() {
		try {
			URL location = ConfigLoader.class.getProtectionDomain().getCodeSource().getLocation();
			URI uri = location.toURI();
			File file = new File(uri);

			if (file.isFile()) {
				// JAR 파일인 경우 부모 디렉터리 반환
				return file.getParent();
			} else {
				// IDE 실행 시 프로젝트 루트 찾기
				String path = file.getAbsolutePath();

				// build/classes/java/main, build/classes, target/classes 등 제거
				String[] patterns = {
					File.separator + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main",
					File.separator + "build" + File.separator + "classes",
					File.separator + "target" + File.separator + "classes",
					File.separator + "out" + File.separator + "production" + File.separator + "classes"
				};

				for (String pattern : patterns) {
					int index = path.indexOf(pattern);
					if (index > 0) {
						path = path.substring(0, index);
						break;
					}
				}

				return path;
			}
		} catch (Exception e) {
			logger.warn("JAR 디렉터리 확인 실패, 현재 디렉터리 사용: {}", e.getMessage());
			return System.getProperty("user.dir");
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

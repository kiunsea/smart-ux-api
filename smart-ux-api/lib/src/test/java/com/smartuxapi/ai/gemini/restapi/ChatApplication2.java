package com.smartuxapi.ai.gemini.restapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

/**
 * 이전 대화 내용도 유지하면서
 * 대화 시작전 숙지할 문서를 캐시로 학습 시킨후 대화에서 해당 내용을 활용
 */
public class ChatApplication2 {
	private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY"; // 여기에 실제 Gemini API 키를 입력하세요!
	private static final String GEMINI_MODEL_NAME = "gemini-2.5-flash"; // "gemini-pro", "gemini-2.0-flash", "gemini-2.5-flash"
	private static final String DOCUMENT_FILE_PATH = "D:\\GIT\\smux-api\\smart-ux-api\\lib\\src\\test\\java\\com\\smartuxapi\\ai\\gemini\\restapi\\document.txt"; // 캐시할 문서 파일 경로

	public static void main(String[] args) {
		
		JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
		String geminiApiKey = config.get("GEMINI_API_KEY").asText();
		if (geminiApiKey == null) geminiApiKey = GEMINI_API_KEY;
		
		if (geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
			System.err.println("오류: GEMINI_API_KEY를 실제 API 키로 교체해주세요.");
			return;
		}

		GeminiApiClient2 apiClient = new GeminiApiClient2(geminiApiKey, GEMINI_MODEL_NAME);
		ChatManager chatManager = new ChatManager();
		Scanner scanner = new Scanner(System.in);

		String cachedContentId = null;

		// 1. 문서 파일 읽기 및 Gemini에 캐시
		try {
			String documentContent = Files.readString(Paths.get(DOCUMENT_FILE_PATH), StandardCharsets.UTF_8);
			System.out.println("문서 파일 '" + DOCUMENT_FILE_PATH + "'을 읽었습니다.");
			System.out.println(documentContent);
			cachedContentId = apiClient.createCachedContent(documentContent);
			System.out.println("문서 캐싱 완료. 이제 대화에서 이 문서를 참조합니다.");
		} catch (IOException e) {
			System.err.println("오류: 문서 파일을 읽을 수 없습니다: " + DOCUMENT_FILE_PATH + " - " + e.getMessage());
			System.err.println("문서 캐싱 없이 진행합니다.");
		} catch (Exception e) {
			System.err.println("오류: Gemini Context Caching API 호출 실패: " + e.getMessage());
			System.err.println("문서 캐싱 없이 진행합니다.");
		}

		// --- 채팅 시작 ---
		String currentSessionId = UUID.randomUUID().toString();
		System.out.println("\n새로운 채팅 세션이 시작되었습니다. (세션 ID: " + currentSessionId + ")");
		System.out.println("대화를 시작하세요. '종료'를 입력하면 프로그램을 종료합니다.");
		System.out.println("'세션초기화'를 입력하면 현재 대화 기록을 지웁니다.");
		System.out.println("--------------------------------------------------");

		while (true) {
			System.out.print("나: ");
			String userInput = scanner.nextLine();

			if (userInput.equalsIgnoreCase("종료")) {
				System.out.println("채팅을 종료합니다.");
				break;
			}
			if (userInput.equalsIgnoreCase("세션초기화")) {
				chatManager.clearSession(currentSessionId);
				System.out.println("--------------------------------------------------");
				System.out.println("새로운 대화를 시작합니다.");
				System.out.println("--------------------------------------------------");
				continue;
			}

			try {
				// 2. 사용자 메시지를 대화 기록에 추가하고, Gemini에 보낼 전체 기록을 가져옴
				List<JSONObject> conversationHistory = chatManager.addUserMessage(currentSessionId, userInput);

				// 3. Gemini API 호출 (전체 대화 기록 + 캐시된 콘텐츠 ID 전송)
				System.out.println("AI 응답 대기 중...");
				String geminiResponse = apiClient.generateContent(conversationHistory, cachedContentId);

				// 4. Gemini 응답을 대화 기록에 추가
				chatManager.addModelResponse(currentSessionId, geminiResponse);

				System.out.println("AI: " + geminiResponse);

			} catch (Exception e) {
				System.err.println("오류 발생: " + e.getMessage());
				System.out.println("AI: 죄송합니다. 지금은 응답할 수 없습니다.");
			}
		}

		scanner.close();
	}
}

package com.smartuxapi.ai.gemini.restapi;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

public class ChatApplication {
	private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY"; // 여기에 실제 Gemini API 키를 입력하세요!
	private static final String GEMINI_MODEL_NAME = "gemini-2.5-flash"; // "gemini-pro", "gemini-2.0-flash", "gemini-2.5-flash"

	public static void main(String[] args) {
		
		JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
		String geminiApiKey = config.get("GEMINI_API_KEY").asText();
		if (geminiApiKey == null) geminiApiKey = GEMINI_API_KEY;
		
		if (geminiApiKey.equals("YOUR_GEMINI_API_KEY")) {
			System.err.println("오류: GEMINI_API_KEY를 실제 API 키로 교체해주세요.");
			return;
		}

		GeminiApiClient apiClient = new GeminiApiClient(geminiApiKey, GEMINI_MODEL_NAME);
		ChatManager chatManager = new ChatManager();
		Scanner scanner = new Scanner(System.in);

		// 간단한 세션 ID 생성 (실제 앱에서는 로그인 유저 ID 또는 더 견고한 세션 관리 필요)
		String currentSessionId = UUID.randomUUID().toString();
		System.out.println("새로운 채팅 세션이 시작되었습니다. (세션 ID: " + currentSessionId + ")");
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
				// 1. 사용자 메시지를 대화 기록에 추가하고, Gemini에 보낼 전체 기록을 가져옴
				List<JSONObject> conversationHistory = chatManager.addUserMessage(currentSessionId, userInput);

				// 2. Gemini API 호출 (전체 대화 기록 전송)
				System.out.println("AI 응답 대기 중...");
				String geminiResponse = apiClient.generateContent(conversationHistory);

				// 3. Gemini 응답을 대화 기록에 추가
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

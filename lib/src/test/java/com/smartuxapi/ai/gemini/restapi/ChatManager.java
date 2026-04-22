package com.smartuxapi.ai.gemini.restapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatManager {
	// 세션 ID를 키로 하여 해당 세션의 대화 기록을 저장
	// 실제 프로덕션 환경에서는 영구적인 저장소(예: 데이터베이스, Redis)를 사용해야 합니다.
	private final Map<String, List<JSONObject>> chatSessions = new ConcurrentHashMap<>();

	/**
	 * 사용자 메시지를 대화 기록에 추가하고, Gemini API 호출을 위한 전체 기록을 반환합니다.
	 * 
	 * @param sessionId   사용자 세션 ID
	 * @param userMessage 사용자 입력 메시지
	 * @return Gemini API에 전송할 전체 대화 기록
	 */
	public List<JSONObject> addUserMessage(String sessionId, String userMessage) {
		List<JSONObject> currentHistory = chatSessions.computeIfAbsent(sessionId, k -> new ArrayList<>());

		// 사용자 메시지 JSON 객체 생성
		JSONObject userContent = new JSONObject();
		userContent.put("role", "user");
		JSONArray userParts = new JSONArray();
		userParts.put(new JSONObject().put("text", userMessage));
		userContent.put("parts", userParts);

		currentHistory.add(userContent);
		return currentHistory; // 이 시점에서 Gemini에 보낼 전체 기록을 반환
	}

	/**
	 * Gemini 모델의 응답을 대화 기록에 추가합니다.
	 * 
	 * @param sessionId     사용자 세션 ID
	 * @param modelResponse Gemini 모델의 응답 텍스트
	 */
	public void addModelResponse(String sessionId, String modelResponse) {
		List<JSONObject> currentHistory = chatSessions.get(sessionId);
		if (currentHistory == null) {
			System.err.println("Error: No session found for ID: " + sessionId);
			return;
		}

		// 모델 응답 JSON 객체 생성
		JSONObject modelContent = new JSONObject();
		modelContent.put("role", "model");
		JSONArray modelParts = new JSONArray();
		modelParts.put(new JSONObject().put("text", modelResponse));
		modelContent.put("parts", modelParts);

		currentHistory.add(modelContent);
		// chatSessions 맵에 이미 참조가 있으므로 별도로 put할 필요는 없음
	}

	/**
	 * 특정 세션의 대화 기록을 초기화합니다.
	 * 
	 * @param sessionId 초기화할 세션 ID
	 */
	public void clearSession(String sessionId) {
		chatSessions.remove(sessionId);
		System.out.println("Session " + sessionId + " cleared.");
	}

	/**
	 * 현재 세션 기록을 가져옵니다. (디버깅용)
	 * 
	 * @param sessionId
	 * @return
	 */
	public List<JSONObject> getSessionHistory(String sessionId) {
		return chatSessions.getOrDefault(sessionId, Collections.emptyList());
	}
}

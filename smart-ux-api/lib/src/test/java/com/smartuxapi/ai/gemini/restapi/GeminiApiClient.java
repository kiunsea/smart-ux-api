package com.smartuxapi.ai.gemini.restapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiApiClient {
	private static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
	private final String apiKey;
	private final String modelName; // 예: "gemini-pro" 또는 "gemini-1.5-flash"

	public GeminiApiClient(String apiKey, String modelName) {
		this.apiKey = apiKey.trim();
		this.modelName = modelName.trim();
	}

	/**
	 * Gemini API에 대화 기록을 전송하고 응답을 받습니다.
	 * 
	 * @param conversationHistory 전체 대화 기록 (User, Model 메시지 포함)
	 * @return Gemini 모델의 응답 텍스트
	 * @throws Exception API 호출 중 발생한 예외
	 */
	public String generateContent(List<JSONObject> conversationHistory) throws Exception {
		String urlStr = GEMINI_API_URL_BASE + modelName + ":generateContent?key=" + apiKey;
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		//conn.setRequestProperty("X-goog-api-key", apiKey);
		//conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);

		// 요청 바디 생성
		JSONObject requestBody = new JSONObject();
		JSONArray contentsArray = new JSONArray();
		for (JSONObject message : conversationHistory) {
			contentsArray.put(message);
		}
		requestBody.put("contents", contentsArray);

		// 요청 바디 전송
		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
			wr.flush();
		}

		int responseCode = conn.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) { // 성공 (200 OK)
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
				StringBuilder response = new StringBuilder();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}

				JSONObject jsonResponse = new JSONObject(response.toString());
				JSONArray candidates = jsonResponse.getJSONArray("candidates");
				if (candidates.length() > 0) {
					JSONObject firstCandidate = candidates.getJSONObject(0);
					JSONObject content = firstCandidate.getJSONObject("content");
					JSONArray parts = content.getJSONArray("parts");
					if (parts.length() > 0) {
						return parts.getJSONObject(0).getString("text");
					}
				}
				return "응답에서 텍스트를 찾을 수 없습니다.";
			}
		} else { // 오류 응답
			try (BufferedReader errorIn = new BufferedReader(
					new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
				StringBuilder errorResponse = new StringBuilder();
				String errorLine;
				while ((errorLine = errorIn.readLine()) != null) {
					errorResponse.append(errorLine);
				}
				System.err.println("Gemini API Error Response Code: " + responseCode);
				System.err.println("Gemini API Error Message: " + errorResponse.toString());
				throw new Exception("Gemini API 호출 실패: " + responseCode + " - " + errorResponse.toString());
			}
		}
	}
}

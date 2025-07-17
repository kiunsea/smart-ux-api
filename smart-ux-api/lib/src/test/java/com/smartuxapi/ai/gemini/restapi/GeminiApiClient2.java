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

public class GeminiApiClient2 {
	private static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
	private static final String CREATE_CACHED_CONTENT_URL = "https://generativelanguage.googleapis.com/v1beta/cachedContents";

	private final String apiKey;
	private final String modelName; // 예: "gemini-pro" 또는 "gemini-1.5-flash"

	public GeminiApiClient2(String apiKey, String modelName) {
		this.apiKey = apiKey;
		this.modelName = modelName;
	}

	/**
	 * 특정 문서를 Gemini에 캐시하고 캐시된 콘텐츠 ID를 반환합니다. 캐시 유효 기간은 3600초 (1시간)으로 설정됩니다.
	 * 
	 * @param documentText 캐시할 문서의 텍스트
	 * @return 캐시된 콘텐츠 ID (예: "cachedContents/xxxxxxxxxxxx")
	 * @throws Exception API 호출 중 발생한 예외
	 */
	public String createCachedContent(String documentText) throws Exception {
		URL url = new URL(CREATE_CACHED_CONTENT_URL+"?key="+apiKey);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("X-goog-api-key", apiKey);
		conn.setDoOutput(true);

		// 요청 바디 생성 (캐시할 문서 내용을 'parts'에 포함)
		JSONObject requestBody = new JSONObject();
		requestBody.put("model", modelName); // 어떤 모델에서 캐시를 사용할지 지정

		JSONObject contentsObject = new JSONObject();
		JSONArray partsArray = new JSONArray();
		partsArray.put(new JSONObject().put("text", documentText));
		contentsObject.put("parts", partsArray);
		contentsObject.put("role", "user"); // 캐시할 내용은 주로 'user' role로 간주될 수 있음

		JSONArray contentsArray = new JSONArray();
		contentsArray.put(contentsObject);
		requestBody.put("contents", contentsArray);

		// 캐시 유효 기간 설정 (초 단위) - 여기서는 1시간
		requestBody.put("ttlSeconds", 3600);

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
				String cachedContentName = jsonResponse.getString("name"); // "cachedContents/..." 형식의 ID
				System.out.println("문서 캐시 성공! Cached Content ID: " + cachedContentName);
				return cachedContentName;
			}
		} else { // 오류 응답
			try (BufferedReader errorIn = new BufferedReader(
					new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
				StringBuilder errorResponse = new StringBuilder();
				String errorLine;
				while ((errorLine = errorIn.readLine()) != null) {
					errorResponse.append(errorLine);
				}
				System.err.println("Cached Content API Error Response Code: " + responseCode);
				System.err.println("Cached Content API Error Message: " + errorResponse.toString());
				throw new Exception("문서 캐싱 실패: " + responseCode + " - " + errorResponse.toString());
			}
		}
	}

	/**
	 * Gemini API에 대화 기록과 캐시된 콘텐츠 ID를 전송하고 응답을 받습니다.
	 * 
	 * @param conversationHistory 현재까지의 대화 기록 (사용자 입력)
	 * @param cachedContentId     캐시된 문서의 ID (선택 사항, null일 수 있음)
	 * @return Gemini 모델의 응답 텍스트
	 * @throws Exception API 호출 중 발생한 예외
	 */
	public String generateContent(List<JSONObject> conversationHistory, String cachedContentId) throws Exception {
		URL url = new URL(GEMINI_API_URL_BASE + modelName + ":generateContent?key="+apiKey);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("X-goog-api-key", apiKey);
		conn.setDoOutput(true);

		// 요청 바디 생성
		JSONObject requestBody = new JSONObject();

		// contents 배열에 기존 대화 기록 추가
		JSONArray contentsArray = new JSONArray();
		for (JSONObject message : conversationHistory) {
			contentsArray.put(message);
		}
		requestBody.put("contents", contentsArray);

		// 캐시된 콘텐츠가 있다면 'cached_content' 필드 추가
		if (cachedContentId != null && !cachedContentId.isEmpty()) {
			JSONObject cachedContent = new JSONObject();
			cachedContent.put("name", cachedContentId);
			requestBody.put("cached_content", cachedContent);
		}

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

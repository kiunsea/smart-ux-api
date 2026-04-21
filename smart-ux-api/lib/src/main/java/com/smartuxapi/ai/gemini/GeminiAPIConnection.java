package com.smartuxapi.ai.gemini;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.gemini.GeminiContextCacheStrategy;

/**
 * API에 연결한다.
 * ref : https://ai.google.dev/gemini-api/docs?hl=ko#rest
 */
public class GeminiAPIConnection {

    private Logger log = LogManager.getLogger(GeminiAPIConnection.class);
    private static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private String apiKey = null;
    private String modelName = null;

    public GeminiAPIConnection(String apiKey, String modelName) {
        this.apiKey = apiKey.trim();
        this.modelName = modelName.trim();
    }

    public String getApiKey() { return apiKey; }
    public String getModelName() { return modelName; }

    /**
     * Gemini API에 대화 기록을 전송하고 응답을 받습니다.
     *
     * @param contentsArray 전체 대화 기록 (User, Model 메시지 포함)
     * @return Gemini 모델의 응답 텍스트
     * @throws Exception API 호출 중 발생한 예외
     */
    public String generateContent(JSONArray contentsArray) throws Exception {
        return generateContent(contentsArray, null);
    }

    /**
     * Gemini API에 대화 기록을 전송하고 응답을 받습니다. 캐시 전략이 주입되고
     * 해당 전략이 {@link GeminiContextCacheStrategy} 이며 prime 되어 있다면
     * 요청에 {@code cachedContent} 필드를 포함시키고, 응답 메트릭을 기록합니다.
     *
     * @param contentsArray 전체 대화 기록
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @return Gemini 모델의 응답 텍스트
     * @throws Exception API 호출 중 발생한 예외
     * @since 0.7.0
     */
    public String generateContent(JSONArray contentsArray, CacheStrategy cacheStrategy) throws Exception {
        String urlStr = GEMINI_API_URL_BASE + modelName + ":generateContent?key=" + apiKey;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-goog-api-key", apiKey);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        // 요청 바디 생성
        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", contentsArray);

        // 캐시 리소스 참조 주입 (Gemini context caching)
        if (cacheStrategy instanceof GeminiContextCacheStrategy) {
            String cacheName = ((GeminiContextCacheStrategy) cacheStrategy).getCacheResourceName();
            if (cacheName != null && !cacheName.isEmpty()) {
                requestBody.put("cachedContent", cacheName);
                log.debug("Gemini request using cachedContent: {}", cacheName);
            }
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

                // 캐시 전략이 주입된 경우 메트릭 기록 (usageMetadata.cachedContentTokenCount)
                if (cacheStrategy != null) {
                    try {
                        JsonNode jacksonNode = new ObjectMapper().readTree(response.toString());
                        cacheStrategy.recordMetricsFromResponse(jacksonNode);
                    } catch (Exception metricsEx) {
                        log.warn("캐시 메트릭 기록 실패 (무시하고 계속): " + metricsEx.getMessage());
                    }
                }

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
                log.error("Gemini API Error Response Code: " + responseCode);
                log.error("Gemini API Error Message: " + errorResponse.toString());
                throw new Exception("Gemini API 호출 실패: " + responseCode + " - " + errorResponse.toString());
            }
        }
    }
}

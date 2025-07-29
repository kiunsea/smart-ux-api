package com.smartuxapi.ai.openai;

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

/**
 * API에 연결한다.
 * ref : https://platform.openai.com/docs/api-reference/responses
 */
public class ResponsesAPIConnection {
    
    private Logger log = LogManager.getLogger(ResponsesAPIConnection.class);
    private static final String OPENAI_API_URL_BASE = "https://api.openai.com/v1/responses";
    
    private String apiKey = null;
    private String modelName = null;
    
    public ResponsesAPIConnection(String apiKey, String modelName) {
        this.apiKey = apiKey.trim();
        this.modelName = modelName.trim();
    }
    
    /**
     * Responses API에 대화 기록을 전송하고 응답을 받습니다.
     * 
     * @param conversationHistory 전체 대화 기록 (User, Model 메시지 포함)
     * @return AI 모델의 응답 텍스트
     * @throws Exception API 호출 중 발생한 예외
     */
    public String generateContent(List<JSONObject> conversationHistory) throws Exception {
        URL url = new URL(OPENAI_API_URL_BASE);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer " + this.apiKey);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        // 요청 바디 생성
        JSONObject requestBody = new JSONObject();
        JSONArray contentsArray = new JSONArray();
        for (JSONObject message : conversationHistory) {
            contentsArray.put(message);
        }
        requestBody.put("input", contentsArray);
        
        // AI모델 설정
        requestBody.put("model", this.modelName);

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

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonRes = mapper.readTree(response.toString());

                // 1. "output" 배열 값 추출
                JsonNode outputArray = jsonRes.get("output");

                if (outputArray != null && outputArray.isArray()) {
                    // 2. 해당 array에서 role이 "assistant"인 JSON 인스턴스 찾기
                    for (JsonNode outputItem : outputArray) {
                        JsonNode roleNode = outputItem.get("role");
                        if (roleNode != null && "assistant".equals(roleNode.asText())) {
                            // 3. 해당 instance의 "content" array 값 추출
                            JsonNode contentArray = outputItem.get("content");
                            if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                                // 4. 첫 번째 JSON 인스턴스의 "text" 값 추출
                                JsonNode firstContentItem = contentArray.get(0);
                                JsonNode textNode = firstContentItem.get("text");

                                if (textNode != null) {
                                    log.debug("추출된 텍스트: " + textNode.asText());
                                    return textNode.asText(); // 원하는 값을 찾았으므로 종료
                                }
                            }
                        }
                    }
                    log.debug("Assistant 역할의 content 또는 text를 찾을 수 없습니다.");
                } else {
                    log.debug("output 배열을 찾을 수 없거나 배열이 아닙니다.");
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
                log.error("OpenAI API Error Response Code: " + responseCode);
                log.error("OpenAI API Error Message: " + errorResponse.toString());
                throw new Exception("OpenAI API 호출 실패: " + responseCode + " - " + errorResponse.toString());
            }
        }
    }
}

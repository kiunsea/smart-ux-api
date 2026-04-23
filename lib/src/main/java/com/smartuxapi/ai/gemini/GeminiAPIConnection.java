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

import java.util.ArrayList;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.gemini.GeminiContextCacheStrategy;
import com.smartuxapi.ai.cost.CostEntry;
import com.smartuxapi.ai.cost.CostTable;
import com.smartuxapi.ai.cost.CostTracker;
import com.smartuxapi.ai.cost.FallbackContext;
import com.smartuxapi.ai.cost.TokenUsageExtractor;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.tools.ToolCall;
import com.smartuxapi.ai.tools.ToolDefinition;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolTurnResult;

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
        return generateContent(contentsArray, null, null);
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
        return generateContent(contentsArray, cacheStrategy, null);
    }

    /**
     * Gemini API에 대화 기록을 전송하고 응답을 받습니다. {@code responseSchema} 가 주입되면
     * 요청에 {@code generationConfig.responseMimeType = "application/json"} 과
     * {@code responseSchema} 를 포함시켜 구조화된 JSON 응답을 강제합니다.
     *
     * @param contentsArray 전체 대화 기록
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @param responseSchema 응답 JSON Schema (null 허용)
     * @return Gemini 모델의 응답 텍스트 (schema 주입 시 JSON 문자열)
     * @throws Exception API 호출 중 발생한 예외
     * @since 0.8.0
     */
    public String generateContent(JSONArray contentsArray, CacheStrategy cacheStrategy,
                                  ResponseSchema responseSchema) throws Exception {
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

        // 구조화 응답(JSON Schema) 주입 — Gemini generationConfig 규격
        if (responseSchema != null) {
            JSONObject genConfig = requestBody.optJSONObject("generationConfig");
            if (genConfig == null) {
                genConfig = new JSONObject();
            }
            genConfig.put("responseMimeType", "application/json");
            // Jackson JsonNode → org.json 브릿지
            genConfig.put("responseSchema", new JSONObject(responseSchema.getSchema().toString()));
            requestBody.put("generationConfig", genConfig);
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

                JsonNode jacksonNode = new ObjectMapper().readTree(response.toString());

                // 캐시 전략이 주입된 경우 메트릭 기록 (usageMetadata.cachedContentTokenCount)
                if (cacheStrategy != null) {
                    try {
                        cacheStrategy.recordMetricsFromResponse(jacksonNode);
                    } catch (Exception metricsEx) {
                        log.warn("캐시 메트릭 기록 실패 (무시하고 계속): " + metricsEx.getMessage());
                    }
                }

                // CostTracker 기록
                recordCost(jacksonNode, responseSchema != null ? "structured" : "chat");

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

    /**
     * Tool Use 지원 버전의 generateContent. 요청에 {@code tools.function_declarations} 를 포함시키고,
     * 응답 parts 중 {@code functionCall} 이 있으면 {@link ToolTurnResult#toolCalls} 로 반환한다.
     *
     * <p>Gemini 응답은 호출 ID 를 포함하지 않으므로 클라이언트에서 UUID 를 부여한다.
     * 동일 턴 내 여러 functionCall 이 있으면 각각 ID 를 받는다.
     *
     * @param contentsArray 대화 기록
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @param tools Tool 레지스트리 (null/empty 면 일반 generateContent 와 동등)
     * @return 이번 턴의 결과
     * @throws Exception API 호출 실패 등
     * @since 0.8.0
     */
    public ToolTurnResult generateContentWithTools(JSONArray contentsArray,
                                                   CacheStrategy cacheStrategy,
                                                   ToolRegistry tools) throws Exception {
        String urlStr = GEMINI_API_URL_BASE + modelName + ":generateContent?key=" + apiKey;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-goog-api-key", apiKey);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);

        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", contentsArray);

        if (cacheStrategy instanceof GeminiContextCacheStrategy) {
            String cacheName = ((GeminiContextCacheStrategy) cacheStrategy).getCacheResourceName();
            if (cacheName != null && !cacheName.isEmpty()) {
                requestBody.put("cachedContent", cacheName);
            }
        }

        if (tools != null && !tools.isEmpty()) {
            JSONArray functionDeclarations = new JSONArray();
            for (ToolDefinition def : tools.all()) {
                JSONObject fn = new JSONObject();
                fn.put("name", def.getName());
                if (def.getDescription() != null && !def.getDescription().isEmpty()) {
                    fn.put("description", def.getDescription());
                }
                fn.put("parameters", new JSONObject(def.getParametersSchema().toString()));
                functionDeclarations.put(fn);
            }
            JSONObject toolsObj = new JSONObject();
            toolsObj.put("functionDeclarations", functionDeclarations);
            JSONArray toolsArr = new JSONArray();
            toolsArr.put(toolsObj);
            requestBody.put("tools", toolsArr);
        }

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader errorIn = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder err = new StringBuilder();
                String line;
                while ((line = errorIn.readLine()) != null) err.append(line);
                log.error("Gemini Tool Use API Error " + responseCode + ": " + err);
                throw new Exception("Gemini API 호출 실패 (tool use): " + responseCode + " - " + err);
            }
        }

        String responseBody;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            responseBody = sb.toString();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonRes = mapper.readTree(responseBody);

        if (cacheStrategy != null) {
            try {
                cacheStrategy.recordMetricsFromResponse(jsonRes);
            } catch (Exception metricsEx) {
                log.warn("캐시 메트릭 기록 실패 (무시): " + metricsEx.getMessage());
            }
        }

        recordCost(jsonRes, "tool_use");

        JsonNode candidates = jsonRes.get("candidates");
        if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
            return ToolTurnResult.finalText("응답에서 candidates 를 찾을 수 없습니다.", "{}");
        }
        JsonNode candidate0 = candidates.get(0);
        JsonNode content = candidate0.get("content");
        if (content == null) {
            return ToolTurnResult.finalText("응답 content 누락", "{}");
        }

        // parts 순회하여 functionCall / text 수집
        java.util.List<ToolCall> calls = new ArrayList<>();
        StringBuilder textBuf = new StringBuilder();
        JsonNode parts = content.get("parts");
        if (parts != null && parts.isArray()) {
            for (JsonNode part : parts) {
                JsonNode fc = part.get("functionCall");
                if (fc != null) {
                    String name = fc.path("name").asText("");
                    JsonNode args = fc.get("args");
                    if (args == null) args = mapper.createObjectNode();
                    String callId = "gemini-call-" + UUID.randomUUID();
                    if (!name.isEmpty()) {
                        calls.add(new ToolCall(callId, name, args));
                    }
                } else {
                    JsonNode textNode = part.get("text");
                    if (textNode != null) textBuf.append(textNode.asText());
                }
            }
        }

        // content 는 그대로 히스토리에 replay 해야 하므로 원본 문자열 보존
        String rawContent = content.toString();

        if (!calls.isEmpty()) {
            return ToolTurnResult.toolCalls(calls, rawContent);
        }
        return ToolTurnResult.finalText(textBuf.toString(), rawContent);
    }

    /**
     * 응답 파싱 후 CostTracker 에 토큰/비용 기록. 실패는 무시 (로깅만).
     */
    private void recordCost(JsonNode responseJson, String callKind) {
        try {
            TokenUsageExtractor.Usage u = TokenUsageExtractor.fromGemini(responseJson);
            double cost = CostTable.calculate(this.modelName, u.inputTokens, u.outputTokens);
            CostTracker.INSTANCE.record(new CostEntry(
                    "gemini", this.modelName, u.inputTokens, u.outputTokens, cost,
                    FallbackContext.isFallback(), callKind));
        } catch (Exception e) {
            log.warn("CostTracker 기록 실패 (무시): " + e.getMessage());
        }
    }
}

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

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheStrategy;
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
    public String generateContent(JSONArray conversationHistory) throws Exception {
        return generateContent(conversationHistory, null, null);
    }

    /**
     * Responses API에 대화 기록을 전송하고 응답을 받습니다. 캐시 전략이 주입되면 응답의
     * {@code usage.prompt_tokens_details.cached_tokens} 를 파싱하여 메트릭을 갱신합니다.
     *
     * @param conversationHistory 전체 대화 기록
     * @param cacheStrategy 캐시 전략 (null 허용 — 메트릭 기록 생략)
     * @return AI 모델의 응답 텍스트
     * @throws Exception API 호출 중 발생한 예외
     * @since 0.7.0
     */
    public String generateContent(JSONArray conversationHistory, CacheStrategy cacheStrategy) throws Exception {
        return generateContent(conversationHistory, cacheStrategy, null);
    }

    /**
     * Responses API에 대화 기록을 전송하고 응답을 받습니다. {@code responseSchema} 가 주입되면
     * 요청에 {@code text.format = json_schema} 구성을 포함시켜 구조화된 JSON 응답을 강제합니다.
     *
     * @param conversationHistory 전체 대화 기록
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @param responseSchema 응답 JSON Schema (null 허용 — 자유형 텍스트)
     * @return AI 모델의 응답 텍스트 (schema 주입 시 JSON 문자열)
     * @throws Exception API 호출 중 발생한 예외
     * @since 0.8.0
     */
    public String generateContent(JSONArray conversationHistory, CacheStrategy cacheStrategy,
                                  ResponseSchema responseSchema) throws Exception {
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
        requestBody.put("input", conversationHistory);
        
        // AI모델 설정
        requestBody.put("model", this.modelName);

        // 구조화 응답(JSON Schema) 주입 — Responses API text.format 규격
        if (responseSchema != null) {
            JSONObject formatNode = new JSONObject();
            formatNode.put("type", "json_schema");
            formatNode.put("name", responseSchema.getName());
            formatNode.put("strict", responseSchema.isStrict());
            if (responseSchema.getDescription() != null) {
                formatNode.put("description", responseSchema.getDescription());
            }
            // Jackson JsonNode → org.json 객체로 브릿지
            formatNode.put("schema", new JSONObject(responseSchema.getSchema().toString()));
            JSONObject textNode = new JSONObject();
            textNode.put("format", formatNode);
            requestBody.put("text", textNode);
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

                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonRes = mapper.readTree(response.toString());

                // 캐시 전략이 주입된 경우 메트릭 기록 (usage.prompt_tokens_details.cached_tokens)
                if (cacheStrategy != null) {
                    try {
                        cacheStrategy.recordMetricsFromResponse(jsonRes);
                    } catch (Exception metricsEx) {
                        log.warn("캐시 메트릭 기록 실패 (무시하고 계속): " + metricsEx.getMessage());
                    }
                }

                // CostTracker 기록
                recordCost(jsonRes, responseSchema != null ? "structured" : "chat");

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

    /**
     * Tool Use 지원 버전의 generateContent. 요청에 {@code tools} 정의를 포함시키고,
     * 응답에서 {@code function_call} 아이템이 있으면 {@link ToolTurnResult#toolCalls} 로 반환한다.
     * 없으면 assistant 메시지의 text 를 추출해 {@link ToolTurnResult#finalText} 로 반환.
     *
     * @param conversationHistory 대화 기록 (assistant output items, tool results 포함 가능)
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @param tools Tool 레지스트리 (null/empty 면 일반 generateContent 와 동등)
     * @return 이번 턴의 결과
     * @throws Exception API 호출 실패 등
     * @since 0.8.0
     */
    public ToolTurnResult generateContentWithTools(JSONArray conversationHistory,
                                                   CacheStrategy cacheStrategy,
                                                   ToolRegistry tools) throws Exception {
        URL url = new URL(OPENAI_API_URL_BASE);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer " + this.apiKey);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);

        JSONObject requestBody = new JSONObject();
        requestBody.put("input", conversationHistory);
        requestBody.put("model", this.modelName);

        // Tool 정의 주입 — Responses API tools 포맷: [{type:"function", name, description, parameters, strict}]
        if (tools != null && !tools.isEmpty()) {
            JSONArray toolsArr = new JSONArray();
            for (ToolDefinition def : tools.all()) {
                JSONObject t = new JSONObject();
                t.put("type", "function");
                t.put("name", def.getName());
                if (def.getDescription() != null && !def.getDescription().isEmpty()) {
                    t.put("description", def.getDescription());
                }
                t.put("parameters", new JSONObject(def.getParametersSchema().toString()));
                toolsArr.put(t);
            }
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
                log.error("OpenAI Tool Use API Error " + responseCode + ": " + err);
                throw new Exception("OpenAI API 호출 실패 (tool use): " + responseCode + " - " + err);
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

        JsonNode outputArray = jsonRes.get("output");
        if (outputArray == null || !outputArray.isArray()) {
            return ToolTurnResult.finalText("응답에서 output 을 찾을 수 없습니다.",
                    new JSONArray().toString());
        }

        // function_call 아이템 수집
        java.util.List<ToolCall> calls = new ArrayList<>();
        String finalText = null;
        // output 을 raw items 로 보존하기 위해 JSONArray 재구성
        JSONArray rawOutput = new JSONArray(outputArray.toString());

        for (JsonNode item : outputArray) {
            String type = item.path("type").asText("");
            if ("function_call".equals(type)) {
                String callId = item.path("call_id").asText(
                        item.path("id").asText(""));
                String name = item.path("name").asText("");
                String argsStr = item.path("arguments").asText("{}");
                JsonNode argsNode;
                try {
                    argsNode = mapper.readTree(argsStr);
                } catch (Exception e) {
                    argsNode = mapper.createObjectNode();
                }
                if (!callId.isEmpty() && !name.isEmpty()) {
                    calls.add(new ToolCall(callId, name, argsNode));
                }
            } else if ("message".equals(type) || "assistant".equals(item.path("role").asText(""))) {
                // 최종 텍스트 후보
                JsonNode contentArr = item.get("content");
                if (contentArr != null && contentArr.isArray() && contentArr.size() > 0) {
                    JsonNode first = contentArr.get(0);
                    JsonNode text = first.get("text");
                    if (text != null) finalText = text.asText();
                }
            }
        }

        if (!calls.isEmpty()) {
            return ToolTurnResult.toolCalls(calls, rawOutput.toString());
        }
        return ToolTurnResult.finalText(finalText != null ? finalText : "", rawOutput.toString());
    }

    /**
     * 응답 파싱 후 CostTracker 에 토큰/비용 기록. 예외는 로그만 남기고 무시 (메인 흐름 보호).
     */
    private void recordCost(JsonNode responseJson, String callKind) {
        try {
            TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(responseJson);
            double cost = CostTable.calculate(this.modelName, u.inputTokens, u.outputTokens);
            CostTracker.INSTANCE.record(new CostEntry(
                    "openai", this.modelName, u.inputTokens, u.outputTokens, cost,
                    FallbackContext.isFallback(), callKind));
        } catch (Exception e) {
            log.warn("CostTracker 기록 실패 (무시): " + e.getMessage());
        }
    }
}

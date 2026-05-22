package com.smartuxapi.ai.claude;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.claude.ClaudePromptCacheStrategy;
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
 * Anthropic Claude Messages API에 연결한다.
 * ref : https://docs.anthropic.com/en/api/messages
 *
 * @since 0.10.0
 */
public class ClaudeAPIConnection {

    private Logger log = LogManager.getLogger(ClaudeAPIConnection.class);

    private static final String CLAUDE_API_URL = "https://api.anthropic.com/v1/messages";

    /** {@code anthropic-version} 헤더 기본값. 호출자가 properties 로 override 가능. */
    public static final String DEFAULT_ANTHROPIC_VERSION = "2023-06-01";

    /** {@code max_tokens} 기본값 — Anthropic Messages API 필수 필드. */
    public static final int DEFAULT_MAX_TOKENS = 4096;

    private final String apiKey;
    private final String modelName;
    private final String anthropicVersion;

    public ClaudeAPIConnection(String apiKey, String modelName) {
        this(apiKey, modelName, DEFAULT_ANTHROPIC_VERSION);
    }

    /**
     * @param apiKey Anthropic API 키
     * @param modelName 모델명 (예: claude-sonnet-4-6)
     * @param anthropicVersion {@code anthropic-version} 헤더 값 (null/빈 문자열이면 기본값)
     */
    public ClaudeAPIConnection(String apiKey, String modelName, String anthropicVersion) {
        this.apiKey = apiKey.trim();
        this.modelName = modelName.trim();
        this.anthropicVersion = (anthropicVersion == null || anthropicVersion.trim().isEmpty())
                ? DEFAULT_ANTHROPIC_VERSION : anthropicVersion.trim();
    }

    public String getModelName() { return modelName; }
    public String getAnthropicVersion() { return anthropicVersion; }

    /**
     * Claude API에 대화 기록을 전송하고 응답 텍스트를 받습니다.
     *
     * @param messages 전체 대화 기록 (user/assistant 메시지)
     * @return 모델 응답 텍스트
     * @throws Exception API 호출 중 발생한 예외
     */
    public String generateContent(JSONArray messages) throws Exception {
        return generateContent(messages, null, null);
    }

    /**
     * 캐시 전략을 주입하여 호출합니다. 전략이 {@link ClaudePromptCacheStrategy} 이며 힌트가
     * prime 되어 있으면 top-level {@code system} 블록에 {@code cache_control} 을 부여하고
     * 응답 메트릭을 기록합니다.
     *
     * @since 0.10.0
     */
    public String generateContent(JSONArray messages, CacheStrategy cacheStrategy) throws Exception {
        return generateContent(messages, cacheStrategy, null);
    }

    /**
     * {@code responseSchema} 가 주입되면 구조화된 JSON 응답을 강제합니다.
     *
     * <p>Anthropic Messages API는 OpenAI 의 {@code text.format.json_schema} 같은 네이티브
     * 구조화 응답 필드가 없으므로, 스키마를 단일 tool 의 {@code input_schema} 로 등록하고
     * {@code tool_choice} 로 호출을 강제하는 방식으로 구현합니다. 강제된 {@code tool_use}
     * 블록의 {@code input} 을 JSON 문자열로 반환합니다.
     *
     * @param messages 전체 대화 기록
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @param responseSchema 응답 JSON Schema (null 허용)
     * @return 모델 응답 텍스트 (schema 주입 시 JSON 문자열)
     * @throws Exception API 호출 중 발생한 예외
     * @since 0.10.0
     */
    public String generateContent(JSONArray messages, CacheStrategy cacheStrategy,
                                  ResponseSchema responseSchema) throws Exception {
        JSONObject requestBody = baseRequest(messages);
        applySystemCache(requestBody, cacheStrategy);

        if (responseSchema != null) {
            JSONObject tool = new JSONObject();
            tool.put("name", responseSchema.getName());
            if (responseSchema.getDescription() != null) {
                tool.put("description", responseSchema.getDescription());
            }
            tool.put("input_schema", new JSONObject(responseSchema.getSchema().toString()));
            requestBody.put("tools", new JSONArray().put(tool));
            JSONObject toolChoice = new JSONObject();
            toolChoice.put("type", "tool");
            toolChoice.put("name", responseSchema.getName());
            requestBody.put("tool_choice", toolChoice);
        }

        JsonNode jsonRes = post(requestBody);

        if (cacheStrategy != null) {
            try {
                cacheStrategy.recordMetricsFromResponse(jsonRes);
            } catch (Exception metricsEx) {
                log.warn("캐시 메트릭 기록 실패 (무시하고 계속): " + metricsEx.getMessage());
            }
        }

        recordCost(jsonRes, responseSchema != null ? "structured" : "chat");

        if (responseSchema != null) {
            String structured = extractForcedToolInput(jsonRes, responseSchema.getName());
            if (structured != null) {
                return structured;
            }
            log.warn("Claude 구조화 응답에서 tool_use 입력을 찾지 못함 — 텍스트로 폴백");
        }
        return extractText(jsonRes);
    }

    /**
     * Tool Use 지원 버전. 요청에 {@code tools} 정의를 포함시키고, 응답 {@code content[]} 의
     * {@code tool_use} 블록이 있으면 {@link ToolTurnResult#toolCalls} 로 반환한다.
     *
     * @param messages 대화 기록 (assistant content blocks, tool_result 포함 가능)
     * @param cacheStrategy 캐시 전략 (null 허용)
     * @param tools Tool 레지스트리 (null/empty 면 일반 generateContent 와 동등)
     * @return 이번 턴의 결과
     * @throws Exception API 호출 실패 등
     * @since 0.10.0
     */
    public ToolTurnResult generateContentWithTools(JSONArray messages, CacheStrategy cacheStrategy,
                                                   ToolRegistry tools) throws Exception {
        JSONObject requestBody = baseRequest(messages);
        applySystemCache(requestBody, cacheStrategy);

        if (tools != null && !tools.isEmpty()) {
            JSONArray toolsArr = new JSONArray();
            for (ToolDefinition def : tools.all()) {
                JSONObject t = new JSONObject();
                t.put("name", def.getName());
                if (def.getDescription() != null && !def.getDescription().isEmpty()) {
                    t.put("description", def.getDescription());
                }
                t.put("input_schema", new JSONObject(def.getParametersSchema().toString()));
                toolsArr.put(t);
            }
            requestBody.put("tools", toolsArr);
        }

        JsonNode jsonRes = post(requestBody);

        if (cacheStrategy != null) {
            try {
                cacheStrategy.recordMetricsFromResponse(jsonRes);
            } catch (Exception metricsEx) {
                log.warn("캐시 메트릭 기록 실패 (무시): " + metricsEx.getMessage());
            }
        }

        recordCost(jsonRes, "tool_use");

        JsonNode content = jsonRes.get("content");
        if (content == null || !content.isArray()) {
            return ToolTurnResult.finalText("응답에서 content 를 찾을 수 없습니다.", "[]");
        }
        // content 배열 = 다음 턴 replay 용 assistant content blocks 원본 보존
        String rawAssistantContent = content.toString();

        List<ToolCall> calls = new ArrayList<>();
        StringBuilder textBuf = new StringBuilder();
        for (JsonNode block : content) {
            String type = block.path("type").asText("");
            if ("tool_use".equals(type)) {
                String id = block.path("id").asText("");
                String name = block.path("name").asText("");
                JsonNode input = block.get("input");
                if (input == null) input = new ObjectMapper().createObjectNode();
                if (!id.isEmpty() && !name.isEmpty()) {
                    calls.add(new ToolCall(id, name, input));
                }
            } else if ("text".equals(type)) {
                textBuf.append(block.path("text").asText(""));
            }
        }

        if (!calls.isEmpty()) {
            return ToolTurnResult.toolCalls(calls, rawAssistantContent);
        }
        return ToolTurnResult.finalText(textBuf.toString(), rawAssistantContent);
    }

    // ===== 내부 헬퍼 =====

    private JSONObject baseRequest(JSONArray messages) {
        JSONObject body = new JSONObject();
        body.put("model", this.modelName);
        body.put("max_tokens", DEFAULT_MAX_TOKENS);
        body.put("messages", messages);
        return body;
    }

    /**
     * 캐시 힌트가 prime 되어 있으면 top-level {@code system} 블록으로 배치하고
     * {@code cache_control: ephemeral} 을 부여한다. Anthropic 은 {@code system} 을
     * {@code messages} 배열에 넣을 수 없으므로 별도 필드로 처리한다.
     */
    private void applySystemCache(JSONObject requestBody, CacheStrategy cacheStrategy) {
        if (!(cacheStrategy instanceof ClaudePromptCacheStrategy)) return;
        CacheHint hint = cacheStrategy.getCurrentHint();
        if (hint == null || hint.getContent() == null || hint.getContent().isEmpty()) return;
        JSONObject sysBlock = new JSONObject();
        sysBlock.put("type", "text");
        sysBlock.put("text", hint.getContent());
        sysBlock.put("cache_control", new JSONObject().put("type", "ephemeral"));
        requestBody.put("system", new JSONArray().put(sysBlock));
        log.debug("Claude request using cached system prefix (len={})", hint.getContentLength());
    }

    private JsonNode post(JSONObject requestBody) throws Exception {
        URL url = new URL(CLAUDE_API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("x-api-key", this.apiKey);
        conn.setRequestProperty("anthropic-version", this.anthropicVersion);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(requestBody.toString().getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            String err = readStream(conn, true);
            log.error("Claude API Error Response Code: " + responseCode);
            log.error("Claude API Error Message: " + err);
            throw new Exception("Claude API 호출 실패: " + responseCode + " - " + err);
        }
        return new ObjectMapper().readTree(readStream(conn, false));
    }

    private String readStream(HttpURLConnection conn, boolean error) throws Exception {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
                error ? conn.getErrorStream() : conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Anthropic 응답 {@code content[]} 에서 첫 text 블록을 추출. 없으면 진단 메시지 반환.
     */
    private String extractText(JsonNode jsonRes) {
        JsonNode content = jsonRes.get("content");
        if (content != null && content.isArray()) {
            for (JsonNode block : content) {
                if ("text".equals(block.path("type").asText(""))) {
                    String text = block.path("text").asText("");
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
        }
        String stopReason = jsonRes.path("stop_reason").asText("UNKNOWN");
        log.warn("Claude 응답에 text 블록 누락 (stop_reason={}). 전체 응답: {}", stopReason, jsonRes);
        return "응답에서 텍스트를 찾을 수 없습니다 (stop_reason=" + stopReason + ")";
    }

    /**
     * 구조화 응답 — 강제된 {@code tool_use} 블록의 {@code input} 을 JSON 문자열로 추출.
     */
    private String extractForcedToolInput(JsonNode jsonRes, String toolName) {
        JsonNode content = jsonRes.get("content");
        if (content == null || !content.isArray()) return null;
        for (JsonNode block : content) {
            if ("tool_use".equals(block.path("type").asText(""))
                    && toolName.equals(block.path("name").asText(""))) {
                JsonNode input = block.get("input");
                if (input != null) {
                    return input.toString();
                }
            }
        }
        return null;
    }

    /**
     * 응답 파싱 후 CostTracker 에 토큰/비용 기록. 실패는 무시 (로깅만).
     */
    private void recordCost(JsonNode responseJson, String callKind) {
        try {
            TokenUsageExtractor.Usage u = TokenUsageExtractor.fromClaude(responseJson);
            double cost = CostTable.calculate(this.modelName, u.inputTokens, u.outputTokens);
            CostTracker.INSTANCE.record(new CostEntry(
                    "anthropic", this.modelName, u.inputTokens, u.outputTokens, cost,
                    FallbackContext.isFallback(), callKind));
        } catch (Exception e) {
            log.warn("CostTracker 기록 실패 (무시): " + e.getMessage());
        }
    }
}

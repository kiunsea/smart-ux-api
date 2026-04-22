package com.smartuxapi.ai.cache.gemini;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;

/**
 * Gemini API의 {@code cachedContents} 리소스를 사용하는 명시적 컨텍스트 캐시 전략.
 *
 * <p>동작:
 * <ol>
 *   <li>{@link #prime(CacheHint)} — {@code POST /v1beta/cachedContents} 로 서버에 캐시 생성</li>
 *   <li>생성된 리소스 이름(예: {@code cachedContents/abc123}) 을 보관</li>
 *   <li>{@link com.smartuxapi.ai.gemini.GeminiAPIConnection} 이 {@code generateContent} 요청 본문에
 *       {@code cachedContent} 필드로 이 이름을 포함</li>
 *   <li>{@link #invalidate()} — {@code DELETE /v1beta/cachedContents/{id}}</li>
 * </ol>
 *
 * <p>⚠️ 주의: Gemini 컨텍스트 캐시에는 <b>최소 토큰 수 요건</b>이 있다 (모델별 상이, 일반적으로 32,768 토큰).
 * 이보다 작은 콘텐츠로 prime 을 시도하면 서버가 400 을 반환한다. 이 경우 예외가 던져지므로
 * 호출자가 폴백(no-cache 진행) 여부를 결정해야 한다.
 *
 * @since 0.7.0
 */
public class GeminiContextCacheStrategy implements CacheStrategy {

    private static final Logger log = LogManager.getLogger(GeminiContextCacheStrategy.class);
    private static final String GEMINI_CACHE_URL_BASE =
            "https://generativelanguage.googleapis.com/v1beta/cachedContents";

    private final String apiKey;
    private final String modelName; // fully-qualified "models/xxx"

    private CacheHint currentHint = null;
    private String cacheResourceName = null; // e.g. "cachedContents/abc123"
    private CacheMetrics lastMetrics = CacheMetrics.EMPTY;

    /**
     * @param apiKey Gemini API 키
     * @param modelName 모델 식별자 — "gemini-1.5-flash" 또는 "models/gemini-1.5-flash" 모두 허용
     */
    public GeminiContextCacheStrategy(String apiKey, String modelName) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("apiKey must not be empty");
        }
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("modelName must not be empty");
        }
        this.apiKey = apiKey.trim();
        String trimmed = modelName.trim();
        this.modelName = trimmed.startsWith("models/") ? trimmed : "models/" + trimmed;
    }

    @Override
    public void prime(CacheHint hint) throws Exception {
        // 기존 캐시가 있으면 먼저 해제 (동일 세션에서 힌트 교체 시)
        if (this.cacheResourceName != null) {
            try {
                invalidate();
            } catch (Exception e) {
                log.warn("기존 Gemini 캐시 해제 실패 (계속 진행): " + e.getMessage());
            }
        }

        if (hint == null) {
            this.currentHint = null;
            return;
        }

        String urlStr = GEMINI_CACHE_URL_BASE + "?key=" + apiKey;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("X-goog-api-key", apiKey);
        conn.setRequestMethod("POST");
        conn.setUseCaches(false);

        // 요청 바디 구성
        JSONObject body = new JSONObject();
        body.put("model", this.modelName);
        body.put("ttl", hint.getTtlSeconds() + "s");

        JSONArray contents = new JSONArray();
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        JSONArray parts = new JSONArray();
        parts.put(new JSONObject().put("text", hint.getContent()));
        userMsg.put("parts", parts);
        contents.put(userMsg);
        body.put("contents", contents);

        if (hint.getLabel() != null && !"unlabeled".equals(hint.getLabel())) {
            body.put("displayName", hint.getLabel());
        }

        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(body.toString().getBytes(StandardCharsets.UTF_8));
            wr.flush();
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);

                JSONObject json = new JSONObject(response.toString());
                this.cacheResourceName = json.optString("name", null);
                this.currentHint = hint;
                if (this.cacheResourceName == null || this.cacheResourceName.isEmpty()) {
                    throw new IllegalStateException(
                            "Gemini cache response missing 'name' field: " + response);
                }
                log.info("Gemini cache primed: {} ({})", this.cacheResourceName, hint);
            }
        } else {
            String errorMsg = readErrorStream(conn);
            log.error("Gemini cache create failed (HTTP {}): {}", responseCode, errorMsg);
            throw new Exception("Gemini 캐시 생성 실패 (HTTP " + responseCode + "): " + errorMsg);
        }
    }

    @Override
    public CacheMetrics getLastMetrics() {
        return lastMetrics;
    }

    /**
     * Gemini 응답의 {@code usageMetadata.promptTokenCount} /
     * {@code usageMetadata.cachedContentTokenCount} 를 추출하여 메트릭을 갱신한다.
     */
    @Override
    public void recordMetricsFromResponse(JsonNode responseJson) {
        if (responseJson == null) return;
        JsonNode usage = responseJson.get("usageMetadata");
        if (usage == null) {
            log.debug("Gemini response missing 'usageMetadata' field; metrics unchanged");
            return;
        }

        long totalInput = readLong(usage, "promptTokenCount", 0L);
        long cached = readLong(usage, "cachedContentTokenCount", 0L);
        this.lastMetrics = new CacheMetrics(totalInput, cached, "gemini");
        log.debug("Gemini cache metrics: {}", this.lastMetrics);
    }

    @Override
    public void invalidate() throws Exception {
        if (this.cacheResourceName == null) {
            this.currentHint = null;
            this.lastMetrics = CacheMetrics.EMPTY;
            return;
        }

        String urlStr = "https://generativelanguage.googleapis.com/v1beta/"
                + this.cacheResourceName + "?key=" + apiKey;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("X-goog-api-key", apiKey);
        conn.setRequestMethod("DELETE");
        conn.setUseCaches(false);

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            log.info("Gemini cache invalidated: {}", this.cacheResourceName);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            log.info("Gemini cache already expired/not found: {}", this.cacheResourceName);
        } else {
            String errorMsg = readErrorStream(conn);
            log.warn("Gemini cache delete non-200 (HTTP {}): {}", responseCode, errorMsg);
            // 캐시 삭제 실패는 예외를 던지되, 호출자가 swallow 해도 TTL 로 자연 만료되므로 문제 없음
            throw new Exception("Gemini 캐시 삭제 실패 (HTTP " + responseCode + "): " + errorMsg);
        }

        this.cacheResourceName = null;
        this.currentHint = null;
        this.lastMetrics = CacheMetrics.EMPTY;
    }

    @Override
    public CacheHint getCurrentHint() {
        return currentHint;
    }

    @Override
    public String getProvider() {
        return "gemini";
    }

    /** 현재 캐시 리소스 이름 (예: "cachedContents/abc123"). prime 전 또는 invalidate 후 null. */
    public String getCacheResourceName() {
        return cacheResourceName;
    }

    // ----- helpers -----

    private static long readLong(JsonNode node, String field, long fallback) {
        JsonNode v = node.get(field);
        return (v == null || !v.canConvertToLong()) ? fallback : v.asLong();
    }

    private static String readErrorStream(HttpURLConnection conn) {
        try (BufferedReader errorIn = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = errorIn.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception readEx) {
            return "(stream read failed: " + readEx.getMessage() + ")";
        }
    }
}

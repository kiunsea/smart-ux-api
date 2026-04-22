package com.smartuxapi.ai.embedding.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartuxapi.ai.cost.CostEntry;
import com.smartuxapi.ai.cost.CostTable;
import com.smartuxapi.ai.cost.CostTracker;
import com.smartuxapi.ai.embedding.EmbeddingException;
import com.smartuxapi.ai.embedding.EmbeddingResult;
import com.smartuxapi.ai.embedding.EmbeddingService;

/**
 * Google Gemini 기반 EmbeddingService 구현체.
 *
 * <p>엔드포인트: {@code POST /v1beta/models/{model}:batchEmbedContents?key={apiKey}}.
 * 기본 모델 {@code text-embedding-004} (768 차원).
 *
 * <p>Gemini 배치 요청 포맷 ({@code batchEmbedContents}) 은 각 request 에 model 경로가
 * 포함되어야 한다 (URL 의 {model} 과 동일):
 * <pre>{
 *   "requests": [
 *     { "model": "models/text-embedding-004", "content": { "parts": [{"text": "..."}] } },
 *     ...
 *   ]
 * }</pre>
 *
 * @since 0.9.0
 */
public class GeminiEmbeddingService implements EmbeddingService {

    private static final Logger log = LogManager.getLogger(GeminiEmbeddingService.class);
    private static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String DEFAULT_MODEL = "text-embedding-004";
    private static final int TIMEOUT_MS = 10_000;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;
    private final String model;
    private final int dimension;

    public GeminiEmbeddingService(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public GeminiEmbeddingService(String apiKey, String model) {
        this.apiKey = (apiKey == null) ? "" : apiKey.trim();
        this.model = (model == null || model.trim().isEmpty()) ? DEFAULT_MODEL : model.trim();
        this.dimension = resolveDimension(this.model);
    }

    private static int resolveDimension(String model) {
        switch (model) {
            case "text-embedding-004": return 768;
            case "embedding-001":      return 768;
            default: return -1;
        }
    }

    @Override
    public float[] embed(String text) throws EmbeddingException {
        if (text == null || text.isEmpty()) {
            throw new EmbeddingException("text must not be empty");
        }
        return embedBatch(Collections.singletonList(text)).get(0);
    }

    @Override
    public EmbeddingResult embedBatch(List<String> texts) throws EmbeddingException {
        if (!isEnabled()) {
            throw new EmbeddingException("Gemini API key not configured");
        }
        if (texts == null || texts.isEmpty()) {
            throw new EmbeddingException("texts must not be empty");
        }

        try {
            ObjectNode body = MAPPER.createObjectNode();
            ArrayNode requests = MAPPER.createArrayNode();
            String modelPath = "models/" + this.model;
            for (String t : texts) {
                if (t == null || t.isEmpty()) {
                    throw new EmbeddingException("batch contains null/empty element");
                }
                ObjectNode req = MAPPER.createObjectNode();
                req.put("model", modelPath);
                ObjectNode content = MAPPER.createObjectNode();
                ArrayNode parts = MAPPER.createArrayNode();
                ObjectNode part = MAPPER.createObjectNode();
                part.put("text", t);
                parts.add(part);
                content.set("parts", parts);
                req.set("content", content);
                requests.add(req);
            }
            body.set("requests", requests);

            String urlStr = GEMINI_API_URL_BASE + this.model + ":batchEmbedContents?key=" + this.apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-goog-api-key", this.apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setUseCaches(false);

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(MAPPER.writeValueAsBytes(body));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String errorBody = readStream(conn.getErrorStream());
                throw new EmbeddingException("Gemini Embeddings HTTP " + code + ": " + errorBody);
            }

            String response = readStream(conn.getInputStream());
            JsonNode json = MAPPER.readTree(response);
            JsonNode arr = json.get("embeddings");
            if (arr == null || !arr.isArray()) {
                throw new EmbeddingException("응답에 embeddings 배열이 없습니다: " + response);
            }
            float[][] vectors = new float[arr.size()][];
            for (int i = 0; i < arr.size(); i++) {
                JsonNode values = arr.get(i).get("values");
                if (values == null || !values.isArray()) {
                    throw new EmbeddingException("embeddings[" + i + "].values 누락");
                }
                float[] vec = new float[values.size()];
                for (int j = 0; j < vec.length; j++) {
                    vec[j] = (float) values.get(j).asDouble();
                }
                vectors[i] = vec;
            }

            // Gemini batchEmbedContents 응답은 토큰 usage 를 제공하지 않음 — 0 으로 기록
            int promptTokens = 0;
            try {
                double cost = CostTable.calculate(this.model, 0, 0);  // 0 토큰 → 0 비용
                CostTracker.INSTANCE.record(new CostEntry(
                        "gemini", this.model, 0, 0, cost, false, "embedding"));
            } catch (Exception te) {
                log.warn("CostTracker 기록 실패 (무시): " + te.getMessage());
            }
            log.debug("Gemini embedding 완료: batch={}, dim={}", vectors.length, vectors[0].length);
            return new EmbeddingResult(vectors, this.model, promptTokens);
        } catch (EmbeddingException ee) {
            throw ee;
        } catch (Exception e) {
            throw new EmbeddingException("Gemini Embeddings 호출 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public String getModel() {
        return model;
    }

    private static String readStream(java.io.InputStream in) throws java.io.IOException {
        if (in == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}

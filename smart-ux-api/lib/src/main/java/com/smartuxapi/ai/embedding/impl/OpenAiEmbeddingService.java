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
import com.smartuxapi.ai.cost.TokenUsageExtractor;
import com.smartuxapi.ai.embedding.EmbeddingException;
import com.smartuxapi.ai.embedding.EmbeddingResult;
import com.smartuxapi.ai.embedding.EmbeddingService;

/**
 * OpenAI Embeddings API 기반 EmbeddingService 구현체.
 *
 * <p>엔드포인트: {@code POST https://api.openai.com/v1/embeddings}.
 * 기본 모델 {@code text-embedding-3-small} (1536 차원).
 *
 * @since 0.9.0
 */
public class OpenAiEmbeddingService implements EmbeddingService {

    private static final Logger log = LogManager.getLogger(OpenAiEmbeddingService.class);
    private static final String OPENAI_EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";
    private static final String DEFAULT_MODEL = "text-embedding-3-small";
    private static final int TIMEOUT_MS = 10_000;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;
    private final String model;
    private final int dimension;

    public OpenAiEmbeddingService(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public OpenAiEmbeddingService(String apiKey, String model) {
        this.apiKey = (apiKey == null) ? "" : apiKey.trim();
        this.model = (model == null || model.trim().isEmpty()) ? DEFAULT_MODEL : model.trim();
        this.dimension = resolveDimension(this.model);
    }

    private static int resolveDimension(String model) {
        switch (model) {
            case "text-embedding-3-small": return 1536;
            case "text-embedding-3-large": return 3072;
            case "text-embedding-ada-002": return 1536;
            default: return -1; // unknown model — dimension 은 실제 호출 후 확인 필요
        }
    }

    @Override
    public float[] embed(String text) throws EmbeddingException {
        if (text == null || text.isEmpty()) {
            throw new EmbeddingException("text must not be empty");
        }
        EmbeddingResult r = embedBatch(Collections.singletonList(text));
        return r.get(0);
    }

    @Override
    public EmbeddingResult embedBatch(List<String> texts) throws EmbeddingException {
        if (!isEnabled()) {
            throw new EmbeddingException("OpenAI API key not configured");
        }
        if (texts == null || texts.isEmpty()) {
            throw new EmbeddingException("texts must not be empty");
        }

        try {
            ObjectNode body = MAPPER.createObjectNode();
            body.put("model", this.model);
            body.put("encoding_format", "float");
            ArrayNode inputArr = MAPPER.createArrayNode();
            for (String t : texts) {
                if (t == null || t.isEmpty()) {
                    throw new EmbeddingException("batch contains null/empty element");
                }
                inputArr.add(t);
            }
            body.set("input", inputArr);

            URL url = new URL(OPENAI_EMBEDDINGS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + this.apiKey);
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
                throw new EmbeddingException("OpenAI Embeddings HTTP " + code + ": " + errorBody);
            }

            String response = readStream(conn.getInputStream());
            JsonNode json = MAPPER.readTree(response);
            JsonNode data = json.get("data");
            if (data == null || !data.isArray()) {
                throw new EmbeddingException("응답에 data 배열이 없습니다: " + response);
            }
            float[][] vectors = new float[data.size()][];
            for (int i = 0; i < data.size(); i++) {
                JsonNode embedding = data.get(i).get("embedding");
                if (embedding == null || !embedding.isArray()) {
                    throw new EmbeddingException("data[" + i + "].embedding 누락");
                }
                float[] vec = new float[embedding.size()];
                for (int j = 0; j < vec.length; j++) {
                    vec[j] = (float) embedding.get(j).asDouble();
                }
                vectors[i] = vec;
            }

            int promptTokens = 0;
            JsonNode usage = json.get("usage");
            if (usage != null) {
                JsonNode pt = usage.get("prompt_tokens");
                if (pt != null) promptTokens = pt.asInt(0);
            }

            // CostTracker 기록 — embedding 은 output 토큰 없음
            try {
                TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(json);
                double cost = CostTable.calculate(this.model, u.inputTokens, u.outputTokens);
                CostTracker.INSTANCE.record(new CostEntry(
                        "openai", this.model, u.inputTokens, u.outputTokens, cost, false, "embedding"));
            } catch (Exception te) {
                log.warn("CostTracker 기록 실패 (무시): " + te.getMessage());
            }

            log.debug("OpenAI embedding 완료: batch={}, dim={}, tokens={}",
                    vectors.length, vectors[0].length, promptTokens);
            return new EmbeddingResult(vectors, this.model, promptTokens);
        } catch (EmbeddingException ee) {
            throw ee;
        } catch (Exception e) {
            throw new EmbeddingException("OpenAI Embeddings 호출 실패: " + e.getMessage(), e);
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

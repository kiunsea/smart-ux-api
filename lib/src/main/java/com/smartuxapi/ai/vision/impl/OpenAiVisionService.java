package com.smartuxapi.ai.vision.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
import com.smartuxapi.ai.vision.ImageScanInfo;
import com.smartuxapi.ai.vision.VisionException;
import com.smartuxapi.ai.vision.VisionService;

/**
 * OpenAI GPT-4 Vision (gpt-4o, gpt-4o-mini) 기반 VisionService 구현체.
 *
 * <p>상태 비유지 — 생성자에 apiKey 와 model 만 받고 필드는 불변.
 *
 * @since 0.7.0
 */
public class OpenAiVisionService implements VisionService {

    private static final Logger log = LogManager.getLogger(OpenAiVisionService.class);
    private static final String OPENAI_VISION_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final int DEFAULT_MAX_TOKENS = 300;
    private static final int TIMEOUT_MS = 10_000;
    private static final String PROMPT =
            "이미지에 있는 모든 텍스트를 추출하세요. 텍스트만 반환하고 설명은 포함하지 마세요. "
                    + "텍스트가 없으면 빈 문자열을 반환하세요.";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;
    private final String model;

    public OpenAiVisionService(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public OpenAiVisionService(String apiKey, String model) {
        this.apiKey = (apiKey == null) ? "" : apiKey.trim();
        this.model = (model == null || model.trim().isEmpty()) ? DEFAULT_MODEL : model.trim();
    }

    @Override
    public String extractText(String imageUrl) throws VisionException {
        if (!isEnabled()) {
            throw new VisionException("OpenAI API key not configured (pass via constructor)");
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new VisionException("imageUrl must not be empty");
        }

        try {
            ObjectNode requestBody = MAPPER.createObjectNode();
            requestBody.put("model", this.model);
            requestBody.put("max_tokens", DEFAULT_MAX_TOKENS);

            ArrayNode messages = MAPPER.createArrayNode();
            ObjectNode message = MAPPER.createObjectNode();
            message.put("role", "user");

            ArrayNode content = MAPPER.createArrayNode();
            ObjectNode textContent = MAPPER.createObjectNode();
            textContent.put("type", "text");
            textContent.put("text", PROMPT);
            content.add(textContent);

            ObjectNode imageContent = MAPPER.createObjectNode();
            imageContent.put("type", "image_url");
            ObjectNode imageUrlObj = MAPPER.createObjectNode();
            imageUrlObj.put("url", imageUrl);
            imageContent.set("image_url", imageUrlObj);
            content.add(imageContent);

            message.set("content", content);
            messages.add(message);
            requestBody.set("messages", messages);

            URL url = new URL(OPENAI_VISION_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Authorization", "Bearer " + this.apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setUseCaches(false);

            try (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                os.write(MAPPER.writeValueAsBytes(requestBody));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readStream(conn.getInputStream());
                JsonNode json = MAPPER.readTree(response);
                recordCost(json);
                JsonNode choices = json.get("choices");
                if (choices == null || !choices.isArray() || choices.size() == 0) {
                    throw new VisionException("OpenAI Vision 응답에 choices 가 없습니다: " + response);
                }
                JsonNode first = choices.get(0);
                JsonNode messageNode = first.get("message");
                if (messageNode == null) {
                    throw new VisionException("OpenAI Vision 응답에 message 가 없습니다");
                }
                JsonNode contentNode = messageNode.get("content");
                String text = (contentNode == null) ? "" : contentNode.asText("").trim();
                log.debug("OpenAI Vision 텍스트 추출 완료: {} chars", text.length());
                return text;
            } else {
                String errorBody = readStream(conn.getErrorStream());
                throw new VisionException("OpenAI Vision HTTP " + responseCode + ": " + errorBody);
            }
        } catch (VisionException ve) {
            throw ve;
        } catch (Exception e) {
            throw new VisionException("OpenAI Vision 호출 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractTextFromBase64(String base64Image) throws VisionException {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new VisionException("base64Image must not be empty");
        }
        return extractText("data:image/jpeg;base64," + base64Image);
    }

    @Override
    public ImageScanInfo scanImage(String imageUrl) throws VisionException {
        String text = extractText(imageUrl);
        // OpenAI API 는 confidence 를 직접 제공하지 않음 — 텍스트 존재 여부로 간이 신뢰도 부여
        double confidence = text.isEmpty() ? 0.0 : 0.95;
        return new ImageScanInfo(imageUrl, text, confidence, this.model);
    }

    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }

    private void recordCost(JsonNode responseJson) {
        try {
            TokenUsageExtractor.Usage u = TokenUsageExtractor.fromOpenAi(responseJson);
            double cost = CostTable.calculate(this.model, u.inputTokens, u.outputTokens);
            CostTracker.INSTANCE.record(new CostEntry(
                    "openai", this.model, u.inputTokens, u.outputTokens, cost, false, "vision"));
        } catch (Exception e) {
            log.warn("CostTracker 기록 실패 (무시): " + e.getMessage());
        }
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

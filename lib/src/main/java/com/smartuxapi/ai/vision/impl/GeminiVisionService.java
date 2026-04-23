package com.smartuxapi.ai.vision.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartuxapi.ai.cost.CostEntry;
import com.smartuxapi.ai.cost.CostTable;
import com.smartuxapi.ai.cost.CostTracker;
import com.smartuxapi.ai.cost.FallbackContext;
import com.smartuxapi.ai.cost.TokenUsageExtractor;
import com.smartuxapi.ai.vision.ImageScanInfo;
import com.smartuxapi.ai.vision.VisionException;
import com.smartuxapi.ai.vision.VisionService;

/**
 * Google Gemini 기반 VisionService 구현체.
 *
 * <p>Gemini 는 OpenAI 와 달리 image URL 직접 전달을 지원하지 않고
 * inline base64 (또는 별도 Files API 업로드) 만 허용한다. 본 구현체는
 * HTTPS URL 을 받으면 내부에서 바이트 다운로드 후 base64 로 인코딩하여
 * {@code inlineData} 파트로 전송한다.
 *
 * <p>data URI ({@code data:image/...;base64,...}) 는 별도 다운로드 없이 페이로드를 직접 사용.
 *
 * <p>상태 비유지 — 생성자에 apiKey 와 model 만 받고 필드는 불변.
 *
 * @since 0.8.1
 */
public class GeminiVisionService implements VisionService {

    private static final Logger log = LogManager.getLogger(GeminiVisionService.class);
    private static final String GEMINI_API_URL_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final String DEFAULT_MODEL = "gemini-1.5-flash";
    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_DOWNLOAD_BYTES = 20 * 1024 * 1024; // 20MB
    private static final String PROMPT =
            "이미지에 있는 모든 텍스트를 추출하세요. 텍스트만 반환하고 설명은 포함하지 마세요. "
                    + "텍스트가 없으면 빈 문자열을 반환하세요.";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiKey;
    private final String model;

    public GeminiVisionService(String apiKey) {
        this(apiKey, DEFAULT_MODEL);
    }

    public GeminiVisionService(String apiKey, String model) {
        this.apiKey = (apiKey == null) ? "" : apiKey.trim();
        this.model = (model == null || model.trim().isEmpty()) ? DEFAULT_MODEL : model.trim();
    }

    @Override
    public String extractText(String imageUrl) throws VisionException {
        if (!isEnabled()) {
            throw new VisionException("Gemini API key not configured (pass via constructor)");
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new VisionException("imageUrl must not be empty");
        }

        String mimeType;
        String base64Data;
        if (imageUrl.startsWith("data:")) {
            // data URI 파싱: data:image/png;base64,AAAA...
            int comma = imageUrl.indexOf(',');
            if (comma < 0) {
                throw new VisionException("Invalid data URI: comma separator missing");
            }
            String header = imageUrl.substring(5, comma); // "image/png;base64"
            base64Data = imageUrl.substring(comma + 1);
            mimeType = header.split(";")[0];
            if (mimeType.isEmpty()) mimeType = "image/jpeg";
        } else {
            // HTTP(S) URL — 다운로드 후 base64 인코딩
            byte[] bytes = downloadImage(imageUrl);
            base64Data = Base64.getEncoder().encodeToString(bytes);
            mimeType = guessMimeTypeFromUrl(imageUrl);
        }

        return callGeminiVision(base64Data, mimeType);
    }

    @Override
    public String extractTextFromBase64(String base64Image) throws VisionException {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new VisionException("base64Image must not be empty");
        }
        if (!isEnabled()) {
            throw new VisionException("Gemini API key not configured");
        }
        return callGeminiVision(base64Image, "image/jpeg");
    }

    @Override
    public ImageScanInfo scanImage(String imageUrl) throws VisionException {
        String text = extractText(imageUrl);
        double confidence = text.isEmpty() ? 0.0 : 0.9;
        return new ImageScanInfo(imageUrl, text, confidence, this.model);
    }

    @Override
    public boolean isEnabled() {
        return apiKey != null && !apiKey.isEmpty();
    }

    // ---------- 내부 ----------

    private String callGeminiVision(String base64Data, String mimeType) throws VisionException {
        try {
            ObjectNode requestBody = MAPPER.createObjectNode();
            ArrayNode contents = MAPPER.createArrayNode();
            ObjectNode content = MAPPER.createObjectNode();
            ArrayNode parts = MAPPER.createArrayNode();

            ObjectNode textPart = MAPPER.createObjectNode();
            textPart.put("text", PROMPT);
            parts.add(textPart);

            ObjectNode imagePart = MAPPER.createObjectNode();
            ObjectNode inlineData = MAPPER.createObjectNode();
            inlineData.put("mimeType", mimeType);
            inlineData.put("data", base64Data);
            imagePart.set("inlineData", inlineData);
            parts.add(imagePart);

            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);

            String urlStr = GEMINI_API_URL_BASE + this.model + ":generateContent?key=" + this.apiKey;
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
                os.write(MAPPER.writeValueAsBytes(requestBody));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String errorBody = readStream(conn.getErrorStream());
                throw new VisionException("Gemini Vision HTTP " + code + ": " + errorBody);
            }

            String response = readStream(conn.getInputStream());
            JsonNode json = MAPPER.readTree(response);
            recordCost(json);
            JsonNode candidates = json.get("candidates");
            if (candidates == null || !candidates.isArray() || candidates.size() == 0) {
                throw new VisionException("Gemini Vision 응답에 candidates 가 없습니다: " + response);
            }
            JsonNode candidate0 = candidates.get(0);
            JsonNode contentNode = candidate0.get("content");
            if (contentNode == null) {
                throw new VisionException("Gemini Vision 응답에 content 가 없습니다");
            }
            JsonNode partsNode = contentNode.get("parts");
            if (partsNode == null || !partsNode.isArray() || partsNode.size() == 0) {
                return "";
            }
            StringBuilder buf = new StringBuilder();
            for (JsonNode p : partsNode) {
                JsonNode t = p.get("text");
                if (t != null) buf.append(t.asText());
            }
            String text = buf.toString().trim();
            log.debug("Gemini Vision 텍스트 추출 완료: {} chars", text.length());
            return text;
        } catch (VisionException ve) {
            throw ve;
        } catch (Exception e) {
            throw new VisionException("Gemini Vision 호출 실패: " + e.getMessage(), e);
        }
    }

    private byte[] downloadImage(String imageUrl) throws VisionException {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);
            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new VisionException("이미지 다운로드 실패 (HTTP " + code + "): " + imageUrl);
            }
            try (InputStream in = conn.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int read;
                int total = 0;
                while ((read = in.read(buf)) > 0) {
                    total += read;
                    if (total > MAX_DOWNLOAD_BYTES) {
                        throw new VisionException("이미지 크기 상한 초과 ("
                                + MAX_DOWNLOAD_BYTES + " bytes)");
                    }
                    out.write(buf, 0, read);
                }
                return out.toByteArray();
            }
        } catch (VisionException ve) {
            throw ve;
        } catch (Exception e) {
            throw new VisionException("이미지 다운로드 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 확장자 기반 MIME 추정. 실패 시 {@code image/jpeg} 기본값.
     */
    static String guessMimeTypeFromUrl(String imageUrl) {
        String lower = imageUrl.toLowerCase();
        int q = lower.indexOf('?');
        if (q > 0) lower = lower.substring(0, q);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".heic") || lower.endsWith(".heif")) return "image/heic";
        return "image/jpeg";
    }

    private void recordCost(JsonNode responseJson) {
        try {
            TokenUsageExtractor.Usage u = TokenUsageExtractor.fromGemini(responseJson);
            double cost = CostTable.calculate(this.model, u.inputTokens, u.outputTokens);
            CostTracker.INSTANCE.record(new CostEntry(
                    "gemini", this.model, u.inputTokens, u.outputTokens, cost,
                    FallbackContext.isFallback(), "vision"));
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

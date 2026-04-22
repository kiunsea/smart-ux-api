package com.smartuxapi.ai.vision;

import java.time.Instant;
import java.util.Objects;

import org.json.simple.JSONObject;

/**
 * 이미지 스캔 결과를 담는 불변 데이터 객체.
 *
 * <p>JSON 키는 설계 스케치 §6.3 에 따라 고정 (T2-b Tool Use 와의 정합):
 * <ul>
 *   <li>{@code imageUrl}</li>
 *   <li>{@code extractedText}</li>
 *   <li>{@code confidence}</li>
 *   <li>{@code timestamp}</li>
 *   <li>{@code modelUsed}</li>
 * </ul>
 *
 * @since 0.7.0
 */
public final class ImageScanInfo {

    private final String imageUrl;
    private final String extractedText;
    private final double confidence;
    private final String timestamp;
    private final String modelUsed;

    /**
     * 편의 생성자 — confidence 1.0, modelUsed null.
     */
    public ImageScanInfo(String imageUrl, String extractedText) {
        this(imageUrl, extractedText, 1.0, null);
    }

    /**
     * 전체 필드 생성자.
     *
     * @param imageUrl 이미지 URL 또는 data URI (필수)
     * @param extractedText 추출 텍스트 (null → 빈 문자열)
     * @param confidence 신뢰도 0.0~1.0 (범위 밖은 clamp)
     * @param modelUsed 사용된 모델 식별자 (선택)
     */
    public ImageScanInfo(String imageUrl, String extractedText, double confidence, String modelUsed) {
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl must not be null");
        this.extractedText = (extractedText == null) ? "" : extractedText;
        this.confidence = clamp(confidence);
        this.timestamp = Instant.now().toString();
        this.modelUsed = modelUsed;
    }

    public String getImageUrl()      { return imageUrl; }
    public String getExtractedText() { return extractedText; }
    public double getConfidence()    { return confidence; }
    public String getTimestamp()     { return timestamp; }
    public String getModelUsed()     { return modelUsed; }

    /**
     * JSON 객체로 직렬화. 키 이름은 본 클래스 Javadoc 에 고정됨.
     */
    @SuppressWarnings("unchecked")
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("imageUrl", imageUrl);
        json.put("extractedText", extractedText);
        json.put("confidence", confidence);
        json.put("timestamp", timestamp);
        json.put("modelUsed", modelUsed);
        return json;
    }

    @Override
    public String toString() {
        return String.format("ImageScanInfo{url='%s', textLen=%d, confidence=%.2f, model='%s'}",
                imageUrl, extractedText.length(), confidence, modelUsed);
    }

    private static double clamp(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}

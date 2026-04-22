package com.smartuxapi.ai.vision;

/**
 * Vision API 서비스 인터페이스 — 이미지에서 텍스트를 추출한다.
 *
 * <p>구현체는 <b>상태 비유지(stateless)</b> 여야 한다 (설계 스케치 §11.4).
 * 이는 향후 Tool Use 핸들러에서 동일 인스턴스가 여러 번 호출되어도 안전하도록 하기 위함이다.
 *
 * <p>시그니처는 {@link #scanImage(String)} / {@link #extractText(String)} /
 * {@link #extractTextFromBase64(String)} 세 가지로 고정되며 오버로드/옵션 파라미터 추가 금지
 * (설계 스케치 §11.3).
 *
 * @since 0.7.0
 */
public interface VisionService {

    /**
     * 이미지 URL 에서 텍스트 추출 — 텍스트만 반환.
     *
     * @param imageUrl http/https URL 또는 data URI
     * @return 추출 텍스트 (텍스트 없으면 빈 문자열)
     * @throws VisionException 네트워크/파싱/API 실패 시
     */
    String extractText(String imageUrl) throws VisionException;

    /**
     * Base64 인코딩 이미지에서 텍스트 추출.
     *
     * @param base64Image Base64 문자열 (MIME prefix 없음)
     * @return 추출 텍스트 (없으면 빈 문자열)
     * @throws VisionException 실패 시
     */
    String extractTextFromBase64(String base64Image) throws VisionException;

    /**
     * 이미지 URL 을 스캔하여 {@link ImageScanInfo} 로 포장하여 반환.
     *
     * <p>실제 API 호출은 {@link #extractText(String)} 에 위임하고, 본 메서드는
     * confidence/modelUsed 등 메타데이터를 함께 채워 반환한다.
     *
     * @param imageUrl 이미지 URL
     * @return 스캔 결과
     * @throws VisionException 실패 시
     */
    ImageScanInfo scanImage(String imageUrl) throws VisionException;

    /**
     * 서비스 활성화 여부 — API 키가 설정되어 있으면 true.
     */
    boolean isEnabled();
}

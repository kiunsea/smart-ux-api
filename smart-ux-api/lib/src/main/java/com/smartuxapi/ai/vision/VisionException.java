package com.smartuxapi.ai.vision;

/**
 * Vision API 호출/파싱 중 발생하는 예외.
 *
 * <p>{@code Exception} 을 직접 상속하여 <b>checked</b> 로 노출된다.
 * 이는 {@link com.smartuxapi.ai.cache.CacheStrategy} 및 향후 Tool Use 호환성 요구사항
 * (설계 스케치 §11.5)을 만족하기 위한 선택이다 — Tool handler 람다가 명시적으로 처리할 수 있도록.
 *
 * @since 0.7.0
 */
public class VisionException extends Exception {

    public VisionException(String message) {
        super(message);
    }

    public VisionException(String message, Throwable cause) {
        super(message, cause);
    }
}

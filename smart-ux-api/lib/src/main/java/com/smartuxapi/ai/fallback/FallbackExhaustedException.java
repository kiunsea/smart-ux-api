package com.smartuxapi.ai.fallback;

/**
 * Fallback chain 의 모든 provider 가 실패했을 때 마지막 예외를 감싸 전달.
 *
 * <p>{@link #getCause()} 는 마지막 slot 의 예외. 앞선 slot 의 실패는 suppressed 로 포함.
 *
 * @since 0.9.1
 */
public class FallbackExhaustedException extends Exception {

    private static final long serialVersionUID = 1L;

    public FallbackExhaustedException(String message, Throwable lastError) {
        super(message, lastError);
    }
}

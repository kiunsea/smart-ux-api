package com.smartuxapi.ai.fallback;

/**
 * API 호출 실패 분류. {@link FallbackPolicy#getTriggerReasons()} 에 포함된 reason 만
 * 다음 provider 로 fallback 된다.
 *
 * @since 0.9.1
 */
public enum FailureReason {
    /** 요청 타임아웃. */
    TIMEOUT,
    /** HTTP 429 — rate limit. */
    RATE_LIMIT,
    /** HTTP 5xx — provider 측 서버 오류. */
    SERVER_ERROR,
    /** 네트워크/IO 오류 — connection reset, DNS 실패 등. */
    NETWORK_ERROR,
    /** HTTP 401/403 — 기본적으로 fallback 하지 않음 (설정 오류 가능성). */
    UNAUTHORIZED,
    /** 분류 불가 — 기본적으로 fallback 하지 않음. */
    UNKNOWN;

    /**
     * 예외 메시지 / 타입에서 reason 추론.
     */
    public static FailureReason classify(Throwable t) {
        if (t == null) return UNKNOWN;
        String msg = t.getMessage() == null ? "" : t.getMessage();
        String typeName = t.getClass().getSimpleName();

        if (t instanceof java.net.SocketTimeoutException
                || typeName.contains("Timeout")
                || msg.toLowerCase().contains("timeout")) {
            return TIMEOUT;
        }
        if (t instanceof java.net.UnknownHostException
                || t instanceof java.net.ConnectException
                || t instanceof java.io.IOException) {
            // IOException 은 기본 네트워크 오류. 단, 메시지에서 HTTP 코드를 찾으면 세분화.
            if (msg.contains(" 429") || msg.contains("rate") || msg.contains("Rate")) return RATE_LIMIT;
            if (msg.contains(" 500") || msg.contains(" 502") || msg.contains(" 503") || msg.contains(" 504")) return SERVER_ERROR;
            if (msg.contains(" 401") || msg.contains(" 403")) return UNAUTHORIZED;
            return NETWORK_ERROR;
        }

        // 일반 Exception 의 메시지에 HTTP 코드가 있는 경우
        if (msg.contains(" 429") || msg.contains("rate") || msg.contains("Rate")) return RATE_LIMIT;
        if (msg.contains(" 500") || msg.contains(" 502") || msg.contains(" 503") || msg.contains(" 504")) return SERVER_ERROR;
        if (msg.contains(" 401") || msg.contains(" 403")) return UNAUTHORIZED;

        return UNKNOWN;
    }
}

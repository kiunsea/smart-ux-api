package com.smartuxapi.ai.fallback;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FailureReason.classify 단위 테스트")
class FailureReasonTest {

    @Test
    @DisplayName("SocketTimeoutException → TIMEOUT")
    void testTimeout() {
        assertEquals(FailureReason.TIMEOUT,
                FailureReason.classify(new SocketTimeoutException("timed out")));
    }

    @Test
    @DisplayName("메시지에 'timeout' 포함 → TIMEOUT")
    void testMessageTimeout() {
        assertEquals(FailureReason.TIMEOUT,
                FailureReason.classify(new RuntimeException("read timeout")));
    }

    @Test
    @DisplayName("UnknownHostException → NETWORK_ERROR")
    void testNetworkUnknownHost() {
        assertEquals(FailureReason.NETWORK_ERROR,
                FailureReason.classify(new UnknownHostException("api.example.com")));
    }

    @Test
    @DisplayName("ConnectException → NETWORK_ERROR")
    void testConnectException() {
        assertEquals(FailureReason.NETWORK_ERROR,
                FailureReason.classify(new ConnectException("refused")));
    }

    @Test
    @DisplayName("메시지에 ' 429' → RATE_LIMIT")
    void testRateLimit() {
        assertEquals(FailureReason.RATE_LIMIT,
                FailureReason.classify(new IOException("API 호출 실패 (tool use): 429 - too many requests")));
    }

    @Test
    @DisplayName("메시지에 ' 500' / ' 503' → SERVER_ERROR")
    void testServerError() {
        assertEquals(FailureReason.SERVER_ERROR,
                FailureReason.classify(new IOException("OpenAI API 호출 실패: 500 - Internal Server Error")));
        assertEquals(FailureReason.SERVER_ERROR,
                FailureReason.classify(new IOException("Gemini API 호출 실패: 503 - Service Unavailable")));
    }

    @Test
    @DisplayName("메시지에 ' 401' → UNAUTHORIZED")
    void testUnauthorized() {
        assertEquals(FailureReason.UNAUTHORIZED,
                FailureReason.classify(new RuntimeException("HTTP 401 Unauthorized")));
    }

    @Test
    @DisplayName("null 또는 분류 불가 → UNKNOWN")
    void testUnknown() {
        assertEquals(FailureReason.UNKNOWN, FailureReason.classify(null));
        assertEquals(FailureReason.UNKNOWN,
                FailureReason.classify(new RuntimeException("some custom error")));
    }
}

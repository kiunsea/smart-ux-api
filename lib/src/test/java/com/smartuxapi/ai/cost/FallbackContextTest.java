package com.smartuxapi.ai.cost;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FallbackContext 단위 테스트")
class FallbackContextTest {

    @AfterEach
    void cleanup() {
        // 다른 테스트 오염 방지
        FallbackContext.exit();
    }

    @Test
    @DisplayName("초기값은 false")
    void testDefault() {
        assertFalse(FallbackContext.isFallback());
    }

    @Test
    @DisplayName("enterFallback 후 true, exit 후 다시 false")
    void testEnterExit() {
        assertFalse(FallbackContext.isFallback());
        FallbackContext.enterFallback();
        assertTrue(FallbackContext.isFallback());
        FallbackContext.exit();
        assertFalse(FallbackContext.isFallback());
    }

    @Test
    @DisplayName("enterFallback 중복 호출 허용 (idempotent)")
    void testReentrant() {
        FallbackContext.enterFallback();
        FallbackContext.enterFallback();
        assertTrue(FallbackContext.isFallback());
        FallbackContext.exit();
        assertFalse(FallbackContext.isFallback(), "한 번의 exit 로 해제");
    }

    @Test
    @DisplayName("Thread 독립성 — 다른 스레드는 영향받지 않음")
    void testThreadIsolation() throws Exception {
        FallbackContext.enterFallback();
        assertTrue(FallbackContext.isFallback());

        final Boolean[] otherThreadValue = new Boolean[1];
        Thread t = new Thread(() -> otherThreadValue[0] = FallbackContext.isFallback());
        t.start();
        t.join();

        assertFalse(otherThreadValue[0], "다른 스레드에서는 false");
        assertTrue(FallbackContext.isFallback(), "원래 스레드는 여전히 true");
    }
}

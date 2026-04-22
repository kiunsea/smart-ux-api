package com.smartuxapi.ai.cache;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NoOpCacheStrategy 단위 테스트")
class NoOpCacheStrategyTest {

    @Test
    @DisplayName("싱글턴 인스턴스가 제공된다")
    void testSingleton() {
        assertSame(NoOpCacheStrategy.INSTANCE, NoOpCacheStrategy.INSTANCE);
    }

    @Test
    @DisplayName("prime/invalidate/recordMetricsFromResponse 는 예외 없이 동작")
    void testNoOpOperations() throws Exception {
        CacheStrategy s = NoOpCacheStrategy.INSTANCE;
        assertDoesNotThrow(() -> s.prime(CacheHint.of("x")));
        assertDoesNotThrow(() -> s.prime(null));
        assertDoesNotThrow(() -> s.recordMetricsFromResponse(null));
        assertDoesNotThrow(s::invalidate);
    }

    @Test
    @DisplayName("getLastMetrics 는 EMPTY, provider=none")
    void testDefaults() {
        CacheStrategy s = NoOpCacheStrategy.INSTANCE;
        assertSame(CacheMetrics.EMPTY, s.getLastMetrics());
        assertNull(s.getCurrentHint());
        assertEquals("none", s.getProvider());
    }
}

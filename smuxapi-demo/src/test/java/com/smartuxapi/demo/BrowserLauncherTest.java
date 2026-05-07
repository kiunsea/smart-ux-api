package com.smartuxapi.demo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link BrowserLauncher#shouldSkipBrowser(String[])} 단위 테스트.
 *
 * <p>주의:
 * <ul>
 *   <li>환경변수 {@code SMUXAPI_NO_BROWSER} 는 OS 레벨에서만 설정 가능하므로 본 테스트에서는 직접 검증하지 않는다.</li>
 *   <li>실행 환경 (로컬 Windows / Linux CI / SSH headless 등) 에 따라 Desktop / headless 분기 결과가 달라지므로
 *       강제로 {@code java.awt.headless} 를 끄지 않는다. 환경별 분기 결과를 그대로 받아들이고,
 *       NPE / 초기화 오류 없이 boolean 이 반환되는지를 일관 검증한다.</li>
 * </ul>
 */
class BrowserLauncherTest {

    private String prevHeadless;

    @BeforeEach
    void saveProps() {
        // 원래 값만 보존. 강제 변경하지 않음 — CI (headless) 와 로컬 (GUI) 차이를 그대로 테스트.
        prevHeadless = System.getProperty("java.awt.headless");
    }

    @AfterEach
    void restoreProps() {
        if (prevHeadless == null) {
            System.clearProperty("java.awt.headless");
        } else {
            System.setProperty("java.awt.headless", prevHeadless);
        }
    }

    @Test
    void skipsWhenNoBrowserArgFlagPresent() {
        // args 분기는 환경 무관하게 항상 true 반환되어야 함
        assertTrue(BrowserLauncher.shouldSkipBrowser(new String[] {"--smuxapi.no-browser"}));
    }

    @Test
    void skipsWhenNoBrowserArgEqualsTrue() {
        assertTrue(BrowserLauncher.shouldSkipBrowser(new String[] {"--smuxapi.no-browser=true"}));
    }

    @Test
    void doesNotThrowWhenNoBrowserArgEqualsFalse() {
        // args 분기에서 skip 결정이 안 되어 환경 분기로 위임되는 경로 — 호출 자체가 NPE/Error 없이 끝나는지만 검증
        assertDoesNotThrow(
                () -> BrowserLauncher.shouldSkipBrowser(new String[] {"--smuxapi.no-browser=false"}));
    }

    @Test
    void skipsWhenHeadlessTrue() {
        // 헤드리스 강제 시 Desktop 분기까지 가지 않고 즉시 true 반환
        String prev = System.getProperty("java.awt.headless");
        System.setProperty("java.awt.headless", "true");
        try {
            assertTrue(BrowserLauncher.shouldSkipBrowser(new String[0]));
        } finally {
            if (prev == null) {
                System.clearProperty("java.awt.headless");
            } else {
                System.setProperty("java.awt.headless", prev);
            }
        }
    }

    @Test
    void handlesNullArgsSafely() {
        // null args 가 와도 환경 분기 도달 시 NPE/Error 없이 boolean 반환
        assertDoesNotThrow(() -> BrowserLauncher.shouldSkipBrowser(null));
    }

    @Test
    void doesNotThrowWithUnrelatedArgs() {
        // 무관한 인자는 args 분기에서 skip 을 유도하지 않음 — 호출이 NPE/Error 없이 끝나는지만 검증
        assertDoesNotThrow(
                () -> BrowserLauncher.shouldSkipBrowser(new String[] {"--server.port=9090", "--debug"}));
    }
}

package com.smartuxapi.ai.vision;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VisionException 단위 테스트")
class VisionExceptionTest {

    @Test
    @DisplayName("Exception 을 상속하여 checked 로 노출된다 (RuntimeException 아님)")
    void testIsChecked() {
        // 클래스 레벨 검증 — instanceof 는 컴파일 타임에 불가 (상속 관계 상 자명)
        assertTrue(Exception.class.isAssignableFrom(VisionException.class));
        assertFalse(RuntimeException.class.isAssignableFrom(VisionException.class),
                "VisionException 은 checked 여야 하며 RuntimeException 하위 금지");
    }

    @Test
    @DisplayName("cause 포함 생성자")
    void testWithCause() {
        Throwable cause = new IllegalStateException("inner");
        VisionException e = new VisionException("outer", cause);
        assertEquals("outer", e.getMessage());
        assertSame(cause, e.getCause());
    }
}

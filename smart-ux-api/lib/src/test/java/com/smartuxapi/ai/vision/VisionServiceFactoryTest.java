package com.smartuxapi.ai.vision;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VisionServiceFactory 단위 테스트")
class VisionServiceFactoryTest {

    @Test
    @DisplayName("createOpenAI(apiKey) — 유효 키로 isEnabled=true")
    void testCreateOpenAIWithKey() {
        VisionService s = VisionServiceFactory.createOpenAI("sk-fake-key");
        assertNotNull(s);
        assertTrue(s.isEnabled());
    }

    @Test
    @DisplayName("createOpenAI(null) — 비어있는 키로 isEnabled=false")
    void testCreateOpenAIWithNullKey() {
        VisionService s = VisionServiceFactory.createOpenAI(null);
        assertNotNull(s);
        assertFalse(s.isEnabled());
    }

    @Test
    @DisplayName("createOpenAI(apiKey, model) — 두 번째 파라미터도 수용")
    void testCreateOpenAIWithModel() {
        VisionService s = VisionServiceFactory.createOpenAI("sk-fake", "gpt-4o");
        assertNotNull(s);
        assertTrue(s.isEnabled());
    }

    @Test
    @DisplayName("createOpenAIFromEnv — 환경변수/시스템프로퍼티 모두 없으면 비활성")
    void testCreateOpenAIFromEnvNoKey() {
        String prev = System.getProperty("openai.api.key");
        try {
            System.clearProperty("openai.api.key");
            VisionService s = VisionServiceFactory.createOpenAIFromEnv();
            assertNotNull(s);
            if (System.getenv("OPENAI_API_KEY") == null || System.getenv("OPENAI_API_KEY").isEmpty()) {
                assertFalse(s.isEnabled(), "키가 없으면 비활성 상태여야 한다");
            }
        } finally {
            if (prev != null) System.setProperty("openai.api.key", prev);
        }
    }
}

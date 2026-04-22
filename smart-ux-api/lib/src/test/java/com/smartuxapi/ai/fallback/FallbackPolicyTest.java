package com.smartuxapi.ai.fallback;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.ChatRoom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("FallbackPolicy / FallbackPolicies 단위 테스트")
class FallbackPolicyTest {

    @Test
    @DisplayName("defaultReasons — 4종 (TIMEOUT/RATE_LIMIT/SERVER_ERROR/NETWORK_ERROR)")
    void testDefaultReasons() {
        java.util.Set<FailureReason> reasons = FallbackPolicy.defaultReasons();
        assertEquals(4, reasons.size());
        assertTrue(reasons.contains(FailureReason.TIMEOUT));
        assertTrue(reasons.contains(FailureReason.RATE_LIMIT));
        assertTrue(reasons.contains(FailureReason.SERVER_ERROR));
        assertTrue(reasons.contains(FailureReason.NETWORK_ERROR));
        assertFalse(reasons.contains(FailureReason.UNAUTHORIZED), "UNAUTHORIZED 기본 제외");
    }

    @Test
    @DisplayName("Builder — 최소 구성")
    void testBuilderMinimal() {
        ChatRoom cr = mock(ChatRoom.class);
        FallbackPolicy p = FallbackPolicy.builder()
                .addSlot(new OpenAiSlot(cr))
                .build();
        assertEquals(1, p.getChain().size());
        assertEquals(FallbackPolicy.defaultReasons(), p.getTriggerReasons());
        assertTrue(p.isLogOnFallback());
        assertEquals(0, p.getPrimaryTimeoutMs());
    }

    @Test
    @DisplayName("Builder — 모든 옵션")
    void testBuilderFull() {
        ChatRoom openai = mock(ChatRoom.class);
        ChatRoom gemini = mock(ChatRoom.class);
        FallbackPolicy p = FallbackPolicy.builder()
                .addSlots(new OpenAiSlot(openai), new GeminiSlot(gemini))
                .triggerReasons(FailureReason.RATE_LIMIT, FailureReason.SERVER_ERROR)
                .timeoutMs(30_000)
                .logOnFallback(false)
                .build();
        assertEquals(2, p.getChain().size());
        assertEquals(2, p.getTriggerReasons().size());
        assertEquals(30_000, p.getPrimaryTimeoutMs());
        assertFalse(p.isLogOnFallback());
    }

    @Test
    @DisplayName("빈 chain → IllegalArgumentException")
    void testEmptyChain() {
        assertThrows(IllegalArgumentException.class,
                () -> FallbackPolicy.builder().build());
    }

    @Test
    @DisplayName("FallbackPolicies 프리셋 — openai primary → gemini")
    void testOpenAiPrimaryPreset() {
        ChatRoom openai = mock(ChatRoom.class);
        ChatRoom gemini = mock(ChatRoom.class);
        FallbackPolicy p = FallbackPolicies.openaiPrimaryGeminiFallback(openai, gemini);
        assertEquals(2, p.getChain().size());
        assertEquals("openai", p.getChain().get(0).getName());
        assertEquals("gemini", p.getChain().get(1).getName());
    }

    @Test
    @DisplayName("FallbackPolicies 프리셋 — gemini primary → openai")
    void testGeminiPrimaryPreset() {
        ChatRoom openai = mock(ChatRoom.class);
        ChatRoom gemini = mock(ChatRoom.class);
        FallbackPolicy p = FallbackPolicies.geminiPrimaryOpenaiFallback(gemini, openai);
        assertEquals("gemini", p.getChain().get(0).getName());
        assertEquals("openai", p.getChain().get(1).getName());
    }

    @Test
    @DisplayName("Slot — null chatRoom 은 IllegalArgumentException")
    void testSlotNullChatRoom() {
        assertThrows(IllegalArgumentException.class, () -> new OpenAiSlot(null));
        assertThrows(IllegalArgumentException.class, () -> new GeminiSlot(null));
    }
}

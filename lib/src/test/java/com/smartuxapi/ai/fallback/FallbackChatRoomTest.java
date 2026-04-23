package com.smartuxapi.ai.fallback;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cost.FallbackContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("FallbackChatRoom 통합 시나리오 테스트")
class FallbackChatRoomTest {

    /** ChatRoom + Chatting 페어 생성 유틸. */
    private static Pair setupChatRoom() {
        ChatRoom room = mock(ChatRoom.class);
        Chatting chatting = mock(Chatting.class);
        when(room.getChatting()).thenReturn(chatting);
        return new Pair(room, chatting);
    }

    private static final class Pair {
        final ChatRoom room; final Chatting chatting;
        Pair(ChatRoom r, Chatting c) { this.room = r; this.chatting = c; }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject okResponse(String text) {
        JSONObject o = new JSONObject();
        o.put("message", text);
        return o;
    }

    @Test
    @DisplayName("primary 성공 — fallback 미발동")
    void testPrimarySucceeds() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi")).thenReturn(okResponse("from-openai"));

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        JSONObject res = fc.getChatting().sendPrompt("hi");

        assertEquals("from-openai", res.get("message"));
        verify(openai.chatting, times(1)).sendPrompt("hi");
        verify(gemini.chatting, times(0)).sendPrompt(anyString());
    }

    @Test
    @DisplayName("primary 5xx → Gemini fallback 성공")
    void testPrimary5xxFallback() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi"))
                .thenThrow(new IOException("OpenAI API 호출 실패: 500 - Internal Server Error"));
        when(gemini.chatting.sendPrompt("hi")).thenReturn(okResponse("from-gemini"));

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        JSONObject res = fc.getChatting().sendPrompt("hi");

        assertEquals("from-gemini", res.get("message"));
        verify(openai.chatting, times(1)).sendPrompt("hi");
        verify(gemini.chatting, times(1)).sendPrompt("hi");
    }

    @Test
    @DisplayName("primary 429 → Gemini fallback 성공")
    void testPrimaryRateLimitFallback() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi"))
                .thenThrow(new IOException("OpenAI API 호출 실패: 429 - rate limit"));
        when(gemini.chatting.sendPrompt("hi")).thenReturn(okResponse("recovered"));

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        JSONObject res = fc.getChatting().sendPrompt("hi");

        assertEquals("recovered", res.get("message"));
    }

    @Test
    @DisplayName("모두 실패 → FallbackExhaustedException")
    void testAllFail() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi"))
                .thenThrow(new IOException("OpenAI API 호출 실패: 500 error"));
        when(gemini.chatting.sendPrompt("hi"))
                .thenThrow(new IOException("Gemini API 호출 실패: 503 unavailable"));

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        FallbackExhaustedException ex = assertThrows(FallbackExhaustedException.class,
                () -> fc.getChatting().sendPrompt("hi"));
        assertTrue(ex.getMessage().contains("provider"));
        assertNotNull(ex.getCause());
    }

    @Test
    @DisplayName("UNAUTHORIZED — 기본 trigger 미포함 → 즉시 throw (fallback 없음)")
    void testUnauthorizedNotTriggered() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi"))
                .thenThrow(new RuntimeException("OpenAI API 호출 실패: 401 Unauthorized"));

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        assertThrows(RuntimeException.class, () -> fc.getChatting().sendPrompt("hi"));
        verify(gemini.chatting, times(0)).sendPrompt(anyString()); // gemini 미호출
    }

    @Test
    @DisplayName("UNKNOWN reason — 기본 trigger 미포함 → 즉시 throw")
    void testUnknownNotTriggered() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi"))
                .thenThrow(new RuntimeException("some random error message"));

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        assertThrows(RuntimeException.class, () -> fc.getChatting().sendPrompt("hi"));
        verify(gemini.chatting, times(0)).sendPrompt(anyString());
    }

    @Test
    @DisplayName("UNAUTHORIZED 도 fallback 하도록 설정 가능")
    void testUnauthorizedCanBeAdded() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.chatting.sendPrompt("hi"))
                .thenThrow(new RuntimeException("OpenAI API 호출 실패: 401 Unauthorized"));
        when(gemini.chatting.sendPrompt("hi")).thenReturn(okResponse("ok"));

        FallbackPolicy policy = FallbackPolicy.builder()
                .addSlot(new OpenAiSlot(openai.room))
                .addSlot(new GeminiSlot(gemini.room))
                .triggerReasons(FailureReason.UNAUTHORIZED,
                        FailureReason.SERVER_ERROR, FailureReason.TIMEOUT)
                .build();
        FallbackChatRoom fc = new FallbackChatRoom(policy);

        JSONObject res = fc.getChatting().sendPrompt("hi");
        assertEquals("ok", res.get("message"));
        verify(gemini.chatting, times(1)).sendPrompt("hi");
    }

    @Test
    @DisplayName("close() — 모든 slot close 호출, 한 쪽 실패해도 계속")
    void testCloseAllSlots() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        when(openai.room.close()).thenThrow(new IOException("close error"));
        when(gemini.room.close()).thenReturn(true);

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        boolean ok = fc.close();
        assertFalse(ok, "한 쪽 close 실패 → false");
        verify(openai.room, times(1)).close();
        verify(gemini.room, times(1)).close();
    }

    // ========================================================================
    // v0.9.4 — FallbackContext ThreadLocal 주입 검증
    // ========================================================================

    @Test
    @DisplayName("FallbackContext — primary 호출 중 isFallback()=false")
    void testContextFalseOnPrimary() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        final Boolean[] captured = new Boolean[1];
        when(openai.chatting.sendPrompt("hi")).thenAnswer(inv -> {
            captured[0] = FallbackContext.isFallback();
            return okResponse("ok");
        });

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        fc.getChatting().sendPrompt("hi");

        assertEquals(Boolean.FALSE, captured[0], "primary 호출 중에는 isFallback=false");
        assertFalse(FallbackContext.isFallback(), "호출 완료 후 ThreadLocal 해제");
    }

    @Test
    @DisplayName("FallbackContext — fallback 호출 중 isFallback()=true, 종료 후 false")
    void testContextTrueOnFallback() throws Exception {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        final Boolean[] primaryCtx = new Boolean[1];
        final Boolean[] fallbackCtx = new Boolean[1];

        when(openai.chatting.sendPrompt("hi")).thenAnswer(inv -> {
            primaryCtx[0] = FallbackContext.isFallback();
            throw new IOException("OpenAI API 호출 실패: 500 - Internal Server Error");
        });
        when(gemini.chatting.sendPrompt("hi")).thenAnswer(inv -> {
            fallbackCtx[0] = FallbackContext.isFallback();
            return okResponse("from-gemini");
        });

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        fc.getChatting().sendPrompt("hi");

        assertEquals(Boolean.FALSE, primaryCtx[0], "primary 호출 시에는 아직 fallback 아님");
        assertEquals(Boolean.TRUE, fallbackCtx[0], "2번째 slot 호출 중 isFallback=true");
        assertFalse(FallbackContext.isFallback(), "chain 종료 후 ThreadLocal 해제");
    }

    @Test
    @DisplayName("FallbackContext — 예외 전파 후에도 ThreadLocal 해제 (finally 경로)")
    void testContextClearedOnException() {
        Pair openai = setupChatRoom();
        Pair gemini = setupChatRoom();
        try {
            when(openai.chatting.sendPrompt("hi"))
                    .thenThrow(new IOException("500 error"));
            when(gemini.chatting.sendPrompt("hi"))
                    .thenThrow(new IOException("503 error"));
        } catch (Exception e) {
            fail(e);
        }

        FallbackChatRoom fc = new FallbackChatRoom(
                FallbackPolicies.openaiPrimaryGeminiFallback(openai.room, gemini.room));
        assertThrows(FallbackExhaustedException.class,
                () -> fc.getChatting().sendPrompt("hi"));

        assertFalse(FallbackContext.isFallback(),
                "chain 전체 실패 후에도 ThreadLocal 은 깨끗해야 한다");
    }
}

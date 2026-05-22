package com.smartuxapi.ai.claude;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cache.NoOpCacheStrategy;
import com.smartuxapi.ai.cache.claude.ClaudePromptCacheStrategy;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClaudeChatRoom 단위 테스트 — 생성자/접근자는 네트워크 호출이 없으므로 더미 키로 검증.
 */
@DisplayName("Claude ChatRoom 테스트")
class ClaudeChatRoomTest {

    @Test
    @DisplayName("ChatRoom 생성 — id 와 ActionQueueHandler 설정")
    void testChatRoomCreation() {
        ChatRoom chatRoom = new ClaudeChatRoom("test-key", "claude-sonnet-4-6");
        chatRoom.setActionQueueHandler(new ActionQueueHandler());

        assertNotNull(chatRoom.getId(), "ChatRoom ID가 있어야 합니다");
        assertNotNull(chatRoom.getActionQueueHandler(), "ActionQueueHandler가 설정되어야 합니다");
    }

    @Test
    @DisplayName("기본 캐시 전략은 ClaudePromptCacheStrategy")
    void testDefaultCacheStrategy() {
        ChatRoom chatRoom = new ClaudeChatRoom("test-key", "claude-sonnet-4-6");
        assertTrue(chatRoom.getCacheStrategy() instanceof ClaudePromptCacheStrategy);
    }

    @Test
    @DisplayName("setCacheStrategy(null) — NoOpCacheStrategy 로 리셋")
    void testSetCacheStrategyNull() {
        ChatRoom chatRoom = new ClaudeChatRoom("test-key", "claude-sonnet-4-6");
        chatRoom.setCacheStrategy(null);
        assertSame(NoOpCacheStrategy.INSTANCE, chatRoom.getCacheStrategy());
    }

    @Test
    @DisplayName("getChatting — ClaudeChatting 인스턴스 반환 (재호출 시 동일)")
    void testGetChatting() {
        ChatRoom chatRoom = new ClaudeChatRoom("test-key", "claude-sonnet-4-6");
        Chatting first = chatRoom.getChatting();
        assertNotNull(first);
        assertSame(first, chatRoom.getChatting(), "getChatting 은 동일 인스턴스를 재사용한다");
    }

    @Test
    @DisplayName("close — 예외 없이 종료")
    void testClose() throws Exception {
        ChatRoom chatRoom = new ClaudeChatRoom("test-key", "claude-sonnet-4-6");
        assertTrue(chatRoom.close());
    }

    @Test
    @DisplayName("anthropic-version 미지정 시 기본값 적용")
    void testDefaultAnthropicVersion() {
        ClaudeAPIConnection conn = new ClaudeAPIConnection("test-key", "claude-sonnet-4-6");
        assertEquals(ClaudeAPIConnection.DEFAULT_ANTHROPIC_VERSION, conn.getAnthropicVersion());
    }

    @Test
    @DisplayName("anthropic-version 명시 — 그대로 적용")
    void testCustomAnthropicVersion() {
        ClaudeAPIConnection conn = new ClaudeAPIConnection("test-key", "claude-sonnet-4-6", "2099-01-01");
        assertEquals("2099-01-01", conn.getAnthropicVersion());
    }
}

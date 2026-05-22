package com.smartuxapi.ai.fallback;

import com.smartuxapi.ai.ChatRoom;

/**
 * {@link com.smartuxapi.ai.claude.ClaudeChatRoom} 를 감싸는 fallback slot.
 *
 * @since 0.10.0
 */
public final class ClaudeSlot implements ProviderSlot {

    private final ChatRoom chatRoom;
    private final String name;

    public ClaudeSlot(ChatRoom chatRoom) {
        this(chatRoom, "claude");
    }

    public ClaudeSlot(ChatRoom chatRoom, String name) {
        if (chatRoom == null) throw new IllegalArgumentException("chatRoom is required");
        this.chatRoom = chatRoom;
        this.name = name == null ? "claude" : name;
    }

    @Override public String getName() { return name; }
    @Override public ChatRoom getChatRoom() { return chatRoom; }
}

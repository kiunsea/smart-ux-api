package com.smartuxapi.ai.fallback;

import com.smartuxapi.ai.ChatRoom;

/**
 * {@link com.smartuxapi.ai.gemini.GeminiChatRoom} 를 감싸는 기본 slot.
 *
 * @since 0.9.1
 */
public final class GeminiSlot implements ProviderSlot {

    private final ChatRoom chatRoom;
    private final String name;

    public GeminiSlot(ChatRoom chatRoom) {
        this(chatRoom, "gemini");
    }

    public GeminiSlot(ChatRoom chatRoom, String name) {
        if (chatRoom == null) throw new IllegalArgumentException("chatRoom is required");
        this.chatRoom = chatRoom;
        this.name = name == null ? "gemini" : name;
    }

    @Override public String getName() { return name; }
    @Override public ChatRoom getChatRoom() { return chatRoom; }
}

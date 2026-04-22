package com.smartuxapi.ai.fallback;

import com.smartuxapi.ai.ChatRoom;

/**
 * {@link com.smartuxapi.ai.openai.ResponsesChatRoom} 를 감싸는 기본 slot.
 *
 * @since 0.9.1
 */
public final class OpenAiSlot implements ProviderSlot {

    private final ChatRoom chatRoom;
    private final String name;

    public OpenAiSlot(ChatRoom chatRoom) {
        this(chatRoom, "openai");
    }

    public OpenAiSlot(ChatRoom chatRoom, String name) {
        if (chatRoom == null) throw new IllegalArgumentException("chatRoom is required");
        this.chatRoom = chatRoom;
        this.name = name == null ? "openai" : name;
    }

    @Override public String getName() { return name; }
    @Override public ChatRoom getChatRoom() { return chatRoom; }
}

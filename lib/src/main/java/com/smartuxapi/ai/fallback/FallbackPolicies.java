package com.smartuxapi.ai.fallback;

import com.smartuxapi.ai.ChatRoom;

/**
 * {@link FallbackPolicy} 자주 쓰이는 프리셋 조합.
 *
 * @since 0.9.1
 */
public final class FallbackPolicies {

    private FallbackPolicies() {}

    /**
     * OpenAI primary → Gemini fallback, 기본 trigger reason.
     */
    public static FallbackPolicy openaiPrimaryGeminiFallback(ChatRoom openai, ChatRoom gemini) {
        return FallbackPolicy.builder()
                .addSlot(new OpenAiSlot(openai))
                .addSlot(new GeminiSlot(gemini))
                .build();
    }

    /**
     * Gemini primary → OpenAI fallback, 기본 trigger reason.
     */
    public static FallbackPolicy geminiPrimaryOpenaiFallback(ChatRoom gemini, ChatRoom openai) {
        return FallbackPolicy.builder()
                .addSlot(new GeminiSlot(gemini))
                .addSlot(new OpenAiSlot(openai))
                .build();
    }
}

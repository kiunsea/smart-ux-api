package com.smartuxapi.ai.fallback;

import com.smartuxapi.ai.ChatRoom;

/**
 * Fallback chain 의 한 자리. 기본 구현 {@link OpenAiSlot} / {@link GeminiSlot} 제공.
 *
 * <p>Slot 은 {@link ChatRoom} 을 소유한다. Slot 이름은 로깅/메트릭 용도.
 *
 * @since 0.9.1
 */
public interface ProviderSlot {

    /**
     * Slot 식별자 — 로그/telemetry 에 표시.
     */
    String getName();

    /**
     * 이 slot 이 소유한 ChatRoom.
     */
    ChatRoom getChatRoom();

    /**
     * 이 slot 이 이 예외를 처리(재시도)할 수 있는가.
     * {@code false} 를 반환하면 chain 순회를 중단하고 원본 예외를 즉시 throw.
     *
     * <p>기본 구현은 모든 예외에 대해 {@code true} — 실제 fallback 여부는
     * {@link FallbackPolicy#getTriggerReasons()} 와 조합해 결정.
     */
    default boolean canHandle(Throwable t) {
        return true;
    }
}

package com.smartuxapi.ai.cost;

/**
 * Fallback 실행 컨텍스트 — 현재 호출이 fallback 경로인지 {@link ThreadLocal} 로 표시.
 *
 * <p>{@code FallbackChatRoom} 이 chain 의 1번째(primary) 를 초과한 slot 호출 직전에
 * {@link #enterFallback()} 을 호출하고, 호출 종료 후 {@link #exit()} 으로 해제한다.
 *
 * <p>{@code APIConnection} / Vision / Embedding 서비스의 {@code recordCost()} 는
 * {@link #isFallback()} 을 읽어 {@link CostEntry#isFallbackTriggered()} 필드를 세팅한다.
 *
 * <p>동기 호출 흐름을 전제로 한다. 비동기/thread pool 처리가 필요하면 ThreadLocal
 * 전파 전략이 추가로 필요하지만, smart-ux-api 는 현재 모든 API 호출이 호출 스레드에서
 * 동기 수행되므로 간단하게 구현한다.
 *
 * @since 0.9.4
 */
public final class FallbackContext {

    private static final ThreadLocal<Boolean> CURRENT = ThreadLocal.withInitial(() -> false);

    private FallbackContext() {}

    /**
     * 현재 스레드가 fallback 경로를 수행 중이면 true.
     */
    public static boolean isFallback() {
        Boolean v = CURRENT.get();
        return v != null && v;
    }

    /**
     * fallback 경로 진입 표시. 호출 종료 후 반드시 {@link #exit()} 를 호출해야 한다.
     */
    public static void enterFallback() {
        CURRENT.set(Boolean.TRUE);
    }

    /**
     * 컨텍스트 해제. ThreadLocal 누수 방지.
     */
    public static void exit() {
        CURRENT.remove();
    }
}

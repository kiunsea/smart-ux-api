package com.smartuxapi.ai.cache;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provider 중립 프롬프트 캐시 전략 인터페이스.
 *
 * <p>구현체는 각 LLM provider 가 제공하는 캐시 메커니즘을 감싼다.
 * <ul>
 *   <li>OpenAI Responses API: 자동 프리픽스 캐싱 — 힌트 콘텐츠를 대화 시작부에 배치한다.</li>
 *   <li>Gemini API: 명시적 {@code cachedContents} 리소스 — prime 시 서버에 생성하고,
 *       요청마다 참조로 포함하며, 종료 시 삭제한다.</li>
 * </ul>
 *
 * <p>기본 구현으로 {@link NoOpCacheStrategy} 가 제공되며, 캐시를 사용하지 않는 경로의 안전한 fallback 이다.
 *
 * @since 0.7.0
 */
public interface CacheStrategy {

    /**
     * 캐시 힌트를 서버 또는 로컬 상태에 등록한다.
     * <ul>
     *   <li>OpenAI: 로컬 상태만 저장 (네트워크 호출 없음)</li>
     *   <li>Gemini: {@code POST /v1beta/cachedContents} 호출 (네트워크 호출 발생)</li>
     * </ul>
     *
     * <p>동일 힌트를 재호출하면 기존 캐시를 교체/갱신한다.
     *
     * @param hint 캐시 힌트
     * @throws Exception 서버 호출 실패 시
     */
    void prime(CacheHint hint) throws Exception;

    /**
     * 마지막 API 호출의 캐시 메트릭을 반환한다.
     * API 호출 전이거나 메트릭이 없으면 {@link CacheMetrics#EMPTY} 반환.
     */
    CacheMetrics getLastMetrics();

    /**
     * API 응답에서 캐시 메트릭을 추출하여 내부 상태를 갱신한다.
     * 각 provider 의 {@code APIConnection} 이 응답 수신 직후 호출한다.
     *
     * @param responseJson provider API 응답 JSON 루트
     */
    void recordMetricsFromResponse(JsonNode responseJson);

    /**
     * 서버 측 캐시 리소스를 해제한다.
     * <ul>
     *   <li>OpenAI: no-op</li>
     *   <li>Gemini: {@code DELETE /v1beta/cachedContents/{id}}</li>
     * </ul>
     *
     * <p>{@code ChatRoom.close()} 에서 호출된다.
     *
     * @throws Exception 서버 호출 실패 시 (무시해도 안전 — 리소스는 TTL 로 만료됨)
     */
    void invalidate() throws Exception;

    /**
     * 현재 등록된 힌트 (없으면 null).
     * {@code APIConnection} 이 요청 본문에 반영할 때 참조한다.
     */
    CacheHint getCurrentHint();

    /**
     * Provider 식별자 — "openai" / "gemini" / "none".
     */
    String getProvider();
}

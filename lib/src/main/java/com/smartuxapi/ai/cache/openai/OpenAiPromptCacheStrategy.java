package com.smartuxapi.ai.cache.openai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;

/**
 * OpenAI Responses API 자동 프리픽스 캐싱 전략.
 *
 * <p>OpenAI 측 정책:
 * <ul>
 *   <li>입력 토큰 수가 1024 이상일 때 프리픽스가 자동 캐시됨</li>
 *   <li>요청 본문에는 별도 캐시 플래그가 없음 — 개발자가 안정적 콘텐츠를 시작부에 배치하면 됨</li>
 *   <li>응답 {@code usage.prompt_tokens_details.cached_tokens} 로 히트 확인</li>
 * </ul>
 *
 * <p>본 전략의 역할:
 * <ol>
 *   <li>{@link #prime(CacheHint)} 로 받은 콘텐츠를 로컬에 보관</li>
 *   <li>{@link com.smartuxapi.ai.openai.ConversationHistory} 가 이 힌트를 system 메시지로 프리픽스에 배치하도록 노출</li>
 *   <li>응답에서 캐시 토큰 수를 파싱하여 메트릭으로 보관</li>
 * </ol>
 *
 * @since 0.7.0
 */
public class OpenAiPromptCacheStrategy implements CacheStrategy {

    private static final Logger log = LogManager.getLogger(OpenAiPromptCacheStrategy.class);

    private CacheHint currentHint = null;
    private CacheMetrics lastMetrics = CacheMetrics.EMPTY;

    @Override
    public void prime(CacheHint hint) {
        this.currentHint = hint;
        if (hint != null) {
            log.debug("OpenAI cache primed (local prefix): {}", hint);
        }
    }

    @Override
    public CacheMetrics getLastMetrics() {
        return lastMetrics;
    }

    /**
     * OpenAI Responses API 응답에서 {@code usage.prompt_tokens} 와
     * {@code usage.prompt_tokens_details.cached_tokens} 를 추출하여 메트릭을 갱신한다.
     */
    @Override
    public void recordMetricsFromResponse(JsonNode responseJson) {
        if (responseJson == null) return;
        JsonNode usage = responseJson.get("usage");
        if (usage == null) {
            log.debug("OpenAI response missing 'usage' field; metrics unchanged");
            return;
        }

        long totalInput = readLong(usage, "prompt_tokens", 0L);
        long cached = 0L;
        JsonNode details = usage.get("prompt_tokens_details");
        if (details != null) {
            cached = readLong(details, "cached_tokens", 0L);
        }

        this.lastMetrics = new CacheMetrics(totalInput, cached, "openai");
        log.debug("OpenAI cache metrics: {}", this.lastMetrics);
    }

    @Override
    public void invalidate() {
        this.currentHint = null;
        this.lastMetrics = CacheMetrics.EMPTY;
        log.debug("OpenAI cache invalidated (local)");
    }

    @Override
    public CacheHint getCurrentHint() {
        return currentHint;
    }

    @Override
    public String getProvider() {
        return "openai";
    }

    private static long readLong(JsonNode node, String field, long fallback) {
        JsonNode v = node.get(field);
        return (v == null || !v.canConvertToLong()) ? fallback : v.asLong();
    }
}

package com.smartuxapi.ai.cache;

import java.util.Objects;

/**
 * 캐시 대상 콘텐츠를 기술하는 불변 값 객체.
 *
 * <p>대화 시작 시 안정적으로 재사용될 프리픽스(예: UI Object Map, 시스템 프롬프트, 대형 지침)
 * 를 {@link CacheStrategy} 에 전달하는 데 사용된다.
 *
 * <p>Provider 별 취급:
 * <ul>
 *   <li>OpenAI Responses API: {@code content} 가 대화 시작부에 배치되어 자동 프리픽스 캐시 대상이 된다.
 *       {@code ttlSeconds} 는 무시된다.</li>
 *   <li>Gemini API: {@code content} 로 {@code cachedContents} 리소스를 생성하고 요청에 참조로 포함한다.
 *       {@code ttlSeconds} 가 TTL 로 사용된다 (기본 3600 초).</li>
 * </ul>
 *
 * @since 0.7.0
 */
public final class CacheHint {

    /** Gemini 기본 TTL (초). */
    public static final long DEFAULT_TTL_SECONDS = 3600L;

    private final String content;
    private final String label;
    private final long ttlSeconds;

    private CacheHint(String content, String label, long ttlSeconds) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.label = (label == null || label.isEmpty()) ? "unlabeled" : label;
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 기본 TTL(3600초) 을 사용하는 캐시 힌트를 생성한다.
     *
     * @param content 캐시될 콘텐츠 (필수)
     * @return CacheHint
     */
    public static CacheHint of(String content) {
        return new CacheHint(content, null, DEFAULT_TTL_SECONDS);
    }

    /**
     * 라벨을 지정한 캐시 힌트를 생성한다. 라벨은 로깅/메트릭에 사용된다.
     *
     * @param content 캐시될 콘텐츠 (필수)
     * @param label 사람이 읽을 수 있는 라벨 (nullable)
     * @return CacheHint
     */
    public static CacheHint of(String content, String label) {
        return new CacheHint(content, label, DEFAULT_TTL_SECONDS);
    }

    /**
     * TTL 을 명시한 캐시 힌트를 생성한다. Gemini 에서만 의미 있음.
     *
     * @param content 캐시될 콘텐츠 (필수)
     * @param ttlSeconds TTL 초 (양수)
     * @return CacheHint
     */
    public static CacheHint withTtl(String content, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be positive, got " + ttlSeconds);
        }
        return new CacheHint(content, null, ttlSeconds);
    }

    /**
     * 라벨과 TTL 을 모두 지정한 캐시 힌트를 생성한다.
     */
    public static CacheHint of(String content, String label, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be positive, got " + ttlSeconds);
        }
        return new CacheHint(content, label, ttlSeconds);
    }

    public String getContent() { return content; }
    public String getLabel()   { return label; }
    public long getTtlSeconds(){ return ttlSeconds; }

    /** 콘텐츠 길이 (문자 수). 로깅용. */
    public int getContentLength() { return content.length(); }

    @Override
    public String toString() {
        return "CacheHint{label='" + label + "', contentLen=" + content.length()
                + ", ttlSec=" + ttlSeconds + "}";
    }
}

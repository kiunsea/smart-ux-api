package com.smartuxapi.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.NoOpCacheStrategy;
import com.smartuxapi.ai.debug.DebugLogger;

/**
 * Chatting 저장소
 */
public interface ChatRoom {

	public String getId();

	/**
	 * Chatting instance 를 반환
	 *
	 * @return
	 */
	public Chatting getChatting();

	/**
	 * ChatRoom 종료<br/>
	 *   - ChatRoo의 구성 객체를 null로 초기화<br/>
	 *   - openai 에서는 thread 삭제
	 *
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean close() throws IOException, ParseException;

	public void setActionQueueHandler(ActionQueueHandler aqHandler);
	public ActionQueueHandler getActionQueueHandler();

	/**
	 * 디버그 로거 설정
	 *
	 * @param debugLogger
	 */
	default void setDebugLogger(DebugLogger debugLogger) {
		// 기본 구현은 아무것도 하지 않음
	}

	/**
	 * 디버그 로거 반환
	 *
	 * @return
	 */
	default DebugLogger getDebugLogger() {
		return null;
	}

	/**
	 * 캐시 전략을 설정한다. 기본 구현은 no-op.
	 * 공급자별 구현체({@code ResponsesChatRoom}, {@code GeminiChatRoom}) 가 override 한다.
	 *
	 * @param strategy 전략 (null → {@link NoOpCacheStrategy} 로 리셋)
	 * @since 0.7.0
	 */
	default void setCacheStrategy(CacheStrategy strategy) { /* default no-op */ }

	/**
	 * 현재 설정된 캐시 전략 (기본 {@link NoOpCacheStrategy}).
	 * @since 0.7.0
	 */
	default CacheStrategy getCacheStrategy() { return NoOpCacheStrategy.INSTANCE; }

	/**
	 * 캐시 대상 콘텐츠를 등록한다. 이후 모든 {@code sendPrompt} 호출이 이 힌트를
	 * 프리픽스로 사용하여 토큰 비용을 절감한다.
	 *
	 * <p>Provider 에 따라 동작이 다르다:
	 * <ul>
	 *   <li>OpenAI: 대화 시작부 system 메시지로 배치 (자동 프리픽스 캐싱 대상)</li>
	 *   <li>Gemini: 서버에 {@code cachedContents} 리소스 생성 (네트워크 호출 발생)</li>
	 * </ul>
	 *
	 * @param hint 캐시 힌트 (null 전달 시 힌트 해제)
	 * @throws Exception Gemini 서버 호출 실패 등
	 * @since 0.7.0
	 */
	default void markAsCacheable(CacheHint hint) throws Exception { /* default no-op */ }

	/**
	 * 마지막 {@code sendPrompt} 호출의 캐시 메트릭 (히트율 포함).
	 * @since 0.7.0
	 */
	default CacheMetrics getLastCacheMetrics() {
		return getCacheStrategy().getLastMetrics();
	}

}

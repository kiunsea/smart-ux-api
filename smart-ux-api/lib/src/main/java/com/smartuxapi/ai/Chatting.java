package com.smartuxapi.ai;

import java.util.Set;

import org.json.simple.JSONObject;

import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.NoOpCacheStrategy;

/**
 * prompt message 를 전송하고 응답받는다.
 */
public interface Chatting {

    public void setActionQueueHandler(ActionQueueHandler aqHandler);

	/**
	 * 입력한 사용자 메세지를 thread 에 추가하고 run 한다
	 *
	 * @param ActionQueueHandler
	 * @param userMsg
	 * @return {"message":String, "action_queue":JSON String, "userFunctionsResult":JSON String}
	 * @throws Exception
	 */
	public JSONObject sendPrompt(String userMsg) throws Exception;

	/**
	 * 보유하고 있는 message id set 을 반환
	 * @return
	 */
	public Set<String> getMessageIdSet();

	/**
	 * 캐시 전략을 설정한다. 기본 구현은 no-op.
	 *
	 * @param strategy 전략 (null 전달 시 {@link NoOpCacheStrategy} 로 리셋)
	 * @since 0.7.0
	 */
	default void setCacheStrategy(CacheStrategy strategy) { /* default no-op */ }

	/**
	 * 현재 설정된 캐시 전략 (기본 {@link NoOpCacheStrategy}).
	 * @since 0.7.0
	 */
	default CacheStrategy getCacheStrategy() { return NoOpCacheStrategy.INSTANCE; }

	/**
	 * 캐시 힌트를 적용한다. provider 별로:
	 * <ul>
	 *   <li>OpenAI: {@code strategy.prime()} 호출 + ConversationHistory 에 프리픽스 설정</li>
	 *   <li>Gemini: {@code strategy.prime()} 호출 (서버 cachedContents 리소스 생성)</li>
	 * </ul>
	 * null 전달 시 힌트 해제 + 전략 invalidate.
	 *
	 * @param hint 캐시 힌트 (nullable)
	 * @throws Exception provider 측 prime 실패 시
	 * @since 0.7.0
	 */
	default void applyCacheHint(CacheHint hint) throws Exception { /* default no-op */ }

}

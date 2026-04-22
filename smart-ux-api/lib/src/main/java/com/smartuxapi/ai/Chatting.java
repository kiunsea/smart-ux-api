package com.smartuxapi.ai;

import java.util.Set;

import org.json.simple.JSONObject;

import java.util.List;

import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.NoOpCacheStrategy;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolResult;

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

	/**
	 * 구조화 응답(JSON Schema) 을 강제하는 프롬프트 전송.
	 *
	 * <p>Provider 에 따라 다음 필드로 매핑된다:
	 * <ul>
	 *   <li>OpenAI: 요청 {@code text.format = { type: "json_schema", ... }}</li>
	 *   <li>Gemini: 요청 {@code generationConfig.responseMimeType = "application/json"}
	 *       + {@code responseSchema}</li>
	 * </ul>
	 *
	 * <p>반환 포맷은 기존 {@link #sendPrompt(String)} 와 동일한 키
	 * ({@code message}, {@code action_queue}) 를 유지하면서 파싱 결과를 신규 키로 병기한다:
	 * <ul>
	 *   <li>{@code message} — provider 응답 원문 (JSON 문자열)</li>
	 *   <li>{@code action_queue} — 기존과 동일 (ActionQueueHandler 경로)</li>
	 *   <li>{@code structured} — 원문을 파싱한 {@code JsonNode} (파싱 실패 시 {@code null})</li>
	 * </ul>
	 *
	 * <p>파싱 실패는 예외를 던지지 않는다 — WARN 로그 후 {@code structured: null} 로 넘긴다.
	 * 호출자가 원문을 재파싱하거나 fallback 을 결정할 수 있도록 한다.
	 *
	 * <p>기본 구현은 {@link UnsupportedOperationException} — provider 별 구현체가 override 한다.
	 *
	 * @param userMsg 사용자 메시지
	 * @param schema 응답 스키마 (null 이면 {@link #sendPrompt(String)} 로 위임)
	 * @return 응답 JSONObject
	 * @throws Exception API 호출 실패 등
	 * @since 0.8.0
	 */
	default org.json.simple.JSONObject sendPromptWithSchema(String userMsg, ResponseSchema schema) throws Exception {
		if (schema == null) return sendPrompt(userMsg);
		throw new UnsupportedOperationException(
				"This provider has not implemented structured output yet");
	}

	/** 기본 최대 tool 호출 라운드 수 — 초과 시 마지막 응답 반환 + 경고 로그. @since 0.8.0 */
	int DEFAULT_MAX_TOOL_ROUNDS = 5;

	/**
	 * Tool Use — 자동 루프 모드.
	 *
	 * <p>LLM 이 {@code tools} 에서 함수 호출을 요청하면 라이브러리가 자동으로
	 * handler 를 실행하고 결과를 다시 LLM 에 전달. 최대 {@link #DEFAULT_MAX_TOOL_ROUNDS}
	 * 라운드까지 반복한다. 초과 시 마지막 응답을 그대로 반환하고 경고 로그를 남긴다.
	 *
	 * <p>반환 JSON:
	 * <pre>{
	 *   "message": "최종 텍스트",
	 *   "action_queue": {...},
	 *   "tool_calls": [ { id, toolName, arguments, result } ]
	 * }</pre>
	 *
	 * <p>{@code tools} 가 null/empty 이면 {@link #sendPrompt(String)} 로 위임한다.
	 *
	 * <p>기본 구현은 {@link UnsupportedOperationException} — provider 구현체가 override.
	 *
	 * @since 0.8.0
	 */
	default org.json.simple.JSONObject sendPromptWithTools(String userMsg, ToolRegistry tools) throws Exception {
		if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);
		throw new UnsupportedOperationException(
				"This provider has not implemented tool use yet");
	}

	/**
	 * Tool Use — 수동 dispatch 모드.
	 *
	 * <p>LLM 이 tool 호출을 요청하면 즉시 반환하여 호출자가 직접 handler 를 실행할 수 있게 한다.
	 * 호출자는 결과를 {@link #continueWithToolResults(List, ToolRegistry)} 로 전달하여 대화를 이어간다.
	 *
	 * <p>반환 JSON:
	 * <pre>
	 *  // LLM 이 바로 최종 응답을 낸 경우 (tool 호출 없음)
	 *  { "message": "...", "action_queue": {...}, "tool_calls": [] }
	 *
	 *  // LLM 이 tool 호출을 요청한 경우
	 *  { "message": null, "tool_calls": [ { id, toolName, arguments } ], "pending": true }
	 * </pre>
	 *
	 * <p>기본 구현은 {@link UnsupportedOperationException}.
	 *
	 * @since 0.8.0
	 */
	default org.json.simple.JSONObject sendPromptExpectingToolCalls(String userMsg, ToolRegistry tools) throws Exception {
		if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);
		throw new UnsupportedOperationException(
				"This provider has not implemented tool use yet");
	}

	/**
	 * 수동 dispatch 의 후속 호출 — 호출자가 실행한 tool 결과들을 submit 하고 대화를 이어간다.
	 * 반환 포맷은 {@link #sendPromptExpectingToolCalls(String, ToolRegistry)} 와 동일.
	 *
	 * @since 0.8.0
	 */
	default org.json.simple.JSONObject continueWithToolResults(List<ToolResult> results, ToolRegistry tools) throws Exception {
		throw new UnsupportedOperationException(
				"This provider has not implemented tool use yet");
	}

}

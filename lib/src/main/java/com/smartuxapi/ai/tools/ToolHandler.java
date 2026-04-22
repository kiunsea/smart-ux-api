package com.smartuxapi.ai.tools;

/**
 * Tool 실행 함수형 인터페이스. LLM 이 호출을 요청했을 때 라이브러리가 invoke 한다.
 *
 * <p>구현체는 {@code call.getArguments()} 로 LLM 이 생성한 인자를 받아
 * 필요한 로직을 수행 후 {@link ToolResult#ok(String, com.fasterxml.jackson.databind.JsonNode)}
 * 혹은 {@link ToolResult#error(String, String)} 로 돌려준다.
 *
 * <p>예외를 던지면 라이브러리가 캐치하여 자동으로
 * {@link ToolResult#error(String, String)} 로 변환한다.
 *
 * @since 0.8.0
 */
@FunctionalInterface
public interface ToolHandler {
    ToolResult invoke(ToolCall call) throws Exception;
}

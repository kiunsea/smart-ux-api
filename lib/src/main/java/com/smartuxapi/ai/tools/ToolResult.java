package com.smartuxapi.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Tool 실행 결과.
 *
 * <p>설계 제약 (§10 Q5):
 * <ul>
 *   <li>{@code output} 직렬화 크기 상한 {@value #MAX_OUTPUT_BYTES} bytes (256KB)</li>
 *   <li>초과 시 자동 축약 + 경고 메시지 포함 {@link #errorMessage} (실패 아님)</li>
 * </ul>
 *
 * @since 0.8.0
 */
public final class ToolResult {

    /** 결과 페이로드 상한 (byte, UTF-8). 256KB. */
    public static final int MAX_OUTPUT_BYTES = 256 * 1024;

    private final String callId;
    private final JsonNode output;
    private final boolean isError;
    private final String errorMessage;

    private ToolResult(String callId, JsonNode output, boolean isError, String errorMessage) {
        if (callId == null || callId.isEmpty()) {
            throw new IllegalArgumentException("ToolResult.callId is required");
        }
        this.callId = callId;
        this.output = output;
        this.isError = isError;
        this.errorMessage = errorMessage;
    }

    /**
     * 성공 결과 생성.
     *
     * @param callId 대응 {@link ToolCall#getId()}
     * @param output 결과 JSON. null 허용 (빈 결과)
     */
    public static ToolResult ok(String callId, JsonNode output) {
        return new ToolResult(callId, output, false, null);
    }

    /**
     * 실패 결과 생성. LLM 은 {@code errorMessage} 를 보고 재시도 판단한다.
     *
     * @param callId 대응 {@link ToolCall#getId()}
     * @param errorMessage 에러 설명
     */
    public static ToolResult error(String callId, String errorMessage) {
        return new ToolResult(callId, TextNode.valueOf(errorMessage == null ? "" : errorMessage),
                true, errorMessage);
    }

    public String getCallId() { return callId; }
    public JsonNode getOutput() { return output; }
    public boolean isError() { return isError; }
    public String getErrorMessage() { return errorMessage; }

    @Override
    public String toString() {
        return "ToolResult{callId=" + callId + ", isError=" + isError + "}";
    }
}

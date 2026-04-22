package com.smartuxapi.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * LLM 이 생성한 Tool 호출 요청.
 *
 * <p>Provider 별 호출 ID 포맷 차이:
 * <ul>
 *   <li>OpenAI: {@code call_xxx} (provider 부여)</li>
 *   <li>Gemini: 클라이언트 생성 (UUID — Gemini API 는 호출 ID 를 응답에 포함하지 않는다)</li>
 * </ul>
 *
 * @since 0.8.0
 */
public final class ToolCall {

    private final String id;
    private final String toolName;
    private final JsonNode arguments;

    public ToolCall(String id, String toolName, JsonNode arguments) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("ToolCall.id is required");
        }
        if (toolName == null || toolName.isEmpty()) {
            throw new IllegalArgumentException("ToolCall.toolName is required");
        }
        this.id = id;
        this.toolName = toolName;
        this.arguments = arguments;
    }

    public String getId() { return id; }
    public String getToolName() { return toolName; }
    public JsonNode getArguments() { return arguments; }

    @Override
    public String toString() {
        return "ToolCall{id=" + id + ", tool=" + toolName + "}";
    }
}

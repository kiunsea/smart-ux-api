package com.smartuxapi.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Tool 정의 — 이름, 설명, 인자 스키마, 실행 핸들러.
 *
 * <p>{@code parametersSchema} 는 JSON Schema object 루트이며,
 * {@link com.smartuxapi.ai.schema.SchemaBuilder} 로 만들거나 raw {@link JsonNode} 로 직접 구성한다.
 * Structured Output (T2-a) 과 동일 포맷.
 *
 * @since 0.8.0
 */
public final class ToolDefinition {

    private final String name;
    private final String description;
    private final JsonNode parametersSchema;
    private final ToolHandler handler;

    public ToolDefinition(String name, String description,
                          JsonNode parametersSchema, ToolHandler handler) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ToolDefinition.name is required");
        }
        if (parametersSchema == null) {
            throw new IllegalArgumentException("ToolDefinition.parametersSchema is required");
        }
        if (handler == null) {
            throw new IllegalArgumentException("ToolDefinition.handler is required");
        }
        this.name = name;
        this.description = description == null ? "" : description;
        this.parametersSchema = parametersSchema;
        this.handler = handler;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public JsonNode getParametersSchema() { return parametersSchema; }
    public ToolHandler getHandler() { return handler; }
}

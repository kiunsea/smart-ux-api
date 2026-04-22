package com.smartuxapi.ai.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provider 중립 구조화 응답(JSON Schema) 래퍼.
 *
 * <p>OpenAI Responses API 의 {@code text.format.json_schema} 와
 * Gemini 의 {@code generationConfig.responseSchema} 두 경로에 동일하게 매핑된다.
 *
 * <p>사용 예:
 * <pre>{@code
 *   JsonNode schemaNode = ...;  // JSON Schema (object root)
 *   ResponseSchema schema = ResponseSchema.of("UserProfile", schemaNode);
 *   chatting.sendPromptWithSchema("Alice, 30", schema);
 * }</pre>
 *
 * <p>제약:
 * <ul>
 *   <li>Root 는 {@code type: "object"} 만 지원 (provider 공통).</li>
 *   <li>OpenAI strict 모드에서는 {@code additionalProperties: false} 가 필수 —
 *       {@link SchemaBuilder} 경로는 자동 주입, raw JsonNode 경로는 호출자 책임.</li>
 *   <li>Gemini 는 OpenAI JSON Schema 의 서브셋만 지원 ({@code $ref}, {@code anyOf} 제한).
 *       초과 시 provider 측 오류가 그대로 전파된다.</li>
 * </ul>
 *
 * @since 0.8.0
 */
public final class ResponseSchema {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String name;
    private final String description;
    private final JsonNode schema;
    private final boolean strict;

    private ResponseSchema(String name, String description, JsonNode schema, boolean strict) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("ResponseSchema.name is required");
        }
        if (schema == null) {
            throw new IllegalArgumentException("ResponseSchema.schema is required");
        }
        this.name = name;
        this.description = description;
        this.schema = schema;
        this.strict = strict;
    }

    /**
     * 이미 만들어진 JsonNode 로부터 구성.
     *
     * @param name   schema 이름 (OpenAI 측 식별자로 쓰임, 필수)
     * @param schema JSON Schema (object root)
     */
    public static ResponseSchema of(String name, JsonNode schema) {
        return new ResponseSchema(name, null, schema, true);
    }

    /**
     * 이름/설명/스키마/strict 플래그를 모두 지정.
     */
    public static ResponseSchema of(String name, String description, JsonNode schema, boolean strict) {
        return new ResponseSchema(name, description, schema, strict);
    }

    /**
     * 새 {@link SchemaBuilder} 를 object 루트로 시작한다.
     */
    public static SchemaBuilder object() {
        return SchemaBuilder.object();
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public JsonNode getSchema() { return schema; }
    public boolean isStrict() { return strict; }

    /**
     * 디버깅용 JSON 문자열.
     */
    @Override
    public String toString() {
        try {
            return "ResponseSchema{name=" + name + ", strict=" + strict
                    + ", schema=" + MAPPER.writeValueAsString(schema) + "}";
        } catch (Exception e) {
            return "ResponseSchema{name=" + name + ", strict=" + strict + "}";
        }
    }
}

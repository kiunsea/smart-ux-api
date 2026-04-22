package com.smartuxapi.ai.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON Schema 편의 빌더.
 *
 * <p>Object 루트 + 5가지 property 타입(string/integer/boolean/array/nested object) 만 지원.
 * 범위를 벗어나는 스키마는 raw {@link JsonNode} 를 직접 구성하여 {@link ResponseSchema#of(String, JsonNode)} 에
 * 전달한다.
 *
 * <p>{@link #build()} 가 반환하는 JsonNode 는 {@link ResponseSchema} 와
 * {@code com.smartuxapi.ai.tools.ToolDefinition.parametersSchema} (T2-b) 에서 공통으로 사용된다.
 *
 * <p>Strict 호환을 위해 {@link #build()} 는 object 루트에 자동으로
 * {@code additionalProperties: false} 를 주입한다.
 *
 * @since 0.8.0
 */
public final class SchemaBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ObjectNode properties = MAPPER.createObjectNode();
    private final Set<String> required = new LinkedHashSet<>();
    private String description;

    private SchemaBuilder() { /* factory only */ }

    public static SchemaBuilder object() {
        return new SchemaBuilder();
    }

    public SchemaBuilder description(String description) {
        this.description = description;
        return this;
    }

    public SchemaBuilder stringProperty(String name, String description) {
        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("type", "string");
        if (description != null) prop.put("description", description);
        properties.set(name, prop);
        return this;
    }

    public SchemaBuilder integerProperty(String name, String description) {
        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("type", "integer");
        if (description != null) prop.put("description", description);
        properties.set(name, prop);
        return this;
    }

    public SchemaBuilder booleanProperty(String name, String description) {
        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("type", "boolean");
        if (description != null) prop.put("description", description);
        properties.set(name, prop);
        return this;
    }

    /**
     * 배열 property — 항목 스키마를 중첩 빌더로 지정.
     *
     * @param name  property 이름
     * @param items 항목 스키마 빌더 (object 루트 기준). 단순 타입이 필요하면
     *              raw JsonNode 경로 사용 권장.
     */
    public SchemaBuilder arrayProperty(String name, SchemaBuilder items) {
        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("type", "array");
        prop.set("items", items.build());
        properties.set(name, prop);
        return this;
    }

    /**
     * 항목 타입이 단순 문자열 배열 — 편의 메서드.
     */
    public SchemaBuilder stringArrayProperty(String name, String description) {
        ObjectNode prop = MAPPER.createObjectNode();
        prop.put("type", "array");
        if (description != null) prop.put("description", description);
        ObjectNode items = MAPPER.createObjectNode();
        items.put("type", "string");
        prop.set("items", items);
        properties.set(name, prop);
        return this;
    }

    /**
     * 중첩 object property.
     */
    public SchemaBuilder objectProperty(String name, SchemaBuilder nested) {
        properties.set(name, nested.build());
        return this;
    }

    /**
     * 필수 property 이름들 추가. 여러 번 호출하면 누적된다.
     */
    public SchemaBuilder required(String... names) {
        if (names != null) required.addAll(Arrays.asList(names));
        return this;
    }

    /**
     * 최종 JSON Schema JsonNode 를 반환. Object 루트 + {@code additionalProperties: false} 자동 포함.
     * 중첩 object 는 자신의 build() 호출 시 재귀적으로 동일 규칙 적용.
     */
    public JsonNode build() {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("type", "object");
        if (description != null) root.put("description", description);
        root.set("properties", properties);
        if (!required.isEmpty()) {
            ArrayNode requiredArray = MAPPER.createArrayNode();
            for (String n : required) requiredArray.add(n);
            root.set("required", requiredArray);
        }
        root.put("additionalProperties", false);
        return root;
    }

    /**
     * {@link ResponseSchema} 로 래핑하여 반환.
     *
     * @param name schema 이름 (OpenAI 식별자)
     */
    public ResponseSchema asResponse(String name) {
        return ResponseSchema.of(name, this.build());
    }
}

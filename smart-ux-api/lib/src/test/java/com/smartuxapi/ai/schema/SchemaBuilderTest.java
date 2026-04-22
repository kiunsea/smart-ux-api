package com.smartuxapi.ai.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SchemaBuilder 단위 테스트")
class SchemaBuilderTest {

    @Test
    @DisplayName("object 루트 + 기본 필드 — type/properties/additionalProperties=false")
    void testBasicObject() {
        JsonNode schema = SchemaBuilder.object()
                .stringProperty("name", "사용자 이름")
                .integerProperty("age", "나이")
                .build();

        assertEquals("object", schema.get("type").asText());
        assertTrue(schema.has("properties"));
        assertEquals("string", schema.get("properties").get("name").get("type").asText());
        assertEquals("integer", schema.get("properties").get("age").get("type").asText());
        assertFalse(schema.get("additionalProperties").asBoolean(),
                "strict 호환을 위해 additionalProperties=false 가 자동 주입되어야 한다");
    }

    @Test
    @DisplayName("required — 중복 제거 및 순서 유지")
    void testRequired() {
        JsonNode schema = SchemaBuilder.object()
                .stringProperty("a", null)
                .stringProperty("b", null)
                .required("a", "b")
                .required("a")
                .build();

        JsonNode req = schema.get("required");
        assertTrue(req.isArray());
        assertEquals(2, req.size(), "중복된 required 항목은 병합되어야 한다");
        assertEquals("a", req.get(0).asText());
        assertEquals("b", req.get(1).asText());
    }

    @Test
    @DisplayName("booleanProperty — description 이 null 이면 description 필드 생략")
    void testBooleanWithoutDescription() {
        JsonNode schema = SchemaBuilder.object()
                .booleanProperty("flag", null)
                .build();
        JsonNode flag = schema.get("properties").get("flag");
        assertEquals("boolean", flag.get("type").asText());
        assertFalse(flag.has("description"));
    }

    @Test
    @DisplayName("stringArrayProperty — items.type=string 자동 구성")
    void testStringArray() {
        JsonNode schema = SchemaBuilder.object()
                .stringArrayProperty("tags", "태그 목록")
                .build();
        JsonNode tags = schema.get("properties").get("tags");
        assertEquals("array", tags.get("type").asText());
        assertEquals("태그 목록", tags.get("description").asText());
        assertEquals("string", tags.get("items").get("type").asText());
    }

    @Test
    @DisplayName("objectProperty — 중첩 object 의 additionalProperties=false 도 주입")
    void testNestedObject() {
        JsonNode schema = SchemaBuilder.object()
                .objectProperty("address",
                        SchemaBuilder.object()
                                .stringProperty("city", null)
                                .required("city"))
                .build();
        JsonNode addr = schema.get("properties").get("address");
        assertEquals("object", addr.get("type").asText());
        assertFalse(addr.get("additionalProperties").asBoolean());
    }

    @Test
    @DisplayName("arrayProperty — items 가 object 인 경우")
    void testArrayOfObjects() {
        JsonNode schema = SchemaBuilder.object()
                .arrayProperty("items",
                        SchemaBuilder.object()
                                .stringProperty("sku", null)
                                .integerProperty("qty", null))
                .build();
        JsonNode items = schema.get("properties").get("items");
        assertEquals("array", items.get("type").asText());
        assertEquals("object", items.get("items").get("type").asText());
        assertEquals("string", items.get("items").get("properties").get("sku").get("type").asText());
    }

    @Test
    @DisplayName("asResponse — ResponseSchema 로 래핑")
    void testAsResponse() {
        ResponseSchema rs = SchemaBuilder.object()
                .stringProperty("x", null)
                .required("x")
                .asResponse("MyShape");

        assertEquals("MyShape", rs.getName());
        assertTrue(rs.isStrict(), "기본 strict=true");
        assertEquals("object", rs.getSchema().get("type").asText());
    }

    @Test
    @DisplayName("description() — root 에 description 주입")
    void testRootDescription() {
        JsonNode schema = SchemaBuilder.object()
                .description("사용자 프로필")
                .stringProperty("name", null)
                .build();
        assertEquals("사용자 프로필", schema.get("description").asText());
    }
}

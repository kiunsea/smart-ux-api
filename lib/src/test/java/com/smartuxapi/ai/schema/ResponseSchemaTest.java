package com.smartuxapi.ai.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResponseSchema 단위 테스트")
class ResponseSchemaTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("of(name, schema) — 기본값 strict=true, description=null")
    void testOfBasic() throws Exception {
        JsonNode node = MAPPER.readTree("{\"type\":\"object\"}");
        ResponseSchema rs = ResponseSchema.of("Foo", node);

        assertEquals("Foo", rs.getName());
        assertNull(rs.getDescription());
        assertTrue(rs.isStrict());
        assertSame(node, rs.getSchema());
    }

    @Test
    @DisplayName("of(name, description, schema, strict) — 전체 인자")
    void testOfFull() throws Exception {
        JsonNode node = MAPPER.readTree("{\"type\":\"object\"}");
        ResponseSchema rs = ResponseSchema.of("Foo", "A shape", node, false);

        assertEquals("A shape", rs.getDescription());
        assertFalse(rs.isStrict());
    }

    @Test
    @DisplayName("name 이 null 또는 빈 문자열이면 IllegalArgumentException")
    void testInvalidName() throws Exception {
        JsonNode node = MAPPER.readTree("{\"type\":\"object\"}");
        assertThrows(IllegalArgumentException.class, () -> ResponseSchema.of(null, node));
        assertThrows(IllegalArgumentException.class, () -> ResponseSchema.of("", node));
    }

    @Test
    @DisplayName("schema 가 null 이면 IllegalArgumentException")
    void testNullSchema() {
        assertThrows(IllegalArgumentException.class, () -> ResponseSchema.of("Foo", null));
    }

    @Test
    @DisplayName("object() 팩토리 — SchemaBuilder 반환")
    void testObjectFactory() {
        SchemaBuilder b = ResponseSchema.object();
        assertNotNull(b);
        JsonNode node = b.stringProperty("x", null).build();
        assertEquals("object", node.get("type").asText());
    }

    @Test
    @DisplayName("toString — 이름과 strict 포함")
    void testToString() throws Exception {
        JsonNode node = MAPPER.readTree("{\"type\":\"object\"}");
        ResponseSchema rs = ResponseSchema.of("Foo", node);
        String s = rs.toString();
        assertTrue(s.contains("Foo"));
        assertTrue(s.contains("strict=true"));
    }
}

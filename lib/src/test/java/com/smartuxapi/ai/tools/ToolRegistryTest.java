package com.smartuxapi.ai.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.smartuxapi.ai.schema.SchemaBuilder;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ToolRegistry/ToolDefinition 단위 테스트")
class ToolRegistryTest {

    private ToolDefinition def(String name) {
        return new ToolDefinition(
                name,
                "desc of " + name,
                SchemaBuilder.object().stringProperty("x", null).build(),
                call -> ToolResult.ok(call.getId(), null));
    }

    @Test
    @DisplayName("register / get / contains / size / isEmpty / all 기본 동작")
    void testBasicOps() {
        ToolRegistry reg = new ToolRegistry();
        assertTrue(reg.isEmpty());
        assertEquals(0, reg.size());

        reg.register(def("foo"));
        reg.register(def("bar"));

        assertEquals(2, reg.size());
        assertFalse(reg.isEmpty());
        assertTrue(reg.contains("foo"));
        assertNotNull(reg.get("foo"));
        assertNull(reg.get("nonexistent"));
        assertEquals(2, reg.all().size());
    }

    @Test
    @DisplayName("등록 순서 유지 — all() 반환 Collection")
    void testInsertionOrder() {
        ToolRegistry reg = new ToolRegistry();
        reg.register(def("c"));
        reg.register(def("a"));
        reg.register(def("b"));

        java.util.Iterator<ToolDefinition> it = reg.all().iterator();
        assertEquals("c", it.next().getName());
        assertEquals("a", it.next().getName());
        assertEquals("b", it.next().getName());
    }

    @Test
    @DisplayName("동일 이름 재등록 시 교체")
    void testReplace() {
        ToolRegistry reg = new ToolRegistry();
        reg.register(def("foo"));
        ToolDefinition old = reg.get("foo");
        reg.register(def("foo"));
        ToolDefinition newDef = reg.get("foo");
        assertNotSame(old, newDef);
        assertEquals(1, reg.size());
    }

    @Test
    @DisplayName("unregister / clear")
    void testRemoval() {
        ToolRegistry reg = new ToolRegistry();
        reg.register(def("foo"));
        reg.register(def("bar"));

        reg.unregister("foo");
        assertFalse(reg.contains("foo"));
        assertEquals(1, reg.size());

        reg.clear();
        assertTrue(reg.isEmpty());
    }

    @Test
    @DisplayName("register(null) / null name / null handler — IllegalArgumentException")
    void testValidation() {
        ToolRegistry reg = new ToolRegistry();
        assertThrows(IllegalArgumentException.class, () -> reg.register(null));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolDefinition(null, "", SchemaBuilder.object().build(), c -> null));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolDefinition("x", "", null, c -> null));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolDefinition("x", "", SchemaBuilder.object().build(), null));
    }
}

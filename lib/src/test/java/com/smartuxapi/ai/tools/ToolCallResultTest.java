package com.smartuxapi.ai.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ToolCall / ToolResult 단위 테스트")
class ToolCallResultTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("ToolCall — 기본 필드")
    void testToolCall() throws Exception {
        ToolCall c = new ToolCall("call_1", "scanImage", MAPPER.readTree("{\"imageUrl\":\"u\"}"));
        assertEquals("call_1", c.getId());
        assertEquals("scanImage", c.getToolName());
        assertEquals("u", c.getArguments().get("imageUrl").asText());
    }

    @Test
    @DisplayName("ToolCall — id/name 빈 값이면 IllegalArgumentException")
    void testToolCallValidation() {
        assertThrows(IllegalArgumentException.class,
                () -> new ToolCall(null, "x", MAPPER.createObjectNode()));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolCall("", "x", MAPPER.createObjectNode()));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolCall("id", null, MAPPER.createObjectNode()));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolCall("id", "", MAPPER.createObjectNode()));
    }

    @Test
    @DisplayName("ToolResult.ok — isError=false, output 유지")
    void testOk() {
        ToolResult r = ToolResult.ok("call_1", TextNode.valueOf("done"));
        assertFalse(r.isError());
        assertEquals("call_1", r.getCallId());
        assertEquals("done", r.getOutput().asText());
        assertNull(r.getErrorMessage());
    }

    @Test
    @DisplayName("ToolResult.error — isError=true, output 에도 에러 메시지 포함")
    void testError() {
        ToolResult r = ToolResult.error("call_1", "boom");
        assertTrue(r.isError());
        assertEquals("boom", r.getErrorMessage());
        assertEquals("boom", r.getOutput().asText());
    }

    @Test
    @DisplayName("ToolResult — callId 필수")
    void testCallIdRequired() {
        assertThrows(IllegalArgumentException.class,
                () -> ToolResult.ok(null, MAPPER.createObjectNode()));
        assertThrows(IllegalArgumentException.class,
                () -> ToolResult.ok("", MAPPER.createObjectNode()));
        assertThrows(IllegalArgumentException.class,
                () -> ToolResult.error(null, "msg"));
    }

    @Test
    @DisplayName("MAX_OUTPUT_BYTES 값 확인 (256KB)")
    void testMaxOutputBytes() {
        assertEquals(256 * 1024, ToolResult.MAX_OUTPUT_BYTES);
    }
}

package com.smartuxapi.ai.tools;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ToolTurnResult 단위 테스트")
class ToolTurnResultTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("finalText — isFinal=true, hasToolCalls=false")
    void testFinal() {
        ToolTurnResult r = ToolTurnResult.finalText("done", "[]");
        assertTrue(r.isFinal());
        assertFalse(r.hasToolCalls());
        assertEquals("done", r.getFinalText());
        assertTrue(r.getToolCalls().isEmpty());
    }

    @Test
    @DisplayName("toolCalls — isFinal=false, hasToolCalls=true, 리스트 불변")
    void testToolCalls() throws Exception {
        ToolCall c = new ToolCall("id1", "foo", MAPPER.createObjectNode());
        ToolTurnResult r = ToolTurnResult.toolCalls(Arrays.asList(c), "raw");
        assertFalse(r.isFinal());
        assertTrue(r.hasToolCalls());
        assertNull(r.getFinalText());
        assertEquals(1, r.getToolCalls().size());
        assertThrows(UnsupportedOperationException.class, () -> r.getToolCalls().add(c));
    }

    @Test
    @DisplayName("rawAssistantPayload — 그대로 보관")
    void testRaw() {
        ToolTurnResult r = ToolTurnResult.finalText("t", "raw-payload");
        assertEquals("raw-payload", r.getRawAssistantPayload());
    }
}

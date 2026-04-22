package com.smartuxapi.ai.gemini;

import org.json.JSONArray;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.schema.SchemaBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * GeminiChatting 의 Structured Output 경로 단위 테스트.
 * 네트워크 호출은 Mockito 로 차단.
 */
@DisplayName("GeminiChatting — Structured Output 단위 테스트")
class GeminiChattingStructuredOutputTest {

    @Test
    @DisplayName("sendPromptWithSchema(msg, null) — sendPrompt 로 위임, structured 키 없음")
    void testNullSchemaDelegates() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(null)))
                .thenReturn("plain text response");

        GeminiChatting chatting = new GeminiChatting(mockConn);
        org.json.simple.JSONObject res = chatting.sendPromptWithSchema("hi", null);

        assertEquals("plain text response", res.get("message"));
        assertFalse(res.containsKey("structured"));
    }

    @Test
    @DisplayName("Schema 지정 — 응답 원문이 JsonNode 로 파싱되어 structured 에 포함")
    void testSchemaPopulatesStructured() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        ResponseSchema schema = SchemaBuilder.object()
                .stringProperty("city", null)
                .required("city")
                .asResponse("Location");

        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(schema)))
                .thenReturn("{\"city\":\"Seoul\"}");

        GeminiChatting chatting = new GeminiChatting(mockConn);
        org.json.simple.JSONObject res = chatting.sendPromptWithSchema("where?", schema);

        JsonNode structured = (JsonNode) res.get("structured");
        assertNotNull(structured);
        assertEquals("Seoul", structured.get("city").asText());
    }

    @Test
    @DisplayName("파싱 실패 — structured=null, 예외 없음")
    void testParseFailureYieldsNull() throws Exception {
        GeminiAPIConnection mockConn = mock(GeminiAPIConnection.class);
        ResponseSchema schema = SchemaBuilder.object().stringProperty("x", null).asResponse("Bad");

        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(schema)))
                .thenReturn("not a json");

        GeminiChatting chatting = new GeminiChatting(mockConn);
        org.json.simple.JSONObject res = chatting.sendPromptWithSchema("x", schema);

        assertTrue(res.containsKey("structured"));
        assertNull(res.get("structured"));
    }
}

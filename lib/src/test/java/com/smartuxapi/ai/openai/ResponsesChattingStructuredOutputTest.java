package com.smartuxapi.ai.openai;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ResponsesChatting 의 Structured Output 경로 단위 테스트.
 * 네트워크 호출은 Mockito 로 차단.
 */
@DisplayName("ResponsesChatting — Structured Output 단위 테스트")
class ResponsesChattingStructuredOutputTest {

    @Test
    @DisplayName("sendPromptWithSchema(msg, null) — sendPrompt 로 위임, structured 키 없음")
    void testNullSchemaDelegates() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(null)))
                .thenReturn("plain text response");

        ResponsesChatting chatting = new ResponsesChatting(mockConn);

        org.json.simple.JSONObject res = chatting.sendPromptWithSchema("hi", null);

        assertEquals("plain text response", res.get("message"));
        assertFalse(res.containsKey("structured"),
                "schema=null 경로는 structured 키를 추가하지 않아야 한다");
    }

    @Test
    @DisplayName("Schema 지정 — 응답 원문이 JsonNode 로 파싱되어 structured 에 포함")
    void testSchemaPopulatesStructured() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ResponseSchema schema = SchemaBuilder.object()
                .stringProperty("name", null)
                .integerProperty("age", null)
                .required("name", "age")
                .asResponse("UserProfile");

        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(schema)))
                .thenReturn("{\"name\":\"Alice\",\"age\":30}");

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        org.json.simple.JSONObject res = chatting.sendPromptWithSchema("who?", schema);

        assertEquals("{\"name\":\"Alice\",\"age\":30}", res.get("message"));
        JsonNode structured = (JsonNode) res.get("structured");
        assertNotNull(structured);
        assertEquals("Alice", structured.get("name").asText());
        assertEquals(30, structured.get("age").asInt());
    }

    @Test
    @DisplayName("파싱 실패 — structured=null, 예외 없음")
    void testParseFailureYieldsNull() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        ResponseSchema schema = SchemaBuilder.object().stringProperty("x", null).asResponse("Bad");

        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(schema)))
                .thenReturn("this is not json");

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        org.json.simple.JSONObject res = chatting.sendPromptWithSchema("x", schema);

        assertEquals("this is not json", res.get("message"));
        assertTrue(res.containsKey("structured"), "schema 지정 시 structured 키는 항상 존재");
        assertNull(res.get("structured"), "파싱 실패 시 null");
    }

    @Test
    @DisplayName("sendPrompt — schema 인자 없이 기존 경로 유지 (structured 키 없음)")
    void testSendPromptBackwardCompat() throws Exception {
        ResponsesAPIConnection mockConn = mock(ResponsesAPIConnection.class);
        when(mockConn.generateContent(any(JSONArray.class), any(CacheStrategy.class), eq(null)))
                .thenReturn("ordinary reply");

        ResponsesChatting chatting = new ResponsesChatting(mockConn);
        org.json.simple.JSONObject res = chatting.sendPrompt("hi");

        assertEquals("ordinary reply", res.get("message"));
        assertFalse(res.containsKey("structured"));
        verify(mockConn, never()).generateContent(any(JSONArray.class));
    }
}

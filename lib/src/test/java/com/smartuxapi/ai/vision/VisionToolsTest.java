package com.smartuxapi.ai.vision;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.tools.ToolCall;
import com.smartuxapi.ai.tools.ToolDefinition;
import com.smartuxapi.ai.tools.ToolResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@DisplayName("VisionTools.scanImageTool 단위 테스트")
class VisionToolsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    @DisplayName("정상 경로 — vision.scanImage 결과가 ToolResult output 으로 반환")
    void testHappyPath() throws Exception {
        VisionService vision = mock(VisionService.class);
        ImageScanInfo info = new ImageScanInfo("https://x/1.png", "button text", 0.9, "gpt-4o");
        when(vision.scanImage("https://x/1.png")).thenReturn(info);

        ToolDefinition def = VisionTools.scanImageTool(vision, null);
        assertEquals("scanImage", def.getName());

        ToolCall call = new ToolCall("c1", "scanImage",
                MAPPER.readTree("{\"imageUrl\":\"https://x/1.png\"}"));
        ToolResult result = def.getHandler().invoke(call);

        assertFalse(result.isError());
        assertEquals("https://x/1.png", result.getOutput().get("imageUrl").asText());
        assertEquals("button text", result.getOutput().get("extractedText").asText());
    }

    @Test
    @DisplayName("ActionQueueHandler 주입 시 addImageScanInfo 자동 호출")
    void testActionQueueInjection() throws Exception {
        VisionService vision = mock(VisionService.class);
        ActionQueueHandler aq = mock(ActionQueueHandler.class);
        ImageScanInfo info = new ImageScanInfo("u", "t", 1.0, null);
        when(vision.scanImage("u")).thenReturn(info);

        ToolDefinition def = VisionTools.scanImageTool(vision, aq);
        ToolCall call = new ToolCall("c", "scanImage", MAPPER.readTree("{\"imageUrl\":\"u\"}"));
        def.getHandler().invoke(call);

        verify(aq).addImageScanInfo(info);
    }

    @Test
    @DisplayName("imageUrl 누락 — error ToolResult")
    void testMissingUrl() throws Exception {
        VisionService vision = mock(VisionService.class);
        ToolDefinition def = VisionTools.scanImageTool(vision, null);

        ToolCall call = new ToolCall("c", "scanImage", MAPPER.readTree("{}"));
        ToolResult r = def.getHandler().invoke(call);
        assertTrue(r.isError());
        assertTrue(r.getErrorMessage().contains("imageUrl"));
    }

    @Test
    @DisplayName("VisionException — error ToolResult 로 변환")
    void testVisionExceptionToError() throws Exception {
        VisionService vision = mock(VisionService.class);
        when(vision.scanImage(any())).thenThrow(new VisionException("boom"));

        ToolDefinition def = VisionTools.scanImageTool(vision, null);
        ToolCall call = new ToolCall("c", "scanImage", MAPPER.readTree("{\"imageUrl\":\"u\"}"));
        ToolResult r = def.getHandler().invoke(call);

        assertTrue(r.isError());
        assertTrue(r.getErrorMessage().contains("VisionException"));
    }

    @Test
    @DisplayName("vision=null — IllegalArgumentException")
    void testNullVision() {
        assertThrows(IllegalArgumentException.class,
                () -> VisionTools.scanImageTool(null, null));
    }
}

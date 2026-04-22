package com.smartuxapi.ai.vision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.schema.SchemaBuilder;
import com.smartuxapi.ai.tools.ToolDefinition;
import com.smartuxapi.ai.tools.ToolResult;

/**
 * Vision 모듈(T1-b) 을 Tool Use(T2-b) 경로에 등록하기 위한 헬퍼.
 *
 * <p>사용 예:
 * <pre>{@code
 *   VisionService vision = VisionServiceFactory.createOpenAI(apiKey);
 *   ToolRegistry tools = new ToolRegistry();
 *   tools.register(VisionTools.scanImageTool(vision, aqHandler));
 *
 *   chatting.sendPromptWithTools(userMsg, tools);
 *   // LLM 이 필요하다고 판단하면 scanImage 자동 호출 → ActionQueueHandler 에 결과 주입
 * }</pre>
 *
 * @since 0.8.0
 */
public final class VisionTools {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private VisionTools() { /* static only */ }

    /**
     * {@code scanImage} Tool 정의 — 이미지 URL 에서 텍스트 추출.
     *
     * <p>ActionQueueHandler 가 null 이 아니면 결과를 자동 주입 (imageUrl 기준 dedupe 는
     * {@link ActionQueueHandler#addImageScanInfo(ImageScanInfo)} 측에서 수행).
     *
     * @param vision VisionService (필수)
     * @param aqHandler 결과 자동 주입 대상 (null 허용)
     */
    public static ToolDefinition scanImageTool(VisionService vision, ActionQueueHandler aqHandler) {
        if (vision == null) throw new IllegalArgumentException("vision is required");

        JsonNode schema = SchemaBuilder.object()
                .stringProperty("imageUrl", "스캔할 이미지의 URL")
                .required("imageUrl")
                .build();

        return new ToolDefinition(
                "scanImage",
                "화면의 이미지 URL 에서 텍스트를 추출한다. alt 가 비어있는 버튼 이미지 등에 사용.",
                schema,
                call -> {
                    JsonNode args = call.getArguments();
                    String url = args == null ? null : args.path("imageUrl").asText(null);
                    if (url == null || url.isEmpty()) {
                        return ToolResult.error(call.getId(), "imageUrl 인자가 비어있음");
                    }
                    try {
                        ImageScanInfo info = vision.scanImage(url);
                        if (aqHandler != null) {
                            aqHandler.addImageScanInfo(info);
                        }
                        JsonNode outputNode = MAPPER.readTree(info.toJSON().toJSONString());
                        return ToolResult.ok(call.getId(), outputNode);
                    } catch (VisionException ve) {
                        return ToolResult.error(call.getId(),
                                "VisionException: " + ve.getMessage());
                    }
                });
    }
}

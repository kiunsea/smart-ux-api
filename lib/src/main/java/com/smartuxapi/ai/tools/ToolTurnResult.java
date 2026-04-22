package com.smartuxapi.ai.tools;

import java.util.Collections;
import java.util.List;

/**
 * Provider 에 대한 한 번의 API 호출 결과. 두 가지 상태 중 하나:
 * <ul>
 *   <li><b>최종 응답</b>: {@link #getFinalText()} 가 non-null. {@link #getToolCalls()} 는 비어있음.</li>
 *   <li><b>Tool 호출 요청</b>: {@link #getToolCalls()} 가 비어있지 않음. {@link #getFinalText()} 는 null.</li>
 * </ul>
 *
 * <p>provider 별 connection 이 응답을 파싱하여 이 객체로 반환하면,
 * 상위 Chatting 의 auto-loop 가 어떤 경로를 택할지 판단한다.
 *
 * @since 0.8.0
 */
public final class ToolTurnResult {

    private final String finalText;
    private final List<ToolCall> toolCalls;
    private final String rawAssistantPayload;

    private ToolTurnResult(String finalText, List<ToolCall> toolCalls, String rawAssistantPayload) {
        this.finalText = finalText;
        this.toolCalls = toolCalls == null ? Collections.emptyList() : Collections.unmodifiableList(toolCalls);
        this.rawAssistantPayload = rawAssistantPayload;
    }

    public static ToolTurnResult finalText(String text, String rawAssistantPayload) {
        return new ToolTurnResult(text, Collections.emptyList(), rawAssistantPayload);
    }

    public static ToolTurnResult toolCalls(List<ToolCall> calls, String rawAssistantPayload) {
        return new ToolTurnResult(null, calls, rawAssistantPayload);
    }

    public String getFinalText() { return finalText; }
    public List<ToolCall> getToolCalls() { return toolCalls; }

    public boolean hasToolCalls() { return !toolCalls.isEmpty(); }
    public boolean isFinal() { return finalText != null; }

    /**
     * provider 가 반환한 원본 assistant payload 의 직렬화 문자열.
     * 대화 히스토리에 저장하여 다음 턴 재전송에 사용할 때 참조.
     */
    public String getRawAssistantPayload() { return rawAssistantPayload; }
}

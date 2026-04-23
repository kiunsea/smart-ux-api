package com.smartuxapi.demo.collector;

import com.smartuxapi.ai.ActionQueueHandler;

/**
 * {@link ActionQueueHandler} 서브클래스 — 원본 동작을 그대로 유지하면서
 * {@link #getCurViewPrompt()} 와 {@link #getActionQueuePrompt(String)} 호출 시점에
 * {@link PromptResponseCollector} 로 데이터를 전달한다.
 *
 * <p>{@code setActionQueueHandler} API 가 {@code ActionQueueHandler} 타입을 요구하므로
 * Decorator 가 아닌 <b>서브클래스</b> 로 구현.
 *
 * @since smuxapi-demo 0.10.0
 */
public class ActionQueueHandlerCollector extends ActionQueueHandler {

    private final PromptResponseCollector collector;

    public ActionQueueHandlerCollector(PromptResponseCollector collector) {
        super();
        this.collector = collector;
    }

    @Override
    public String getCurViewPrompt() {
        String uiInfo = super.getCurViewPrompt();
        // uiInfo 가 null 이면 "변경 없음" 의미 — 저장하지 않음 (이전 턴 값이 유지됨)
        if (uiInfo != null) collector.captureUiInfo(uiInfo);
        return uiInfo;
    }

    @Override
    public String getActionQueuePrompt(String userMsg) {
        String fullPrompt = super.getActionQueuePrompt(userMsg);
        if (fullPrompt != null) {
            String apiPrompt = extractApiPromptTemplate(fullPrompt, userMsg);
            collector.captureApiPrompt(apiPrompt);
        }
        return fullPrompt;
    }

    /**
     * userMsg 부분을 플레이스홀더 {@code {userMsg}} 로 치환하여 템플릿 부분을 반환.
     * userMsg 가 null/empty 거나 fullPrompt 에 포함되지 않으면 원본 그대로 반환.
     */
    static String extractApiPromptTemplate(String fullPrompt, String userMsg) {
        if (fullPrompt == null) return null;
        if (userMsg == null || userMsg.isEmpty()) return fullPrompt;
        int idx = fullPrompt.indexOf(userMsg);
        if (idx < 0) return fullPrompt;
        return fullPrompt.substring(0, idx) + "{userMsg}" + fullPrompt.substring(idx + userMsg.length());
    }
}

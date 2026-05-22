package com.smartuxapi.ai.claude;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Anthropic Messages API 대화 기록 처리 클래스.
 *
 * <p>Anthropic 의 {@code messages} 배열은 {@code {role, content}} 항목으로 구성되며
 * {@code content} 는 문자열 또는 content 블록 배열이다. {@code system} 은 별도 필드이므로
 * 본 클래스는 user/assistant 메시지만 관리한다 (시스템 캐시 프리픽스는
 * {@link ClaudeAPIConnection} 이 처리).
 *
 * @since 0.10.0
 */
public class ConversationHistory {

    private final JSONArray convHistory = new JSONArray();

    /**
     * 사용자 메시지를 대화 기록에 추가하고, API 호출을 위한 전체 기록을 반환합니다.
     *
     * @param userPrompt 사용자 입력 프롬프트 (필수)
     * @param curViewPrompt 현재 화면 프롬프트 (null 허용)
     * @return API에 전송할 전체 대화 기록
     */
    public JSONArray addUserPrompt(String userPrompt, String curViewPrompt) {

        JSONArray rtnValue = new JSONArray(this.convHistory); // 원본을 복사

        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        userContent.put("content", userPrompt);
        this.convHistory.put(userContent);

        if (curViewPrompt != null) {
            JSONObject rtnContent = new JSONObject();
            rtnContent.put("role", "user");
            rtnContent.put("content", userPrompt + ", " + curViewPrompt);
            rtnValue.put(rtnContent);
            return rtnValue;
        }
        return this.convHistory;
    }

    /**
     * 모델의 텍스트 응답을 대화 기록에 추가합니다.
     *
     * @param modelResponse 모델 응답 텍스트
     */
    public void addModelResponse(String modelResponse) {
        JSONObject modelContent = new JSONObject();
        modelContent.put("role", "assistant");
        modelContent.put("content", modelResponse);
        this.convHistory.put(modelContent);
    }

    /**
     * Anthropic 응답의 assistant {@code content[]} 블록 배열을 그대로 히스토리에 추가한다.
     * Tool Use 턴에서 {@code tool_use} 블록이 다음 호출 시 replay 되도록 보존.
     *
     * @param contentBlocks 응답 content 배열 — 각 원소는 provider 규격 그대로
     * @since 0.10.0
     */
    public void addAssistantContentBlocks(JSONArray contentBlocks) {
        if (contentBlocks == null) return;
        JSONObject assistant = new JSONObject();
        assistant.put("role", "assistant");
        assistant.put("content", contentBlocks);
        this.convHistory.put(assistant);
    }

    /**
     * Tool 실행 결과를 {@code tool_result} 블록들로 묶어 user 메시지로 추가한다.
     * Anthropic 은 한 assistant {@code tool_use} 턴의 모든 결과를 단일 user 메시지에 담아야 한다.
     *
     * @param toolResultBlocks 각 원소는 {@code {type:"tool_result", tool_use_id, content}}
     * @since 0.10.0
     */
    public void addToolResults(JSONArray toolResultBlocks) {
        if (toolResultBlocks == null || toolResultBlocks.length() == 0) return;
        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        userContent.put("content", toolResultBlocks);
        this.convHistory.put(userContent);
    }

    /**
     * 대화 기록을 초기화합니다.
     */
    public void clearHistory() {
        this.convHistory.clear();
    }

    /**
     * 현재까지의 메시지 기록을 반환합니다.
     */
    public JSONArray getHistory() {
        return this.convHistory;
    }
}

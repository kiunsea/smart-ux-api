package com.smartuxapi.ai.openai;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Conversation History 처리 클래스<br/>
 * 1. USER와 AI가 주고 받은 모든 메세지를 저장<br/>
 * 2. AI에게 전달할 프롬프트를 작성<br/>
 * 3. UIC DOC 내용을 프롬프트에 추가<br/>
 * 4. 변경된 UI 정보를 Latest 로 저장하고 프롬프트에 추가
 */
public class ConversationHistory {

    private final JSONArray convHistory = new JSONArray();

    /** 캐시 대상 프리픽스 (있으면 매 요청마다 system 메시지로 앞에 붙는다). */
    private String cacheablePrefix = null;

    /**
     * 캐시 대상 프리픽스를 설정한다. 이후 {@link #addUserPrompt(String, String)} 가
     * 반환하는 전체 기록의 맨 앞에 role="system" 메시지로 삽입된다.
     *
     * <p>내부 대화 기록({@code convHistory}) 자체에는 추가되지 않아 중복 누적이 방지된다.
     *
     * @param content 프리픽스 콘텐츠. null 전달 시 프리픽스 해제.
     * @since 0.7.0
     */
    public void setCacheablePrefix(String content) {
        this.cacheablePrefix = content;
    }

    /**
     * 현재 설정된 캐시 프리픽스 (없으면 null).
     *
     * @since 0.7.0
     */
    public String getCacheablePrefix() {
        return cacheablePrefix;
    }

    /**
     * 사용자 메시지를 대화 기록에 추가하고, AI API 호출을 위한 전체 기록을 반환합니다.
     *
     * @param userPrompt 사용자 입력 프롬프트 (필수)
     * @param curViewPrompt 현재 화면 프롬프트 (필수 아님, null available)
     * @return AI API에 전송할 전체 대화 기록 (캐시 프리픽스가 설정된 경우 맨 앞에 포함됨)
     */
    public JSONArray addUserPrompt(String userPrompt, String curViewPrompt) {

        JSONArray rtnValue = new JSONArray(this.convHistory); // 원본을 복사

        // 사용자 메시지 JSON 객체 생성
        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        userContent.put("content", userPrompt);
        convHistory.put(userContent);

        JSONArray base;
        if (curViewPrompt != null) {
            // 반환용 JSONArray 작성
            JSONObject rtnContent = new JSONObject();
            rtnContent.put("role", "user");
            rtnContent.put("content", userPrompt + ", " + curViewPrompt);
            rtnValue.put(rtnContent);
            base = rtnValue;
        } else {
            base = this.convHistory;
        }

        // 캐시 프리픽스가 있으면 맨 앞에 system 메시지로 추가 (별도 배열 반환, 내부 상태 불변)
        if (this.cacheablePrefix != null && !this.cacheablePrefix.isEmpty()) {
            JSONArray withPrefix = new JSONArray();
            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", this.cacheablePrefix);
            withPrefix.put(systemMsg);
            for (int i = 0; i < base.length(); i++) {
                withPrefix.put(base.get(i));
            }
            return withPrefix;
        }

        return base;
    }

    /**
     * AI 모델의 응답을 대화 기록에 추가합니다.
     * 
     * @param modelResponse AI 모델의 응답 텍스트
     */
    public void addModelResponse(String modelResponse) {

        // 모델 응답 JSON 객체 생성
        JSONObject modelContent = new JSONObject();
        modelContent.put("role", "assistant");
        modelContent.put("content", modelResponse);

        convHistory.put(modelContent);
        // chatSessions 맵에 이미 참조가 있으므로 별도로 put할 필요는 없음
    }

    /**
     * 대화 기록을 초기화합니다.
     * 
     * @param sessionId 초기화할 세션 ID
     */
    public void clearHistory() {
        convHistory.clear();
        System.out.println("History cleared.");
    }

    /**
     * 현재까지의 메세지 기록을 가져옵니다. (디버깅용)
     * 
     * @param sessionId
     * @return
     */
    public JSONArray getHistory() {
        return convHistory;
    }
}

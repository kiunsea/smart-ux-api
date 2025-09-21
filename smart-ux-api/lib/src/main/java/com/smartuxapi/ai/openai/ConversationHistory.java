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

    /**
     * 사용자 메시지를 대화 기록에 추가하고, AI API 호출을 위한 전체 기록을 반환합니다.
     * 
     * @param userPrompt 사용자 입력 프롬프트 (필수)
     * @param curViewPrompt 현재 화면 프롬프트 (필수 아님, null available)
     * @return AI API에 전송할 전체 대화 기록
     */
    public JSONArray addUserPrompt(String userPrompt, String curViewPrompt) {

        JSONArray rtnValue = new JSONArray(this.convHistory); // 원본을 복사

        // 사용자 메시지 JSON 객체 생성
        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        userContent.put("content", userPrompt);
        convHistory.put(userContent);

        if (curViewPrompt != null) {
            // 반환용 JSONArray 작성
            JSONObject rtnContent = new JSONObject();
            rtnContent.put("role", "user");
            rtnContent.put("content", userPrompt + ", " + curViewPrompt);
            rtnValue.put(rtnContent);
            return rtnValue;
        } else {
            return this.convHistory;
        }
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

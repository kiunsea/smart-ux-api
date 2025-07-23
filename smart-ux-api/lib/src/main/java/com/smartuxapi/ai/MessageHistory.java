package com.smartuxapi.ai;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Message History 처리 클래스<br/>
 * 1. USER와 AI가 주고 받은 모든 메세지를 저장<br/>
 * 2. AI에게 전달할 프롬프트를 작성<br/>
 * 3. UIC DOC 내용을 프롬프트에 추가<br/>
 * 4. 변경된 UI 정보를 Latest 로 저장하고 프롬프트에 추가
 */
public class MessageHistory {

    private final List<JSONObject> messageHistory = new ArrayList<>();

    /**
     * 사용자 메시지를 대화 기록에 추가하고, AI API 호출을 위한 전체 기록을 반환합니다.
     * 
     * @param userMessage 사용자 입력 메시지
     * @return AI API에 전송할 전체 대화 기록
     */
    public List<JSONObject> addUserMessage(String userMessage) {

        // 사용자 메시지 JSON 객체 생성
        JSONObject userContent = new JSONObject();
        userContent.put("role", "user");
        JSONArray userParts = new JSONArray();
        userParts.put(new JSONObject().put("text", userMessage));
        userContent.put("parts", userParts);

        messageHistory.add(userContent);
        return messageHistory; // 이 시점에서 AI에 보낼 전체 기록을 반환
    }

    /**
     * AI 모델의 응답을 대화 기록에 추가합니다.
     * 
     * @param modelResponse AI 모델의 응답 텍스트
     */
    public void addModelResponse(String modelResponse) {

        // 모델 응답 JSON 객체 생성
        JSONObject modelContent = new JSONObject();
        modelContent.put("role", "model");
        JSONArray modelParts = new JSONArray();
        modelParts.put(new JSONObject().put("text", modelResponse));
        modelContent.put("parts", modelParts);

        messageHistory.add(modelContent);
        // chatSessions 맵에 이미 참조가 있으므로 별도로 put할 필요는 없음
    }

    /**
     * 대화 기록을 초기화합니다.
     * 
     * @param sessionId 초기화할 세션 ID
     */
    public void clearHistory() {
        messageHistory.clear();
        System.out.println("History cleared.");
    }

    /**
     * 현재까지의 메세지 기록을 가져옵니다. (디버깅용)
     * 
     * @param sessionId
     * @return
     */
    public List<JSONObject> getHistory() {
        return messageHistory;
    }
}

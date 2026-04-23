package com.smartuxapi.demo.collector;

/**
 * Full Scenario Test Case 의 한 턴 데이터.
 *
 * <p>필드는 full-scenario-test-plan.md §2.1 에 정의됨.
 *
 * @since smuxapi-demo 0.10.0
 */
public class ScenarioTurn {

    private final int turnNo;
    private String uiInfo;
    private String userPrompt;
    private String apiPrompt;
    private String resMsg;
    private Object actionQueue;  // JsonNode or JSONObject — provider 응답 그대로

    public ScenarioTurn(int turnNo) {
        this.turnNo = turnNo;
    }

    public int getTurnNo() { return turnNo; }

    public String getUiInfo() { return uiInfo; }
    public void setUiInfo(String uiInfo) { this.uiInfo = uiInfo; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public String getApiPrompt() { return apiPrompt; }
    public void setApiPrompt(String apiPrompt) { this.apiPrompt = apiPrompt; }

    public String getResMsg() { return resMsg; }
    public void setResMsg(String resMsg) { this.resMsg = resMsg; }

    public Object getActionQueue() { return actionQueue; }
    public void setActionQueue(Object actionQueue) { this.actionQueue = actionQueue; }
}

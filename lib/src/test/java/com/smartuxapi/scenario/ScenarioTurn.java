package com.smartuxapi.scenario;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * smuxapi-demo 의 Scenario Collector 가 저장한 JSON 의 단일 턴.
 *
 * <p>필드 이름은 {@code com.smartuxapi.demo.collector.ScenarioTurn} 의 직렬화 결과와 호환:
 * {@code turnNo, uiInfo, userPrompt, apiPrompt, resMsg, actionQueue}.
 *
 * @since lib 0.9.5
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioTurn {

    private int turnNo;
    private String uiInfo;
    private String userPrompt;
    private String apiPrompt;
    private String resMsg;
    private JsonNode actionQueue;

    public int getTurnNo() { return turnNo; }
    public void setTurnNo(int turnNo) { this.turnNo = turnNo; }

    public String getUiInfo() { return uiInfo; }
    public void setUiInfo(String uiInfo) { this.uiInfo = uiInfo; }

    public String getUserPrompt() { return userPrompt; }
    public void setUserPrompt(String userPrompt) { this.userPrompt = userPrompt; }

    public String getApiPrompt() { return apiPrompt; }
    public void setApiPrompt(String apiPrompt) { this.apiPrompt = apiPrompt; }

    public String getResMsg() { return resMsg; }
    public void setResMsg(String resMsg) { this.resMsg = resMsg; }

    public JsonNode getActionQueue() { return actionQueue; }
    public void setActionQueue(JsonNode actionQueue) { this.actionQueue = actionQueue; }
}

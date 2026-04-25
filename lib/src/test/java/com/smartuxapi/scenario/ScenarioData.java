package com.smartuxapi.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 시나리오 JSON 파일 전체.
 *
 * <p>대응 포맷 (smuxapi-demo Scenario Collector v0.10.0+):
 * <pre>{
 *   "schemaVersion": 1,
 *   "sessionId": "...",
 *   "aiModel": "chatgpt|gemini",
 *   "savedAt": "...",
 *   "turnCount": N,
 *   "turns": [ {turnNo, uiInfo, userPrompt, apiPrompt, resMsg, actionQueue}, ... ]
 * }</pre>
 *
 * @since lib 0.9.5
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScenarioData {

    private int schemaVersion = 1;
    private String sessionId;
    private String aiModel;
    private String savedAt;
    private int turnCount;
    private List<ScenarioTurn> turns = new ArrayList<>();

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getAiModel() { return aiModel; }
    public void setAiModel(String aiModel) { this.aiModel = aiModel; }

    public String getSavedAt() { return savedAt; }
    public void setSavedAt(String savedAt) { this.savedAt = savedAt; }

    public int getTurnCount() { return turnCount; }
    public void setTurnCount(int turnCount) { this.turnCount = turnCount; }

    public List<ScenarioTurn> getTurns() {
        return turns == null ? Collections.emptyList() : turns;
    }
    public void setTurns(List<ScenarioTurn> turns) { this.turns = turns; }

    public int turnSize() {
        return turns == null ? 0 : turns.size();
    }
}

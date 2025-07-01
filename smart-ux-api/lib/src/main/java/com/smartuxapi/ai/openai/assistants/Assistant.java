package com.smartuxapi.ai.openai.assistants;

/**
 * 접속 정보를 관리한다.<br/>
 */
public class Assistant {

    private String apiKey = null;
    private String assistantId = null;
    
	public Assistant(String id) {
		this.assistantId = id;
	}
	
    public String getAssistantId() {
		return this.assistantId;
	}
	public String getApiKey() {
		return this.apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
}

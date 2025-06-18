package com.omnibuscode.ai.openai.assistants;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.omnibuscode.ai.ProcessFunction;

/**
 * 접속 정보를 관리한다.<br/>
 */
public class OpenAIAssistant {

    private String apiKey = null;
    private String assistantId = null;
    
	private Map<String, ProcessFunction> usrFuncs = new HashMap<String, ProcessFunction>(); //function call 수행을 위한 사용자 정의 인스턴스
	
	public OpenAIAssistant(String id) {
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
    
	/**
	 * user function 을 Assistant 에 등록한다.<br/>
	 * assistants/tools/functions 목록을 확인하고 신규 추가 또는 기존 내용을 수정한다.
	 * @param funcName
	 */
	public void putFunction(String funcName, ProcessFunction usrFunc) {
		
		JSONObject usrFuncJson = usrFunc.getFunctionJson();
		// TODO user function name 을 assistant 에서 조회하고 이미 있는 경우는 overwrite 할지 확인후 user function 의 내용을 등록하게 해야 한다.
		// usrFuncJson 이 null 이 아니면 usrFuncJson 내용을 assistant 에 반영해야 함
		if (usrFuncJson != null) {
			
		}
		
		this.usrFuncs.put(funcName, usrFunc);
	}
	
	public Map getFunctions() {
		return this.usrFuncs;
	}
}

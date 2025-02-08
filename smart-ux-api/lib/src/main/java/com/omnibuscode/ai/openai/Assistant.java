package com.omnibuscode.ai.openai;

import java.util.HashMap;
import java.util.Map;

import com.omnibuscode.ai.UserFunction;

/**
 * 접속 정보를 관리한다.<br/>
 */
public class Assistant {

    private String apiKey = null;
    private String assistantId = null;
    
	private Map<String, UserFunction> usrFuncs = new HashMap<String, UserFunction>(); //function call 수행을 위한 사용자 정의 인스턴스
	
    public String getAssistantId() {
		return this.assistantId;
	}
	public void setAssistantId(String id) {
		this.assistantId = id;
	}
	public String getApiKey() {
		return this.apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
    
	/**
	 * user function 을 등록
	 * @param funcName
	 */
	public void putFunction(String funcName, UserFunction usrFunc) {
		this.usrFuncs.put(funcName, usrFunc);
	}
	
	public Map getFunctions() {
		return this.usrFuncs;
	}
}

package com.omnibuscode.ai.openai;

import java.util.HashMap;
import java.util.Map;

import com.omnibuscode.ai.UserFunction;

/**
 * 접속 정보를 관리한다.<br/>
 */
public class Assistant {

	//TODO id와 key는 사용자가 소유해야하고 api에서의 기본값은 null 로 초기화 되어 있어야 한다. (테스트이므로 임시로 설정해놓음)
    private String apiKey = "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";
    private String assistantId = "asst_hsP6560JM3JiFi0HlU4gR8hZ";
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

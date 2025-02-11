package com.omnibuscode.ai.openai.connection;

import java.util.HashMap;
import java.util.Map;

import com.omnibuscode.ai.openai.Assistant;

/**
 * Assistant 를 관리하고 openai api에 연결한다.<br/>
 * 2025.02.10 현재는 미사용으로 곧 file search 와 functions 를 사용해야 해서 이 클래스를 완성해야 한다.
 */
public class AssistantConnection {

	private Map<String, Assistant> assistList = new HashMap<String, Assistant>();

	public void setAssistant(String id, Assistant assist) {
		this.assistList.put(id, assist);
	}
	
	public Assistant getAssistant(String id) {
		return this.assistList.get(id);
	}
	
	public boolean createAssistant() {
		//TODO
		return false;
	}
	
	public boolean deleteAssistant(String id) {
		//TODO
		return false;
	}
}

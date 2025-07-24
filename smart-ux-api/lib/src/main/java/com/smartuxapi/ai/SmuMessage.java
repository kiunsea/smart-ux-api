package com.smartuxapi.ai;

import java.util.Set;

import org.json.simple.JSONObject;

/**
 * prompt message 를 전송하고 응답받는다.
 */
public interface SmuMessage {
    
    public void setActionQueueHandler(ActionQueueHandler aqHandler);
    
	/**
	 * 입력한 사용자 메세지를 thread 에 추가하고 run 한다
	 * 
	 * @param ActionQueueHandler
	 * @param userMsg
	 * @return {"message":String, "action_queue":JSON String, "userFunctionsResult":JSON String}
	 * @throws Exception
	 */
	public JSONObject sendPrompt(String userMsg) throws Exception;
	
	/**
	 * 보유하고 있는 message id set 을 반환
	 * @return
	 */
	public Set<String> getMessageIdSet();
	
}

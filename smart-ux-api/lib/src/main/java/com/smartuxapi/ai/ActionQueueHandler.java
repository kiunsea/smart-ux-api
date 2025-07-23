package com.smartuxapi.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.util.ActionQueueUtil;

/**
 * Action Queue 처리 클래스<br/>
 * 1. 현재 화면 정보를 저장 관리<br/>
 * 2. Action Queue 요청 메세지 작성<br/>
 * 3. 응답 메세지로부터 Action Queue 추출<br/>
 */
public class ActionQueueHandler {

    private Logger log = LogManager.getLogger(ActionQueueHandler.class);
    private String curViewInfo = null;
    
    /**
     * 현재 화면 정보 저장
     * @param curViewInfo(json string)
     */
    public void setCurViewInfo(String curViewInfo) {
        this.curViewInfo = curViewInfo;
        log.debug("현재 화면 정보 저장 : " + curViewInfo);
    }
    
    public String getCurViewInfo() {
        return this.curViewInfo;
    }
    
    public JsonNode getActionQueue(String resMsg) {
        JsonNode aqObj = ActionQueueUtil.extractActionQueue(resMsg);
        if (aqObj.hasNonNull("actionQueue")) {
            return aqObj.get("actionQueue");
        } else {
            return aqObj;
        }
    }
    
    /**
     * 사용자 요청에 현재 화면 정보에 대한 Action Queue 생성 요청을 덧붙인다.<br/>
     * > 현재 화면 정보가 없다면 일반 Prompt 로 동작한다.
     * 
     * @param User Message
     * @return Action Queue Prompt
     */
    public String decoratePrompt(String userMsg) {
        
        if (this.curViewInfo == null) //현재 화면 정보가 없다면 일반 Prompt 로 동작
            return userMsg;
        
        StringBuffer aqPromptSb = new StringBuffer();
        JsonNode config = ConfigLoader.loadConfigFromClasspath();
        
        Map<String, String> valueMap = new HashMap<>();
        valueMap.put("CurViewInfo", this.curViewInfo);
        valueMap.put("UserMsg", userMsg);
        StrSubstitutor sub = new StrSubstitutor(valueMap);
        
        Iterator<JsonNode> elements = null;
        if (config.get("prompt").get("cur_view_info").isArray()) {
            elements = config.get("prompt").get("cur_view_info").elements();
            while (elements.hasNext()) {
                JsonNode elementNode = elements.next();
                aqPromptSb.append(" " + sub.replace(elementNode));
            }
        }
        if (config.get("prompt").get("cur_view_info").isArray()) {
            elements = config.get("prompt").get("action_queue").elements();
            while (elements.hasNext()) {
                JsonNode elementNode = elements.next();
                aqPromptSb.append(" " + sub.replace(elementNode));
            }
        }
        
        log.debug("Action Queue Prompt : " + aqPromptSb);
        
        return aqPromptSb.toString();
    }
    
}

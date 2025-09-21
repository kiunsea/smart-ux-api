package com.smartuxapi.ai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.smartuxapi.util.ActionQueueUtil;
import com.smartuxapi.util.JSONUtil;

/**
 * Action Queue 처리 클래스<br/>
 * 1. 현재 화면 정보를 저장 관리<br/>
 * 2. Action Queue 요청 메세지 작성<br/>
 * 3. 응답 메세지로부터 Action Queue 추출<br/>
 */
public class ActionQueueHandler {

    public static String FORMAT_HTML = "HTML";
    public static String FORMAT_NEXACRO = "NEXACRO";
    
    private Logger log = LogManager.getLogger(ActionQueueHandler.class);
    private String format = null;
    private JsonNode config = null;
    private String curViewInfo = null;
    
    /**
     * 기본 생성자
     * HTML Format 에 대해서 Action Queue 를 처리한다.
     */
    public ActionQueueHandler() {
        this.format = FORMAT_HTML;
        this.config = ConfigLoader.loadConfigFromClasspath(); //기본 config 로 동작
    }
    
    /**
     * 사용자 생성자
     * 사용자가 지정한 Format 에 대해 사용자가 입력한 Config Prompt 로 Action Queue 를 처리한다.
     * @param format
     * @param confFileName
     */
    public ActionQueueHandler(String format, String confFileName) {
        this.format = format;
        this.config = ConfigLoader.loadConfigFromClasspath(confFileName);
    }
    
    /**
     * 현재 화면 정보 저장
     * @param format (ActionQueueHandler에서 지원하는 UI Format)
     * @param curViewInfo
     * @throws ParseException
     */
    public void setCurrentViewInfo(String curViewInfo) throws ParseException {
        
        if (curViewInfo != null) {
            boolean isJsonArray = false;
            char firstChar = curViewInfo.trim().charAt(0);
            if (firstChar == '[') {
                isJsonArray = true;
            }
            
            JSONObject viewInfoJson = null;
            if (isJsonArray) {
                viewInfoJson = new JSONObject();
                JSONArray viewInfoJarray = JSONUtil.parseJsonArray(curViewInfo);
                viewInfoJson.put("viewInfo", viewInfoJarray);
            } else {
                viewInfoJson = JSONUtil.parseJSONObject(curViewInfo);
            }
            viewInfoJson.put("format", this.format);
            this.curViewInfo = viewInfoJson.toJSONString();
            
            log.debug("현재 화면 정보 저장 : " + this.curViewInfo);
        } else {
            this.curViewInfo = null;
        }
    }

    /**
     * 현재 화면 정보가 저장되어 있는지 확인
     * 
     * @return 화면 정보 저장 여부
     */
    public boolean isCurrentViewInfo() {
        return (this.curViewInfo != null && this.curViewInfo.length() > 0);
    }
    
    /**
     * 현재 화면 정보 설정에 대한 프롬프트를 반환
     * @return Current View Prompt
     */
    public String getCurViewPrompt() {
        StringBuffer aqPromptSb = new StringBuffer();
        Map<String, String> valueMap = new HashMap<>();

        if (this.curViewInfo == null) {// 현재 화면 정보가 없다면 prompt 생성 취소
            return null;
        } else {// 현재 화면 정보가 있다면 Prompt 추가

            valueMap.put("CurViewInfo", this.curViewInfo);
            StrSubstitutor sub = new StrSubstitutor(valueMap);

            Iterator<JsonNode> elements = null;
            if (config.get("prompt").get("cur_view_info").isArray()) {
                elements = config.get("prompt").get("cur_view_info").elements();
                while (elements.hasNext()) {
                    JsonNode elementNode = elements.next();
                    aqPromptSb.append(" " + sub.replace(elementNode));
                }
            }
        }

        log.debug("Current View Prompt : " + aqPromptSb);
        return aqPromptSb.toString();
    }
    
    /**
     * 현재 화면 정보에 대한 Action Queue 생성 요청 Prompt 를 반환.<br/>
     * 현재 화면 정보(CurViewInfo)가 필수로 저장되어 있어야 한다.
     * @return Action Queue Prompt
     */
    public String getActionQueuePrompt(String userMsg) {

        StringBuffer aqPromptSb = new StringBuffer();
        Map<String, String> valueMap = new HashMap<>();

        if (this.curViewInfo == null) {// 현재 화면 정보가 없다면 prompt 생성 취소
            return null;
        } else {// 현재 화면 정보가 있다면 Prompt 추가
            valueMap.put("UserMsg", userMsg);
            StrSubstitutor sub = new StrSubstitutor(valueMap);

            Iterator<JsonNode> elements = null;
            if (config.get("prompt").get("action_queue").isArray()) {
                elements = config.get("prompt").get("action_queue").elements();
                while (elements.hasNext()) {
                    JsonNode elementNode = elements.next();
                    aqPromptSb.append(" " + sub.replace(elementNode));
                }
            }
        }

        log.debug("Action Queue Prompt : " + aqPromptSb);
        return aqPromptSb.toString();
    }
//    public String getActionQueuePrompt(String userMsg) {
//        
//        StringBuffer aqPromptSb = new StringBuffer();
//        Map<String, String> valueMap = new HashMap<>();
//        
//        if (this.curViewInfo == null) {// 현재 화면 정보가 없다면 일반 Prompt 로 동작
//            
//            valueMap.put("UserMsg", userMsg);
//            StrSubstitutor sub = new StrSubstitutor(valueMap);
//            
//            Iterator<JsonNode> elements = null;
//            if (this.config.get("prompt").get("normal_prompt").isArray()) {
//                elements = this.config.get("prompt").get("normal_prompt").elements();
//                while (elements.hasNext()) {
//                    JsonNode elementNode = elements.next();
//                    aqPromptSb.append(" " + sub.replace(elementNode));
//                }
//            }
//            
//        } else {// 현재 화면 정보가 있다면 Prompt 추가
//            
//            valueMap.put("CurViewInfo", this.curViewInfo);
//            valueMap.put("UserMsg", userMsg);
//            StrSubstitutor sub = new StrSubstitutor(valueMap);
//            
//            Iterator<JsonNode> elements = null;
//            if (config.get("prompt").get("cur_view_info").isArray()) {
//                elements = config.get("prompt").get("cur_view_info").elements();
//                while (elements.hasNext()) {
//                    JsonNode elementNode = elements.next();
//                    aqPromptSb.append(" " + sub.replace(elementNode));
//                }
//            }
//            if (config.get("prompt").get("action_queue").isArray()) {
//                elements = config.get("prompt").get("action_queue").elements();
//                while (elements.hasNext()) {
//                    JsonNode elementNode = elements.next();
//                    aqPromptSb.append(" " + sub.replace(elementNode));
//                }
//            }
//        }
//        
//        log.debug("Action Queue Prompt : " + aqPromptSb);
//        return aqPromptSb.toString();
//    }
    
    public JsonNode getActionQueue(String resMsg) {
        JsonNode aqObj = ActionQueueUtil.extractActionQueue(resMsg);
        if (aqObj != null && aqObj.hasNonNull("action_queue")) {
            return aqObj.get("action_queue");
        } else {
            return aqObj;
        }
    }
    
    public void clearCurrentViewInfo() {
        this.curViewInfo = null;
    }
}

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

import com.fasterxml.jackson.databind.JsonNode;
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
    
    private Logger log = LogManager.getLogger(ActionQueueHandler.class);
    private String format = null;
    private JsonNode config = null;
    private JSONObject curViewInfo = null;
    private JSONObject lastSentViewInfo = null; // 마지막으로 전송된 화면 정보
    private boolean viewInfoChanged = false; // 화면 정보 변경 여부 플래그
    
    /**
     * 기본 생성자
     * HTML Format 에 대해서 Action Queue 를 처리한다.
     */
    public ActionQueueHandler() {
        this.format = FORMAT_HTML;
        this.config = ConfigLoader.loadConfigFromClasspath(); //기본 config 로 동작
    }
    
    /**
     * 사용자 생성자 사용자가 지정한 Format 에 대해 사용자가 입력한 Config Prompt 로 Action Queue 를 처리한다.
     * 
     * @param formatUi
     * @param configPrompt
     */
    public ActionQueueHandler(String formatUi, JsonNode configPrompt) {
        this.format = formatUi;
        this.config = configPrompt;
    }
    
    /**
     * 화면 정보가 변경되었는지 확인
     * 
     * @param newViewInfo 새로운 화면 정보
     * @return 변경 여부
     */
    private boolean hasViewInfoChanged(JSONObject newViewInfo) {
        if (newViewInfo == null) {
            return (this.lastSentViewInfo != null);
        }
        
        if (this.lastSentViewInfo == null) {
            return true; // 이전에 전송된 정보가 없으면 변경된 것으로 간주
        }
        
        // JSON 문자열로 비교하여 변경 여부 확인
        String newViewInfoStr = newViewInfo.toJSONString();
        String lastSentViewInfoStr = this.lastSentViewInfo.toJSONString();
        
        boolean changed = !newViewInfoStr.equals(lastSentViewInfoStr);
        
        if (changed) {
            log.debug("화면 정보 변경 감지됨");
        }
        
        return changed;
    }
    
    /**
     * 현재 화면 정보 저장
     * 
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
            
            // 화면 정보 변경 여부 확인
            this.viewInfoChanged = hasViewInfoChanged(viewInfoJson);
            
            this.curViewInfo = viewInfoJson;
            
            if (this.viewInfoChanged) {
                log.debug("현재 화면 정보 저장 (변경됨) : " + this.curViewInfo.toJSONString());
            } else {
                log.debug("현재 화면 정보 저장 (변경 없음) : " + this.curViewInfo.toJSONString());
            }
        } else {
            this.curViewInfo = null;
            this.viewInfoChanged = (this.lastSentViewInfo != null);
        }
    }

    /**
     * 현재 화면 정보에 추가 정보를 병합
     * 
     * @param additionalViewInfo 추가할 화면 정보 (JsonNode)
     */
    public void addCurrentViewInfo(JsonNode additionalViewInfo) {
        if (additionalViewInfo == null) {
            log.warn("추가할 화면 정보가 null입니다.");
            return;
        }
        
        try {
            JSONObject additionalJson = JSONUtil.jsonNodeToJSONObject(additionalViewInfo);
            if (additionalJson == null) {
                log.error("JsonNode를 JSONObject로 변환하는데 실패했습니다.");
                return;
            }
            
            if (this.curViewInfo == null) {
                // 기존 화면 정보가 없으면 새로 생성
                this.curViewInfo = new JSONObject();
                this.curViewInfo.put("format", this.format);
            }
            
            // 기존 정보에 추가 정보 병합
            @SuppressWarnings("unchecked")
            java.util.Set<String> keys = additionalJson.keySet();
            for (String key : keys) {
                // format 필드는 덮어쓰지 않음
                if (!"format".equals(key)) {
                    this.curViewInfo.put(key, additionalJson.get(key));
                }
            }
            
            // 화면 정보 변경 여부 확인
            this.viewInfoChanged = hasViewInfoChanged(this.curViewInfo);
            
            if (this.viewInfoChanged) {
                log.debug("현재 화면 정보에 추가 정보 병합 완료 (변경됨) : " + this.curViewInfo.toJSONString());
            } else {
                log.debug("현재 화면 정보에 추가 정보 병합 완료 (변경 없음) : " + this.curViewInfo.toJSONString());
            }
        } catch (Exception e) {
            log.error("화면 정보 추가 중 오류 발생", e);
        }
    }

    /**
     * 현재 화면 정보가 저장되어 있는지 확인
     * 
     * @return 화면 정보 저장 여부
     */
    public boolean isCurrentViewInfo() {
        return (this.curViewInfo != null);
    }
    
    /**
     * 현재 화면 정보 설정에 대한 프롬프트를 반환
     * 화면 정보가 변경되었을 때만 프롬프트를 반환합니다.
     * @return Current View Prompt (변경되지 않았으면 null)
     */
    public String getCurViewPrompt() {
        // 화면 정보가 없거나 변경되지 않았으면 null 반환
        if (this.curViewInfo == null || !this.viewInfoChanged) {
            if (this.curViewInfo == null) {
                log.debug("현재 화면 정보가 없어 프롬프트를 생성하지 않습니다.");
            } else {
                log.debug("화면 정보가 변경되지 않아 프롬프트를 생성하지 않습니다.");
            }
            return null;
        }
        
        StringBuffer aqPromptSb = new StringBuffer();
        Map<String, String> valueMap = new HashMap<>();

        valueMap.put("CurViewInfo", this.curViewInfo.toJSONString());
        StrSubstitutor sub = new StrSubstitutor(valueMap);

        Iterator<JsonNode> elements = null;
        if (config.get("prompt").get("cur_view_info").isArray()) {
            elements = config.get("prompt").get("cur_view_info").elements();
            while (elements.hasNext()) {
                JsonNode elementNode = elements.next();
                aqPromptSb.append(" " + sub.replace(elementNode));
            }
        }

        log.debug("Current View Prompt (변경 감지됨) : " + aqPromptSb);
        return aqPromptSb.toString();
    }
    
    /**
     * 프롬프트 전송 후 호출하여 마지막 전송된 화면 정보를 업데이트합니다.
     * 화면 정보가 변경되어 프롬프트에 포함된 경우에만 호출해야 합니다.
     */
    public void markViewInfoAsSent() {
        if (this.curViewInfo != null && this.viewInfoChanged) {
            try {
                // 깊은 복사로 마지막 전송된 정보 저장
                this.lastSentViewInfo = JSONUtil.parseJSONObject(this.curViewInfo.toJSONString());
                this.viewInfoChanged = false;
                log.debug("화면 정보 전송 완료로 표시됨");
            } catch (ParseException e) {
                log.error("마지막 전송된 화면 정보 업데이트 실패", e);
            }
        }
    }
    
    /**
     * 화면 정보 변경 여부를 확인합니다.
     * @return 변경 여부
     */
    public boolean isViewInfoChanged() {
        return this.viewInfoChanged;
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
        this.lastSentViewInfo = null;
        this.viewInfoChanged = false;
    }
}

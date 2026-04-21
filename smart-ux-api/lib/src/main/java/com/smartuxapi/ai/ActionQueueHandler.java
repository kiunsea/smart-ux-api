package com.smartuxapi.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.vision.ImageScanInfo;
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

    /** 이미지 스캔 결과 — imageUrl 을 키로 하여 중복 주입 방지 (dedupe). 삽입 순서 유지. */
    private final Map<String, ImageScanInfo> imageScanInfoMap = new LinkedHashMap<>();
    
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
        return (this.curViewInfo != null) || !this.imageScanInfoMap.isEmpty();
    }

    // ========== 이미지 스캔 정보 (Vision API 통합) ==========

    /**
     * 이미지 스캔 정보를 추가한다. 동일 {@code imageUrl} 로 이미 주입된 정보가 있으면 최신값으로 대체
     * (dedupe — 설계 스케치 §11.2).
     *
     * <p>추가 시 {@code viewInfoChanged} 플래그를 설정하여 다음 프롬프트 빌드에 반영되도록 한다.
     *
     * @param scanInfo 스캔 결과 (null 이면 무시)
     * @since 0.7.0
     */
    public void addImageScanInfo(ImageScanInfo scanInfo) {
        if (scanInfo == null) return;
        String key = scanInfo.getImageUrl();
        ImageScanInfo prev = this.imageScanInfoMap.put(key, scanInfo);
        this.viewInfoChanged = true;
        if (prev != null) {
            log.debug("이미지 스캔 정보 교체 (dedupe): {}", key);
        } else {
            log.debug("이미지 스캔 정보 추가: {}", scanInfo);
        }
    }

    /**
     * 이미지 스캔 정보 리스트를 한 번에 추가 (각 항목은 dedupe 적용).
     *
     * @since 0.7.0
     */
    public void addImageScanInfoList(List<ImageScanInfo> scanInfoList) {
        if (scanInfoList == null || scanInfoList.isEmpty()) return;
        for (ImageScanInfo info : scanInfoList) {
            addImageScanInfo(info);
        }
    }

    /**
     * 현재 저장된 이미지 스캔 정보 (삽입 순서, 읽기 전용).
     *
     * @since 0.7.0
     */
    public List<ImageScanInfo> getImageScanInfoList() {
        return Collections.unmodifiableList(new ArrayList<>(this.imageScanInfoMap.values()));
    }

    /**
     * 이미지 스캔 정보 전체 초기화.
     *
     * @since 0.7.0
     */
    public void clearImageScanInfo() {
        if (this.imageScanInfoMap.isEmpty()) return;
        this.imageScanInfoMap.clear();
        this.viewInfoChanged = true;
        log.debug("이미지 스캔 정보 전체 초기화");
    }
    
    /**
     * 현재 화면 정보 설정에 대한 프롬프트를 반환
     * 화면 정보가 변경되었을 때만 프롬프트를 반환합니다.
     *
     * <p>이미지 스캔 정보가 등록되어 있으면 {@code curViewInfo} JSON 의 {@code imageScanInfo} 키에
     * 배열로 병합된 후 직렬화되어 AI 에 전달된다.
     *
     * @return Current View Prompt (변경되지 않았으면 null)
     */
    public String getCurViewPrompt() {
        // 화면 정보가 없거나 변경되지 않았으면 null 반환
        if (!isCurrentViewInfo() || !this.viewInfoChanged) {
            if (!isCurrentViewInfo()) {
                log.debug("현재 화면 정보가 없어 프롬프트를 생성하지 않습니다.");
            } else {
                log.debug("화면 정보가 변경되지 않아 프롬프트를 생성하지 않습니다.");
            }
            return null;
        }

        StringBuffer aqPromptSb = new StringBuffer();
        Map<String, String> valueMap = new HashMap<>();

        valueMap.put("CurViewInfo", buildEffectiveViewInfoJson());
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
        this.imageScanInfoMap.clear();
    }

    /**
     * curViewInfo + imageScanInfo 를 합쳐 AI 에게 전달할 JSON 문자열을 구성한다.
     * curViewInfo 자체는 변경하지 않는다 (불변 유지).
     */
    @SuppressWarnings("unchecked")
    private String buildEffectiveViewInfoJson() {
        JSONObject effective;
        if (this.curViewInfo != null) {
            // shallow copy — curViewInfo 를 오염시키지 않기 위해
            effective = new JSONObject();
            for (Object entryKey : this.curViewInfo.keySet()) {
                String k = (String) entryKey;
                effective.put(k, this.curViewInfo.get(k));
            }
        } else {
            effective = new JSONObject();
            effective.put("format", this.format);
        }

        if (!this.imageScanInfoMap.isEmpty()) {
            JSONArray scanArray = new JSONArray();
            for (ImageScanInfo info : this.imageScanInfoMap.values()) {
                scanArray.add(info.toJSON());
            }
            effective.put("imageScanInfo", scanArray);
        }

        return effective.toJSONString();
    }
}

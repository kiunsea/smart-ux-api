package com.omnibuscode.ai.openai;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnibuscode.ai.ChatAction;
import com.omnibuscode.ai.ProcessFunction;
import com.omnibuscode.ai.manager.ChatManager;
import com.omnibuscode.ai.openai.connection.AssistantsConnection;
import com.omnibuscode.utils.JSONUtil;

public class ChatActionOpenAI extends ChatAction {

    private Logger log = LogManager.getLogger(ChatActionOpenAI.class);
    ObjectMapper objMapper = new ObjectMapper();
    
    private Assistant assistInfo = null;
    private AssistantsConnection conn = null;
    private String threadId = null; // thread id
    private Map<String, JsonNode> messages = null; // 대화방에서의 대화 목록
    
    public ChatActionOpenAI(Assistant assistInfo, AssistantsConnection conn, String threadId) {
        this.messages = new HashMap<String, JsonNode>();
        this.assistInfo = assistInfo;
        this.conn = conn;
        this.threadId = threadId;
    }
    
    @Override
    public JSONObject sendMessage(String userMsg) throws IOException, ParseException {

        JSONObject resJson = new JSONObject();
        
        String reqActions = 
                "\"" + userMsg + "\""
                + " 명령을 수행하기 위한 actionQueue JSON 을 작성해서 JSON 의 내용만 응답 메세지로 출력해줘, 그리고 JSON 내용에는 id 에 해당하는 selector 와 xpath 도 포함해줘";
        
        this.conn.createMessage(this.threadId, reqActions); //메세지 전달
        String runId = this.conn.createRun(this.threadId); //메세지 분석
        
        String runStatus = null;
        do {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JsonNode runInfo = this.conn.retrieveRun(this.threadId, runId);
            runStatus = runInfo.get("status").asText();
            if ("requires_action".equals(runStatus)) {
                JsonNode toolCalls = runInfo.get("required_action").get("submit_tool_outputs").get("tool_calls");
                JSONObject usrFuncsRst = null;
                for (JsonNode tc : toolCalls) {
                    if ("function".equals(tc.get("type").asText())) {
                        JsonNode fJson = tc.get("function");
                        String fName = fJson.get("name").asText();
                        String args = fJson.get("arguments").asText();
                        
                        Map<String, ProcessFunction> usrFuncMap = this.assistInfo.getFunctions();
                        if (usrFuncMap.containsKey(fName)) {
                            ProcessFunction usrFunc = usrFuncMap.get(fName);
                            JSONObject result = usrFunc.execFunction(fName, this.objMapper.readTree(args));
                            
                            if (usrFuncsRst == null) usrFuncsRst = new JSONObject();
                            usrFuncsRst.put(fName, result);
                            resJson.put(ChatManager.USER_FUNCTIONS_RESULT, usrFuncsRst);
                        }
                    }
                }
                this.conn.submitToolOutputs(toolCalls, this.threadId, runId);
            }
        } while (runStatus == null || !"completed".equals(runStatus));

        JsonNode msgArr = this.conn.listMessages(this.threadId);
        
        String resMsg = null;
        // 배열 노드 확인
        if (msgArr.isArray()) {
            // 배열 노드 순회
            String msg_id = null;
            for (JsonNode message : msgArr) {
                // 각 객체 노드의 값 출력
                msg_id = message.get("id").asText();
                if (!this.messages.containsKey(msg_id)) {
                    this.messages.put(msg_id, message);
                    if ("assistant".equals(message.get("role").asText())) {
                        resMsg = message.get("content").get(0).get("text").get("value").asText();
                    }
                }
            }
        } else {
            log.error("배열 형식이 아닙니다.");
        }
        
        if (resMsg != null) {
			JSONObject jObj = this.findJsonBlock(resMsg);
			JSONArray aqArr = null;
			if (jObj != null) {
				aqArr = (JSONArray) jObj.get("actionQueue");
			} else {
				aqArr = this.extractActionQueue(resMsg);
			}
            resJson.put("action_queue", aqArr);
        }
        
        resJson.put("message", resMsg);
        
        return resJson;
    }
    
    private JSONObject findJsonBlock(String paragraph) {
        
        JSONObject resObj = null;
        
        // JSON 문자열의 시작과 끝을 탐색
        int start = paragraph.indexOf("{");
        int end = paragraph.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            String jsonString = paragraph.substring(start, end + 1);
            try {
                resObj = JSONUtil.parseJSONObject(jsonString);
                log.debug("추출된 JSON 객체: " + resObj);
            } catch (Exception e) {
                log.debug("유효한 JSON이 아닙니다: " + e.getMessage());
            }
        } else {
            log.info("JSON 형식을 찾을 수 없습니다.");
        }
        
        return resObj;
    }
    
	private JSONArray extractActionQueue(String paragraph) throws ParseException {
		
		Object msgObj = JSONUtil.parseJSONObject(paragraph).get("actionQueue");
		if (msgObj == null) {
			return null;
		}
		String input = msgObj.toString();
		
		// 문자열에서 JSON 블록만 추출
		int jsonStart = input.indexOf("```json");
		int jsonEnd = input.lastIndexOf("```");

		if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
			throw new IllegalArgumentException("JSON block not found in input");
		}

		// JSON 문자열만 추출
		String jsonString = input.substring(jsonStart + 7, jsonEnd).trim();

		// JSONObject 생성 및 actionQueue 추출
		JSONObject jsonObject = JSONUtil.parseJSONObject(jsonString.replaceAll("\\/\\/", "//"));
		Object obj = jsonObject.get("actionQueue");
		return obj != null ? (JSONArray) obj : null;
	}

}

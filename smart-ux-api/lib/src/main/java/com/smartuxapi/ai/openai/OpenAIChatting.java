package com.smartuxapi.ai.openai;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.openai.assistants.AssistantAPIConnection;
import com.smartuxapi.util.JSONUtil;

public class OpenAIChatting implements Chatting {

    private Logger log = LogManager.getLogger(OpenAIChatting.class);
    
    ObjectMapper objMapper = new ObjectMapper();
    
    private AssistantAPIConnection connApi = null;
    private String idThread = null; // thread id
    
//    private String preprocMsg = null; //사용자 명령어 처리전 AI설정용 프롬프트
    private Set<String> messageIdSet = new HashSet<String>(); // 대화방에서의 메세지 id 목록
    
    public OpenAIChatting(Chatting chatting, AssistantAPIConnection connApi, String idThread) {
        this.connApi = connApi;
        this.idThread = idThread;
        this.messageIdSet = chatting.getMessageIdSet();
    }
    
    public OpenAIChatting(AssistantAPIConnection connApi, String idThread) {
        this.connApi = connApi;
        this.idThread = idThread;
    }
    
    public Set<String> getMessageIdSet() {
    	return this.messageIdSet;
    }
    
    @Override
    public JSONObject sendMessage(String userMsg) throws IOException, ParseException {

        JSONObject resJson = new JSONObject();
        
        this.connApi.createMessage(this.idThread, userMsg); //메세지 전달
        String runId = this.connApi.createRun(this.idThread); //메세지 분석
        
        String runStatus = null;
        do {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            JsonNode runInfo = this.connApi.retrieveRun(this.idThread, runId);
			runStatus = (runInfo != null && runInfo.hasNonNull("id")) ? runInfo.get("status").asText() : null;
            
            /**
             * TODO 추후 function call 기능을 구현해야 할때를 대비해서 구현한 코드
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
            */
            
        } while (runStatus == null || !"completed".equals(runStatus));

        JsonNode msgArr = this.connApi.listMessages(this.idThread);
		log.debug("🕒 res msg=" + msgArr.toPrettyString());
		resJson.put("org_msg", msgArr.toString());
        
        String resMsg = null;
        // 배열 노드 확인
        if (msgArr.isArray()) {
            // 배열 노드 순회
            for (JsonNode message : msgArr) {
                // 각 객체 노드의 값 출력
            	String id_msg = message.get("id").asText();
				if (!this.messageIdSet.contains(id_msg)) {
					this.messageIdSet.add(id_msg);
					if ("assistant".equals(message.get("role").asText())) {
						resMsg = message.get("content").get(0).get("text").get("value").asText();
					}
				}
            }
		} else if (msgArr.has("object") && "list".equals(msgArr.get("object").asText()) && msgArr.has("data")) {
			msgArr = msgArr.get("data");
		} else {
			log.error("응답 형식을 찾을 수 없습니다.");
		}
        resJson.put("message", resMsg);
        
        return resJson;
    }
    
    protected JsonNode extractJsonBlock(String paragraph) {
        
        JsonNode resObj = null;
        
        // JSON 문자열의 시작과 끝을 탐색
        int start = paragraph.indexOf("{");
        int end = paragraph.lastIndexOf("}");

        if (start != -1 && end != -1 && end > start) {
            String jsonString = paragraph.substring(start, end + 1);
            try {
                resObj = JSONUtil.parseJsonNode(jsonString);
                log.debug("추출된 JSON 객체: " + resObj);
            } catch (Exception e) {
                log.debug("유효한 JSON이 아닙니다: " + e.getMessage());
            }
        } else {
            log.info("JSON 형식을 찾을 수 없습니다.");
        }
        
        return resObj;
    }
    
	protected JsonNode extractActionQueue(String paragraph) throws ParseException, JsonMappingException, JsonProcessingException {

		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = null;

		JsonNode actionQueueNode = null;
		if (JSONUtil.isValidJson(paragraph)) {
			if (paragraph.indexOf("```") > -1) {
				// 문자열에서 JSON 블록만 추출
				int jsonStart = paragraph.indexOf("```json");
				int jsonEnd = paragraph.lastIndexOf("```");

				if (jsonStart == -1 || jsonEnd == -1 || jsonStart >= jsonEnd) {
					throw new IllegalArgumentException("JSON block not found in input");
				}

				// JSON 문자열만 추출
				String jsonString = paragraph.substring(jsonStart + 7, jsonEnd).trim();
				rootNode = objectMapper.readTree(jsonString);
			} else {
				rootNode = objectMapper.readTree(paragraph);
			}

			actionQueueNode = rootNode.get("actionQueue");
		}
		
		if (actionQueueNode != null) {
			if (actionQueueNode.isArray()) {
				log.debug("--- 'actionQueue' (배열) 값 ---");
				for (JsonNode action : actionQueueNode) {
					log.debug(action.toPrettyString());
				}
			} else {
				log.debug("--- 'actionQueue' (다른 타입) 값 ---");
				log.debug("타입: " + actionQueueNode.getNodeType());
				log.debug("값: " + actionQueueNode.asText());
			}
		} else {
			log.debug("'actionQueue' 필드가 존재하지 않습니다.");
		}
		
		return actionQueueNode;
	}

}
package com.smartuxapi.ai.openai.assistants;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.SmuMessage;

public class OpenAIMessage implements SmuMessage {

    private Logger log = LogManager.getLogger(OpenAIMessage.class);
    
    private ObjectMapper objMapper = new ObjectMapper();
    
    private AssistantAPIConnection connApi = null;
    private String idThread = null; // thread id
    
    private ActionQueueHandler aqHandler = null;
    private Set<String> messageIdSet = new HashSet<String>(); // 대화방에서의 메세지 id 목록
    
    public OpenAIMessage(SmuMessage chatting, AssistantAPIConnection connApi, String idThread) {
        this.connApi = connApi;
        this.idThread = idThread;
        this.messageIdSet = chatting.getMessageIdSet();
    }
    
    public OpenAIMessage(AssistantAPIConnection connApi, String idThread) {
        this.connApi = connApi;
        this.idThread = idThread;
    }
    
    public Set<String> getMessageIdSet() {
    	return this.messageIdSet;
    }
    
    @Override
    public JSONObject sendMessage(String userMsg) throws IOException, ParseException {

        JSONObject resJson = new JSONObject();
        
        // Action Queue 요청 Prompt 작성 및 전달
        String aqPrompt = null;
        if (this.aqHandler != null) {
            aqPrompt = this.aqHandler.decoratePrompt(userMsg);
        } else {
            aqPrompt = userMsg;
        }
        this.connApi.createMessage(this.idThread, aqPrompt); //메세지 전달
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
        
        // Action Queue 메세지 전달
        resJson.put("message", resMsg);

        JsonNode aqObj = this.aqHandler.getActionQueue(resMsg);
        if (aqObj.hasNonNull("actionQueue")) {
            resJson.put("action_queue", aqObj.get("actionQueue"));
        } else {
            resJson.put("action_queue", aqObj);
        }

        return resJson;
    }

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
    }

}
package com.omnibuscode.ai.openai.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omnibuscode.ai.openai.Assistant;

/**
 * Assistants API에 연결한다.
 * ※ References
 *   - https://platform.openai.com/docs/api-reference/assistants
 *   - https://kiunsea.tistory.com/37
 * ※ Conversation Process
 *   - Create a Thread : 대화방 생성
 *   - Add a Message to the Thread : 전달할 메세지 봉투 생성
 *   - Create a Run : 메세지 봉투 전달
 *   - Retrieve Run : 전달 확인
 *   - List Messages : 응답 메세지 확인
 */
public class AssistantsConnection {
	
	private Logger log = LogManager.getLogger(AssistantsConnection.class);
	private String BASE_URL = "https://api.openai.com/v1";
	
    private Assistant assistInfo = null;
    private ObjectMapper objMapper = new ObjectMapper();
    
    public AssistantsConnection(Assistant assistInfo) {
    	this.assistInfo = assistInfo;
    }
    
    /**
     * 대화방 생성 Request (id를 반환)
     * @return thread id
     * @throws IOException
     * @throws ParseException
     */
    public String createThread() throws IOException, ParseException {
        String url = String.format("%s/threads", this.BASE_URL);

        log.debug("Request url: "+url);
        String response = sendRequest(url, "POST", null);
        log.debug("Response of sendRequest(): "+response);
        
        // JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode threadInfo = this.objMapper.readTree(response);
        String threadId = threadInfo.get("id").asText();
        return threadId;
    }
    
    /**
     * 대화방 종료
     * @param threadId
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public boolean deleteThread(String threadId) throws IOException, ParseException {
    	String url = String.format("%s/threads/%s", this.BASE_URL, threadId);
        String response = sendRequest(url, "DELETE", null);
        log.debug("Response of sendRequest(): "+response);
        JsonNode resInfo = this.objMapper.readTree(response);
        JsonNode rtnVal = resInfo.get("deleted");
        return rtnVal != null ? rtnVal.asBoolean() : false;
    }
    
    /**
     * openai 에 사용자 메세지 봉투 생성을 Request (id 반환)
     * @param threadId
     * @param content
     * @return 메세지 아이디
     * @throws IOException
     * @throws ParseException
     */
    public String createMessage(String threadId, String content) throws IOException, ParseException {
        String url = String.format("%s/threads/%s/messages", this.BASE_URL, threadId);
        log.debug("Request url: "+url);
        
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("role", "user");
        bodyJson.put("content", content);
        
        String response = sendRequest(url, "POST", bodyJson.toJSONString());
        log.debug("Response of sendRequest(): "+response);
        
        // JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode msgInfo = this.objMapper.readTree(response);
        String msgId = msgInfo.get("id").asText();
        return msgId;
    }
    
    /**
     * 사용자 메세지를 분석 실행시키고 실행 아이디를 받는다.
     * @param threadId
     * @return 실행 아이디
     * @throws IOException
     * @throws ParseException
     */
    public String createRun(String threadId) throws IOException, ParseException {
        String url = String.format("%s/threads/%s/runs", this.BASE_URL, threadId);
        log.debug("Request url: "+url);
        
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("assistant_id", this.assistInfo.getAssistantId());
        String response = sendRequest(url, "POST", bodyJson.toJSONString());
        log.debug("Response of sendRequest(): "+response);
        
        // JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode runInfo = this.objMapper.readTree(response);
		String runId = runInfo.has("id") ? runInfo.get("id").asText() : null;
        return runId;
    }
    
    /**
     * 메세지 처리가 완료되었는지 여부
     * @param threadId
     * @param runId
     * @return
     * @throws IOException
     * @throws ParseException
     */
    public boolean completedRun(String threadId, String runId) throws IOException, ParseException {
    	String runStatus = retrieveRun(threadId, runId).get("status").asText();
    	return (runStatus == null || !"completed".equals(runStatus)) ? false : true;
    }
    
	/**
	 * 메세지 처리 확인을 위한 Request
	 * @param threadId
	 * @param runId
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public JsonNode retrieveRun(String threadId, String runId) throws IOException, ParseException {
		String url = String.format("%s/threads/%s/runs/%s", this.BASE_URL, threadId, runId);
		log.debug("Request url: " + url);

		String response = sendRequest(url, "GET", null);
		log.debug("Response of sendRequest(): " + response);

		// JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode runInfo = this.objMapper.readTree(response);
		return runInfo;
	}
	
	/**
	 * 대화방 안의 메세지 봉투 목록 Request
	 * @param threadId
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public JsonNode listMessages(String threadId) throws IOException, ParseException {
		String url = String.format("%s/threads/%s/messages", this.BASE_URL, threadId);
		log.debug("Request url: " + url);

		String response = sendRequest(url, "GET", null);
		log.debug("Response of sendRequest(): " + response);

        JsonNode resJson = this.objMapper.readTree(response);
		JsonNode messages = resJson.get("data");

		return messages;
	}
	
	/**
	 * function call 처리 결과를 응답 Request
	 * @param toolCalls
	 * @param threadId
	 * @param runId
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public boolean submitToolOutputs(JsonNode toolCalls, String threadId, String runId) throws IOException, ParseException {
		String url = String.format("%s/threads/%s/runs/%s/submit_tool_outputs", this.BASE_URL, threadId, runId);

		JSONObject bodyJson = new JSONObject();
		if (toolCalls != null && toolCalls.isArray()) {
		    JSONArray callArr = new JSONArray();
		    for (JsonNode element : toolCalls) {
		        JSONObject callJson = new JSONObject();
		        String callId = element.get("id").asText();
		        callJson.put("tool_call_id", callId);
		        callJson.put("output", "완료"); //요청 액션에 대한 실행 결과를 응답
		        callArr.add(callJson);

		        if ("function".equals(element.get("type").asText())) {
		            String fName = element.get("function").get("name").asText();
		            log.debug(" > Processing complete: [" + fName + "] function called");//function 처리 결과
		        }
		    }
		    bodyJson.put("tool_outputs", callArr);
		} else {
		    log.debug("'data' is not an array or is missing.");
		}

		String response = sendRequest(url, "POST", bodyJson.toJSONString());
        JsonNode runInfo = this.objMapper.readTree(response);
		String status = runInfo.get("status").asText();
		return (status != null && "queued".equals(status));
	}
    
    private String sendRequest(String urlString, String methodType, String bodyJson) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(methodType != null ? methodType : "POST");
        connection.setRequestProperty("Authorization", "Bearer " + this.assistInfo.getApiKey());
        connection.setRequestProperty("Content-Type", "application/json; utf-8");
        connection.setRequestProperty("OpenAI-Beta", "assistants=v2");
        connection.setDoOutput(true);

        if (bodyJson != null) {
	        try (OutputStream os = connection.getOutputStream()) {
	            byte[] input = bodyJson.getBytes(StandardCharsets.UTF_8);
	            os.write(input, 0, input.length);
	        }
        }

        int responseCode = connection.getResponseCode();
        InputStream is = (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) ? connection.getInputStream() : connection.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}

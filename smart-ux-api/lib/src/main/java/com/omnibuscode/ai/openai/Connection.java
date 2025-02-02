package com.omnibuscode.ai.openai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Connection {
	
	private String BASE_URL = "https://api.openai.com/v1";
	
    private String assistantId = null;
    private String apiKey = null;
    
    private ObjectMapper mapper = null;
    
    public Connection(String assistantId, String apiKey) {
    	this.assistantId = assistantId;
    	this.apiKey = apiKey;
    	
    	this.mapper = new ObjectMapper();
    }
    
    /**
     * 
     * @return thread id
     * @throws IOException
     * @throws ParseException
     */
    public String createThread() throws IOException, ParseException {
        String url = String.format("%s/threads", this.BASE_URL);

        System.out.println("Request url: "+url);
        String response = sendRequest(url, "POST", null);
        System.out.println("Response of sendRequest(): "+response);
        
        // JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode threadInfo = this.mapper.readTree(response);
        String threadId = threadInfo.get("id").asText();
        return threadId;
    }
    
    public String deleteThread(String threadId) throws IOException, ParseException {
    	String url = String.format("%s/threads/%s", this.BASE_URL, threadId);
        String response = sendRequest(url, "DELETE", null);
        System.out.println("Response of sendRequest(): "+response);
        JsonNode resInfo = this.mapper.readTree(response);
        String deleted = resInfo.get("deleted").asText();
        return deleted;
    }
    
    public String createMessage(String threadId, String content) throws IOException, ParseException {
        String url = String.format("%s/threads/%s/messages", this.BASE_URL, threadId);
        System.out.println("Request url: "+url);
        
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("role", "user");
        bodyJson.put("content", content);
        
        String response = sendRequest(url, "POST", bodyJson.toJSONString());
        System.out.println("Response of sendRequest(): "+response);
        
        // JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode msgInfo = this.mapper.readTree(response);
        String msgId = msgInfo.get("id").asText();
        return msgId;
    }
    
    public String createRun(String threadId) throws IOException, ParseException {
        String url = String.format("%s/threads/%s/runs", this.BASE_URL, threadId);
        System.out.println("Request url: "+url);
        
        JSONObject bodyJson = new JSONObject();
        bodyJson.put("assistant_id", this.assistantId);
        String response = sendRequest(url, "POST", bodyJson.toJSONString());
        System.out.println("Response of sendRequest(): "+response);
        
        // JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode runInfo = this.mapper.readTree(response);
        String runId = runInfo.get("id").asText();
        return runId;
    }
    
    public boolean completedRun(String threadId, String runId) throws IOException, ParseException {
    	String runStatus = retrieveRun(threadId, runId).get("status").asText();
    	return (runStatus == null || !"completed".equals(runStatus)) ? false : true;
    }
    
	public JsonNode retrieveRun(String threadId, String runId) throws IOException, ParseException {
		String url = String.format("%s/threads/%s/runs/%s", this.BASE_URL, threadId, runId);
		System.out.println("Request url: " + url);

		String response = sendRequest(url, "GET", null);
		System.out.println("Response of sendRequest(): " + response);

		// JSON 파싱을 통해 id 추출 (라이브러리 없이 간단히 처리)
        JsonNode runInfo = this.mapper.readTree(response);
		return runInfo;
	}
	
	public JsonNode listMessages(String threadId) throws IOException, ParseException {
		String url = String.format("%s/threads/%s/messages", this.BASE_URL, threadId);
		System.out.println("Request url: " + url);

		String response = sendRequest(url, "GET", null);
		System.out.println("Response of sendRequest(): " + response);

        JsonNode resJson = this.mapper.readTree(response);
		JsonNode messages = resJson.get("data");

		return messages;
	}
	
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
		            System.out.println(" > [" + fName + "] function called!!");//function 처리에 대해서만 로깅
		        }
		    }
		    bodyJson.put("tool_outputs", callArr);
		} else {
		    System.out.println("'data' is not an array or is missing.");
		}

		String response = sendRequest(url, "POST", bodyJson.toJSONString());
        JsonNode runInfo = this.mapper.readTree(response);
		String status = runInfo.get("status").asText();
		return (status != null && "queued".equals(status));
	}
    
    private String sendRequest(String urlString, String methodType, String bodyJson) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(methodType != null ? methodType : "POST");
        connection.setRequestProperty("Authorization", "Bearer " + this.apiKey);
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

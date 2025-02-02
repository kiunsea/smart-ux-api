package com.omnibuscode.ai.openai;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.ChatRoom;

/**
 * 대화방 클래스
 * ChatGPT에서는 Thread
 */
public class ChatThread implements ChatRoom {
	
	private Logger log = LogManager.getLogger(ChatThread.class);
	
	private Connection conn = null;
	private String threadId = null; // thread id
	private Map messages = null; // 대화방에서의 대화 목록

	public ChatThread() {
		
	    String apiKey = "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";
	    String assistantId = "asst_hsP6560JM3JiFi0HlU4gR8hZ";
	    String baseUrl = "https://api.openai.com/v1";
		
		conn = new Connection(apiKey, assistantId, baseUrl);
		try {
			String threadId = conn.createThread();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
		this.threadId = threadId;
		
		this.messages = new HashMap<String, ChatMessage>();
	}

	/**
	 * 입력한 사용자 메세지를 thread 에 추가하고 run 한다
	 * @param userMsg
	 * @return assistant message
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public String sendMessage(String userMsg) throws IOException, ParseException {
		
		String msgId = conn.createMessage(this.threadId, userMsg);
		String runId = conn.createRun(threadId);
		
		Map<String, String> onboardLinkMap = new HashMap<String, String>();
		String runStatus = null;
		do {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			JsonNode runInfo = conn.retrieveRun(this.threadId, runId);
			runStatus = runInfo.get("status").asText();
			if ("requires_action".equals(runStatus)) {
				JsonNode toolCalls = runInfo.get("required_action").get("submit_tool_outputs").get("tool_calls");
				for (JsonNode tc : toolCalls) {
					if ("function".equals(tc.get("type").asText())) {
						JsonNode fJson = tc.get("function");
						//TODO tool call 처리 로직은 기본과 사용자 두가지로 추후 처리
//						String onboardName = fJson.get("name").asText();
//						String args = fJson.get("arguments").asText();
//						
//						String userCodePath = OnboardingUtil.createUsrOnboarding(seqUser, onboardName, JSONUtil.parseJsonNode(args));
//						onboardLinkMap.put(onboardName, userCodePath);
					}
				}
				conn.submitToolOutputs(toolCalls, this.threadId, runId);
			}
		} while (runStatus == null || !"completed".equals(runStatus));

		JsonNode msgArr = conn.listMessages(this.threadId);
		
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
		
		//onbording 태그 추가
		if (onboardLinkMap.size() > 0) {
			Iterator<String> keyIter = onboardLinkMap.keySet().iterator();
			while (keyIter.hasNext()) {
				String key = keyIter.next().toString();
				String link = "/jbs/onboarding?usr_scenario=" + onboardLinkMap.get(key).toString();
				resMsg += "\n- 온보딩 가이드 : <a href=\"#\" onclick=\"userOnboarding('" + java.net.URLEncoder.encode(link, "UTF-8") + "')\">" + key + "</a>";
			}
		}
		
		//html tag로 변환
		resMsg = resMsg.replaceAll("\\r\\n|\\r|\\n", "<br>");
		resMsg = this.convertAsterisksToBoldTags(resMsg);
		
		return resMsg;
	}
	
	/**
	 * 대화방 나가기
	 * @throws ParseException
	 * @throws IOException 
	 */
	public void closeChat() throws IOException, ParseException {
		String deleted = conn.deleteThread(threadId);
		log.debug("delete thread [" + threadId + "] - " + deleted);
	}
	
	/**
	 * '**' 문자가 시작하는 부분부터 끝에 해당하는 단어에 bold 를 적용한다.
	 * @param text
	 * @return
	 */
	private String convertAsterisksToBoldTags(String input) {
		StringBuilder result = new StringBuilder();
		boolean boldToggle = false; // Bold 상태를 추적
		int index = 0;

		while (index < input.length()) {
			// '**' 발견 시 처리
			if (index + 1 < input.length() && input.charAt(index) == '*' && input.charAt(index + 1) == '*') {
				result.append(boldToggle ? "</b>" : "<b>"); // Bold 상태에 따라 태그 추가
				boldToggle = !boldToggle; // Bold 상태 변경
				index += 2; // '**'를 건너뜀
			} else {
				result.append(input.charAt(index)); // 일반 문자 추가
				index++;
			}
		}

		return result.toString();
    }
}

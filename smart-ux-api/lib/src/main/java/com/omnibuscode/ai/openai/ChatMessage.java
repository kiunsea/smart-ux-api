package com.omnibuscode.ai.openai;

/**
 * 메세지 클래스
 * 대화방에서 AI와 사용자가 주고받는 메세지
 * openai api 에서는 message 의 role 속성에 'user' 와 'assistant' 로 구분한다.
 */
public class ChatMessage {

	private String msgId = null; // message id
	private String role = null;
	private String content = null;

	public ChatMessage(String msgId, String role, String content) {
		this.msgId = msgId;
		this.role = role;
		this.content = content;
	}
	
	public String getRole() {
		return this.role;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return this.content;
	}

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}
}

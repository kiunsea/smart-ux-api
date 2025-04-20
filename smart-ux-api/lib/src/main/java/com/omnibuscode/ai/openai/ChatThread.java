package com.omnibuscode.ai.openai;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.connection.ChatConnection;

/**
 * ChatGPT의 Thread
 */
public class ChatThread extends ChatRoom {
	
	private Logger log = LogManager.getLogger(ChatThread.class);
	
	private Assistant assistInfo = null;
	private ChatConnection conn = null;
	private String threadId = null; // thread id

	public String getId() {
		return this.threadId;
	}
	
	public ChatThread(Assistant assistInfo) {
		
		this.assistInfo = assistInfo;
		this.conn = new ChatConnection(assistInfo);
		try {
			this.threadId = this.conn.createThread();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Chatting createChatting(int chatmode) {
		
		Chatting chat = null;
		
		if (chatmode == 1) {
			chat = new ChatActionOpenAI(this.assistInfo, this.conn, this.threadId);
		} else {
			chat = new ChatBuddyOpenAI(this.assistInfo, this.conn, this.threadId);
		}
		
		return chat;
	}
	
	
	public void setFunctionMap(Map usrFuncs) {
		//TODO
		
	}
	
	/**
	 * 대화방 나가기
	 * @throws ParseException
	 * @throws IOException 
	 */
	public boolean closeChat() throws IOException, ParseException {
		boolean deleted = this.conn.deleteThread(threadId);
		log.debug("delete thread [" + threadId + "] - " + deleted);
		return deleted;
	}
	
	//CHECK 사용 안함
//	/**
//	 * '**' 문자가 시작하는 부분부터 끝에 해당하는 단어에 bold 를 적용한다.
//	 * @param text
//	 * @return
//	 */
//	private String convertAsterisksToBoldTags(String input) {
//		StringBuilder result = new StringBuilder();
//		boolean boldToggle = false; // Bold 상태를 추적
//		int index = 0;
//
//		while (index < input.length()) {
//			// '**' 발견 시 처리
//			if (index + 1 < input.length() && input.charAt(index) == '*' && input.charAt(index + 1) == '*') {
//				result.append(boldToggle ? "</b>" : "<b>"); // Bold 상태에 따라 태그 추가
//				boldToggle = !boldToggle; // Bold 상태 변경
//				index += 2; // '**'를 건너뜀
//			} else {
//				result.append(input.charAt(index)); // 일반 문자 추가
//				index++;
//			}
//		}
//
//		return result.toString();
//    }
}

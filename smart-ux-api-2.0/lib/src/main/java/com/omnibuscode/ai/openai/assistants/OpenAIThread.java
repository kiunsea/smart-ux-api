package com.omnibuscode.ai.openai.assistants;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.OpenAIChatting;

/**
 * ChatGPT의 Thread
 */
public class OpenAIThread {
	
	private Logger log = LogManager.getLogger(OpenAIThread.class);
	
	private OpenAIAssistant assistInfo = null;
	private APIConnection conn = null;
	private String threadId = null; // thread id

	public String getId() {
		return this.threadId;
	}
	
	public OpenAIThread(OpenAIAssistant assistInfo) {
		
		this.assistInfo = assistInfo;
		this.conn = new APIConnection(assistInfo);
		try {
			this.threadId = this.conn.createThread();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Chatting createChatting(int chatmode) {
		
		return new OpenAIChatting();
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
}

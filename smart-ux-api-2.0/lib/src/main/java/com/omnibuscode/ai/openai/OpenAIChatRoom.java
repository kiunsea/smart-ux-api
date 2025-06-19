package com.omnibuscode.ai.openai;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.assistants.APIConnection;
import com.omnibuscode.ai.openai.assistants.Assistant;

public class OpenAIChatRoom implements ChatRoom {

	private Logger log = LogManager.getLogger(OpenAIChatRoom.class);

	private String idThread = null; // OpenAI에서 부여하는 채팅방 고유 키값(thread id)
	private Assistant assistInfo = null;
	private APIConnection connApi = null;

	public OpenAIChatRoom(Assistant assistInfo) {

		this.assistInfo = assistInfo;
		this.connApi = new APIConnection(assistInfo);
		try {
			this.idThread = this.connApi.createThread();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

	public Chatting createChatting(int chatmode) {

		return new OpenAIChatting(this.connApi, this.idThread);
	}

	public String getId() {
		return this.idThread;
	}

	public void setFunctionMap(Map usrFuncs) {
		// TODO
	}

	@Override
	public Chatting createChatting() {
		return new OpenAIChatting(this.connApi, this.idThread);
	}

	/**
	 * 대화방 나가기
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean closeChat() throws IOException, ParseException {
		boolean deleted = this.connApi.deleteThread(idThread);
		log.debug("delete thread [" + idThread + "] - " + deleted);
		return deleted;
	}
}

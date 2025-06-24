package com.omnibuscode.ai.openai;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.assistants.APIConnection;
import com.omnibuscode.ai.openai.assistants.Assistant;
import com.omnibuscode.ai.openai.decorator.ActionQueueDecorator;
import com.omnibuscode.ai.openai.decorator.UXInfoDecorator;

public class OpenAIChatRoom implements ChatRoom {

	public static String USER_FUNCTIONS_RESULT = "USR_FUNCS_RST";
	private Logger log = LogManager.getLogger(OpenAIChatRoom.class);
	private String curViewInfo = null;

	private String idThread = null; // OpenAI에서 부여하는 채팅방 고유 키값(thread id)
	private APIConnection connApi = null;
	
	private Chatting chat = null;

	public OpenAIChatRoom(Assistant assistInfo) throws ParseException {

		this.connApi = new APIConnection(assistInfo);
		try {
			this.idThread = this.connApi.createThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getThreadId() {
		return this.idThread;
	}

	public void setFunctionMap(Map usrFuncs) {
		// TODO
	}

	@Override
	public Chatting createChatting() {
		if (this.chat == null) {
			this.chat = new OpenAIChatting(this.connApi, this.idThread);
		}
		return this.chat;
	}
	
	public Chatting decorateActionQueue(Chatting chat) {
		return new ActionQueueDecorator(chat, this.connApi, this.idThread);
	}
	
	public Chatting decorateUXInfo(Chatting chat) {
		return new UXInfoDecorator(chat, this.connApi, this.idThread);
	}
	
	/**
	 * 현재 화면 정보를 AI에게 전달하여 학습
	 * @param viewInfoJson
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public String setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {
		this.curViewInfo = viewInfoJson;
		Chatting chat = this.createChatting();
		String usrQ = "다음은 사용자가 현재 보고있는 화면에서 사용자 액션이 가능한 dom element structure야. 해당 정보를 이용해서 사용자 액션을 처리할거야. "+viewInfoJson;
		JSONObject resJson = chat.sendMessage(usrQ);
		String aiMsg = resJson.containsKey("message") ? resJson.get("message").toString() : null;
		return aiMsg;
	}

	/**
	 * 대화방 나가기
	 * 
	 * @throws IOException
	 * @throws org.json.simple.parser.ParseException 
	 */
	public boolean closeChat() throws IOException, ParseException {
		boolean deleted = this.connApi.deleteThread(idThread);
		log.debug("delete thread [" + idThread + "] - " + deleted);
		return deleted;
	}
}

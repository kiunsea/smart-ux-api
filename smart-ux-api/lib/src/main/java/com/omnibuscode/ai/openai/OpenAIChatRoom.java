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

	public static String CHATROOM_TYPE_NORMAL = "NORMAL";
	public static String CHATROOM_TYPE_UXINFO = "UXINFO";
	public static String USER_FUNCTIONS_RESULT = "USR_FUNCS_RST";
	
	private Logger log = LogManager.getLogger(OpenAIChatRoom.class);

	private String idThread = null; // OpenAI에서 부여하는 채팅방 고유 키값(thread id)
	private APIConnection connApi = null;
	
	private String roomType = null;
	private Chatting chat = null;

	public OpenAIChatRoom(Assistant assistInfo) throws ParseException {

		this.connApi = new APIConnection(assistInfo);
		try {
			this.idThread = this.connApi.createThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.roomType = OpenAIChatRoom.CHATROOM_TYPE_NORMAL;
	}

	public String getThreadId() {
		return this.idThread;
	}

	public void setFunctionMap(Map usrFuncs) {
		// TODO
	}
	
	public String getRoomType() {
		return this.roomType;
	}

	/**
	 * Chatting instance 생성후 재사용한다.
	 */
	@Override
	public Chatting getChatting() {
		if (this.chat == null) {
			this.roomType = OpenAIChatRoom.CHATROOM_TYPE_NORMAL;
			this.chat = new OpenAIChatting(this.connApi, this.idThread);
		}
		return this.chat;
	}
	
	public Chatting decorateUXInfo(Chatting chat) {
		this.roomType = OpenAIChatRoom.CHATROOM_TYPE_UXINFO;
		return new ActionQueueChatting(chat, this.connApi, this.idThread);
	}
	
	/**
	 * 현재 화면 정보를 채팅목록에 저장 
	 * @param viewInfoJson
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {

		ActionQueueChatting aqChat = null;
		Chatting chat = this.getChatting();
		if (!OpenAIChatRoom.CHATROOM_TYPE_UXINFO.equals(this.roomType)) {
			aqChat = (ActionQueueChatting) this.decorateUXInfo(chat);
			this.chat = aqChat;
		} else {
			aqChat = (ActionQueueChatting) chat;
		}
		aqChat.setCurViewInfo(viewInfoJson);
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

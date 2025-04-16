package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.openai.Assistant;

public class ChatManagerTest {

	private ChatManager cm = null;
	private ChatRoom cr = null;
	
	@Before
    public void setUp() throws Exception {
		cm = new ChatManager();
		
		//jiniebox용
//		Assistant assist = new Assistant("asst_hsP6560JM3JiFi0HlU4gR8hZ");
//		DummyUserFunction dumUsrFunc = new DummyUserFunction();
//		assist.putFunction("on_jangbogo", dumUsrFunc);
		
		//su-api용
		Assistant assist = new Assistant("asst_6T4VCQSWs0R6WrBZRxsiXiFJ");
		
		assist.setApiKey("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");
		cm.setAssistant(assist);

		JSONObject jo = cm.createChatRoom(cm.AI_NAME_OPENAI);
		this.cr = (ChatRoom) jo.get("instance");
		System.out.println("* [" + cr.getId() + "] 채팅방 생성 결과: " + jo.toString());
    }
	
//	@Test
//	public void testChatBuddy() throws Exception {
//		Chatting chat = cr.createChatting(0);
//		this.sendMsg(chat, "넌 누구니?");
//		this.sendMsg(chat, "너의 이름은?");
//		this.sendMsg(chat, "장보고 온보딩");
//
//		if (cm.closeChatRoom())
//			System.out.println("* [" + cr.getId() + "] 채팅방 삭제 완료");
//	}
	
	@Test
	public void testChatAction() throws Exception {
		Chatting chat = cr.createChatting(1);
		this.sendMsg(chat, "아메리카노와 커피프라페 주문해줘");

		if (cm.closeChatRoom())
			System.out.println("* [" + cr.getId() + "] 채팅방 삭제 완료");
	}

	private void sendMsg(Chatting chat, String usrQ) throws IOException, ParseException {
		System.out.println("* USER msg: " + usrQ);
		JSONObject resJson = chat.sendMessage(usrQ);
		System.out.println("* AI msg: " + resJson.get("message"));
		Object usrFuncRst = resJson.get(ChatManager.USER_FUNCTIONS_RESULT);
		if (usrFuncRst != null) {
			Object onJbgRst = ((JSONObject) usrFuncRst).get("on_jangbogo");
			if (onJbgRst != null)
				System.out.println("* AI user_link: " + ((JSONObject) onJbgRst).get("user_link"));
		}
	}

}

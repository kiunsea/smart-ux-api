package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.openai.Assistant;

public class ChatManagerTest {

	@Test
	public void test() throws Exception {
//		fail("Not yet implemented");
		ChatManager cm = new ChatManager();
		Assistant assist = new Assistant();
		DummyUserFunction dumUsrFunc = new DummyUserFunction();
		assist.putFunction("on_jangbogo", dumUsrFunc);
		assist.setApiKey("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");
		assist.setAssistantId("asst_hsP6560JM3JiFi0HlU4gR8hZ");
		cm.setAssistInfo(assist);

		JSONObject jo = cm.createChatRoom(cm.AI_NAME_OPENAI);
		ChatRoom cr = (ChatRoom) jo.get("instance");
		System.out.println("* [" + cr.getId() + "] 채팅방 생성 결과: " + jo.toString());

		this.sendMsg(cr, "넌 누구니?");
		this.sendMsg(cr, "너의 이름은?");
		this.sendMsg(cr, "장보고 온보딩");

		if (cm.closeChatRoom())
			System.out.println("* [" + cr.getId() + "] 채팅방 삭제 완료");
	}

	private void sendMsg(ChatRoom cr, String usrQ) throws IOException, ParseException {
		System.out.println("* USER msg: " + usrQ);
		JSONObject resJson = cr.sendMessage(usrQ);
		System.out.println("* AI msg: " + resJson.get("message"));
		Object usrFuncRst = resJson.get(ChatManager.USER_FUNCTIONS_RESULT);
		if (usrFuncRst != null) {
			Object onJbgRst = ((JSONObject) usrFuncRst).get("on_jangbogo");
			if (onJbgRst != null)
				System.out.println("* AI user_link: " + ((JSONObject) onJbgRst).get("user_link"));
		}
	}

}

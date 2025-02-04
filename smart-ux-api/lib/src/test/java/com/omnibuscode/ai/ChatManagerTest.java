package com.omnibuscode.ai;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.omnibuscode.ai.openai.Assistant;

public class ChatManagerTest {

	@Test
	public void test() throws IOException, ParseException {
//		fail("Not yet implemented");
		ChatManager cm = new ChatManager();
		Assistant assist = new Assistant();
		cm.setAssistInfo(assist);

		JSONObject jo = cm.createChatRoom(cm.AINAME_OPENAI);
		ChatRoom cr = (ChatRoom) jo.get("instance");
		System.out.println("* [" + cr.getId() + "] 채팅방 생성 결과: " + jo.toJSONString());

		this.sendMsg(cr, "넌 누구니?");
		this.sendMsg(cr, "너의 이름은?");
		this.sendMsg(cr, "장보고 온보딩");

		if (cm.closeChatRoom())
			System.out.println("* [" + cr.getId() + "] 채팅방 삭제 완료");
	}

	private void sendMsg(ChatRoom cr, String usrQ) throws IOException, ParseException {
		System.out.println("* USER: " + usrQ);
		System.out.println("* AI: " + cr.sendMessage(usrQ).get("message"));
	}

}

package com.omnibuscode.ai.manager;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.assistants.OpenAIAssistant;
import com.omnibuscode.legacy.ChatRoom;
import com.omnibuscode.legacy.manager.ChatManager;

/**
 * TODO servlet session 이 필요해서 테스트 케이스는 일단 보류
 */
public class ActionQueueManagerTest {

	private ChatManager cm = null;
	private ChatRoom cr = null;
	
	@Before
    public void setUp() throws Exception {
		cm = new ChatManager();
		
		//su-api용
		OpenAIAssistant assist = new OpenAIAssistant("asst_6T4VCQSWs0R6WrBZRxsiXiFJ");
		
		assist.setApiKey("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");
		cm.setAssistant(assist);

		JSONObject jo = cm.createChatRoom(cm.AI_NAME_OPENAI);
		this.cr = (ChatRoom) jo.get("instance");
		System.out.println("* [" + cr.getId() + "] 채팅방 생성 결과: " + jo.toString());
    }
	
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
		
		if (resJson.containsKey("actionQueue")) {
			System.out.println("* Action Queue: " + resJson.get("actionQueue").toString());
		}

	}

}

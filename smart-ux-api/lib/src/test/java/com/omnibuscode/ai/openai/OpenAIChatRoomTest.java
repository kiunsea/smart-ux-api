package com.omnibuscode.ai.openai;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.Chatting;
import com.omnibuscode.ai.openai.assistants.Assistant;

public class OpenAIChatRoomTest {

	private OpenAIChatRoom chatRoom = null;
	
	public OpenAIChatRoomTest() throws ParseException {
		Assistant assist = new Assistant("asst_6T4VCQSWs0R6WrBZRxsiXiFJ");
		assist.setApiKey(
				"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");
		this.chatRoom = new OpenAIChatRoom(assist);
		System.out.println("* [" + this.chatRoom.getThreadId() + "] 채팅방 생성");
	}
	
	/**
	 * OpenAI Assistants API에 접속하여 thread를 생성하고 요청 메세지를 전달후 응답 메세지를 받는다.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		OpenAIChatRoomTest crTest = new OpenAIChatRoomTest();
//		crTest.testChat(); //일반 대화 테스트
//		crTest.testChatAction(); //static action queue 응답 테스트
		crTest.testSetCurrentViewInfo(); //dynamic action queue 응답 테스트
	}
	
	public void testChat() throws Exception {
		Chatting chat = this.chatRoom.getChatting();
		String usrQ = "너는 누구니?";

		System.out.println("* USER msg: " + usrQ);
		JSONObject resJson = chat.sendMessage(usrQ);
		System.out.println("* AI msg: " + resJson.get("message"));
		
		if (resJson.containsKey("actionQueue")) {
			System.out.println("* Action Queue: " + resJson.get("actionQueue").toString());
		}
		
		Object usrFuncRst = resJson.get(OpenAIChatRoom.USER_FUNCTIONS_RESULT);
		if (usrFuncRst != null) {
			Object onJbgRst = ((JSONObject) usrFuncRst).get("on_jangbogo");
			if (onJbgRst != null)
				System.out.println("* AI user_link: " + ((JSONObject) onJbgRst).get("user_link"));
		}

		if (this.chatRoom.closeChat())
			System.out.println("* [" + this.chatRoom.getThreadId() + "] 채팅방 삭제 완료");
	}
	
	public void testChatAction() throws Exception {
		Chatting chat = this.chatRoom.getChatting();
		chat = this.chatRoom.decorateActionQueue(chat);
		String usrQ = "아메리카노와 커피프라페 주문해줘";

		System.out.println("* USER msg: " + usrQ);
		JSONObject resJson = chat.sendMessage(usrQ);
		System.out.println("* AI msg: " + resJson.get("message"));
		
		if (resJson.containsKey("actionQueue")) {
			System.out.println("* Action Queue: " + resJson.get("actionQueue").toString());
		}
		
		Object usrFuncRst = resJson.get(OpenAIChatRoom.USER_FUNCTIONS_RESULT);
		if (usrFuncRst != null) {
			Object onJbgRst = ((JSONObject) usrFuncRst).get("on_jangbogo");
			if (onJbgRst != null)
				System.out.println("* AI user_link: " + ((JSONObject) onJbgRst).get("user_link"));
		}

		if (this.chatRoom.closeChat())
			System.out.println("* [" + this.chatRoom.getThreadId() + "] 채팅방 삭제 완료");
	}
	
	public void testSetCurrentViewInfo() throws Exception {
		String viewInfo = "[{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"mega_top_bar_home\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"menu_bar_left\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\"<\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":null,\\\"class\\\":\\\"mega_menu_1\\\",\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\"추천(음료)\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":null,\\\"class\\\":\\\"mega_menu_1\\\",\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\"추천(디저트)\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":null,\\\"class\\\":\\\"mega_menu_1\\\",\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\"커피(HOT)\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":null,\\\"class\\\":\\\"mega_menu_1\\\",\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\"커피(ICE)\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"menu_bar_right\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\">\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"ice_아메리카노\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"유니콘프라페\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"딸기쿠키프라페\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"커피프라페\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"hot_토피넛마끼아또\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"ice_토피넛마끼아또\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"hot_레몬차\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"ice_레몬차\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"hot_사과유자차\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"ice_사과유자차\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"hot_허니자몽블랙티\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"ice_허니자몽블랙티\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":null,\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]},{\\\"tag\\\":\\\"div\\\",\\\"id\\\":\\\"total_price\\\",\\\"class\\\":null,\\\"name\\\":null,\\\"type\\\":null,\\\"role\\\":null,\\\"text\\\":\\\"0원\\n결제하기\\\",\\\"value\\\":null,\\\"placeholder\\\":null,\\\"eventListeners\\\":[\\\"onclick\\\"]}]\r\n";
		String aiMsg = this.chatRoom.setCurrentViewInfo(viewInfo);
		System.out.println("* AI msg(현재화면설정 응답): " + aiMsg);
		
		Chatting chat = this.chatRoom.getChatting();
		chat = this.chatRoom.decorateUXInfo(chat);
		String usrQ = "유니콘프라페 주문해줘";
		
		System.out.println("* USER msg: " + usrQ);
		JSONObject resJson = chat.sendMessage(usrQ);
		System.out.println("* AI msg(액션큐 응답): " + resJson.get("message"));
		
		if (resJson.containsKey("actionQueue")) {
			System.out.println("* Action Queue: " + resJson.get("actionQueue").toString());
		}
		
		Object usrFuncRst = resJson.get(OpenAIChatRoom.USER_FUNCTIONS_RESULT);
		if (usrFuncRst != null) {
			Object onJbgRst = ((JSONObject) usrFuncRst).get("on_jangbogo");
			if (onJbgRst != null)
				System.out.println("* AI user_link: " + ((JSONObject) onJbgRst).get("user_link"));
		}

		if (this.chatRoom.closeChat())
			System.out.println("* [" + this.chatRoom.getThreadId() + "] 채팅방 삭제 완료");
	}
}

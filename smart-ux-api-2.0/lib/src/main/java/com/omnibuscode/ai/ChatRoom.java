package com.omnibuscode.ai;

/**
 * 대화방
 */
public interface ChatRoom {

	public Chatting createChatting();
	
	/**
	 * 대화방 나가기 (openai 에서는 thread 삭제)
	 * @return
	 */
	public boolean closeChat();
}

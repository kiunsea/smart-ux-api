package com.smartuxapi.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.debug.DebugLogger;

/**
 * Chatting 저장소
 */
public interface ChatRoom {

	public String getId();

	/**
	 * Chatting instance 를 반환
	 *
	 * @return
	 */
	public Chatting getChatting();

	/**
	 * ChatRoom 종료<br/>
	 *   - ChatRoo의 구성 객체를 null로 초기화<br/>
	 *   - openai 에서는 thread 삭제
	 *
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean close() throws IOException, ParseException;

	public void setActionQueueHandler(ActionQueueHandler aqHandler);
	public ActionQueueHandler getActionQueueHandler();

	/**
	 * 디버그 로거 설정
	 *
	 * @param debugLogger
	 */
	default void setDebugLogger(DebugLogger debugLogger) {
		// 기본 구현은 아무것도 하지 않음
	}

	/**
	 * 디버그 로거 반환
	 *
	 * @return
	 */
	default DebugLogger getDebugLogger() {
		return null;
	}

}

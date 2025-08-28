package com.smartuxapi.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

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
	
//	/**
//	 * 현재 화면 정보를 저장
//	 * 
//	 * @param viewInfoJson
//	 */
//	public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException;
//	
//	/**
//	 * 현재 화면 정보를 Handler에서 삭제
//	 * 
//	 * @throws IOException
//	 * @throws ParseException
//	 */
//	public void clearCurrentViewInfo() throws IOException, ParseException;
	
	
	public void setActionQueueHandler(ActionQueueHandler aqHandler);
	public ActionQueueHandler getActionQueueHandler();
	
}

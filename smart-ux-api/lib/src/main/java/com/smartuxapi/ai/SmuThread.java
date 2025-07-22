package com.smartuxapi.ai;

import java.io.IOException;

import org.json.simple.parser.ParseException;

/**
 * Messages 저장소
 */
public interface SmuThread {
	
	public String getId();

	/**
	 * Messages instance 를 반환
	 * 
	 * @return
	 */
	public SmuMessages getMessages();
	
	/**
	 * thread 종료 (openai 에서는 thread 삭제)
	 * 
	 * @return
	 * @throws ParseException
	 * @throws IOException
	 */
	public boolean closeThread() throws IOException, ParseException;
	
	/**
	 * 현재 화면 정보를 저장
	 * 
	 * @param viewInfoJson
	 */
	public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException;
}

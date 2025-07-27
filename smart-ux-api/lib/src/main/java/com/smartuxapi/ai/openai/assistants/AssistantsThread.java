package com.smartuxapi.ai.openai.assistants;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;

public class AssistantsThread implements ChatRoom {

	private Logger log = LogManager.getLogger(AssistantsThread.class);

	private AssistantsAPIConnection connApi = null;
	private final ActionQueueHandler aqHandler = new ActionQueueHandler();

    private String idThread = null; // OpenAI에서 부여하는 Thread 고유 키값(thread id)
	private Chatting message = null;

	public AssistantsThread(Assistants assistInfo) throws ParseException {

		this.connApi = new AssistantsAPIConnection(assistInfo);
		try {
			this.idThread = this.connApi.createThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    @Override
    public String getId() {
        return this.idThread;
    }
    
	public void setFunctionMap(Map usrFuncs) {
		// TODO
	}
	
	/**
	 * Message instance 생성후 재사용한다.
	 */
	@Override
	public Chatting getChatting() {
		if (this.message == null) {
			this.message = new AssistantsMessage(this.connApi, this.idThread);
		}
		this.message.setActionQueueHandler(this.aqHandler);
		return this.message;
	}
	
    /**
     * 현재 화면 정보를 Handler에 저장
     */
    public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {
        this.aqHandler.setCurViewInfo(viewInfoJson);
    }
    
    /**
     * 현재 화면 정보를 Handler에서 삭제
     */
    public void clearCurrentViewInfo() throws IOException, ParseException {
        if (this.aqHandler != null) {
            this.aqHandler.setCurViewInfo(null);
        }
    }

	/**
	 * 대화방 나가기
	 */
	public boolean close() throws IOException, ParseException {
		boolean deleted = this.connApi.deleteThread(idThread);
		log.debug("delete thread [" + idThread + "] - " + deleted);
		return deleted;
	}

}

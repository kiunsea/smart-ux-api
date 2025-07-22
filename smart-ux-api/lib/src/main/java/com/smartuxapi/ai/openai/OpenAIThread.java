package com.smartuxapi.ai.openai;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.SmuThread;
import com.smartuxapi.ai.SmuMessages;
import com.smartuxapi.ai.openai.assistants.Assistant;
import com.smartuxapi.ai.openai.assistants.AssistantAPIConnection;

public class OpenAIThread implements SmuThread {

	private Logger log = LogManager.getLogger(OpenAIThread.class);

	private AssistantAPIConnection connApi = null;
	private ActionQueueHandler aqHandler = null;

    private String idThread = null; // OpenAI에서 부여하는 채팅방 고유 키값(thread id)
	private SmuMessages messages = null;

	public OpenAIThread(Assistant assistInfo) throws ParseException {

		this.connApi = new AssistantAPIConnection(assistInfo);
		try {
			this.idThread = this.connApi.createThread();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getThreadId() {
		return this.idThread;
	}

	public void setFunctionMap(Map usrFuncs) {
		// TODO
	}
	
	/**
	 * Messages instance 생성후 재사용한다.
	 */
	@Override
	public SmuMessages getMessages() {
		if (this.messages == null) {
			this.messages = new OpenAIMessages(this.connApi, this.idThread);
		}
		this.messages.setActionQueueHandler(this.aqHandler);
		return this.messages;
	}
	
    /**
     * 현재 화면 정보를 Handler에 저장
     */
    public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {
        if (this.aqHandler == null) {
            this.aqHandler = new ActionQueueHandler();
        }
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
	public boolean closeThread() throws IOException, ParseException {
		boolean deleted = this.connApi.deleteThread(idThread);
		log.debug("delete thread [" + idThread + "] - " + deleted);
		return deleted;
	}

    @Override
    public String getId() {
        return this.idThread;
    }
}

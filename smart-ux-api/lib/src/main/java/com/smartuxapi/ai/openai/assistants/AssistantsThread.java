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
	private ActionQueueHandler aqHandler = null;

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
	 * 대화방 나가기
	 */
	public boolean close() throws IOException, ParseException {
		boolean deleted = this.connApi.deleteThread(idThread);
		log.debug("delete thread [" + idThread + "] - " + deleted);
		return deleted;
	}

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
    }

    @Override
    public ActionQueueHandler getActionQueueHandler() {
        return this.aqHandler;
    }

}

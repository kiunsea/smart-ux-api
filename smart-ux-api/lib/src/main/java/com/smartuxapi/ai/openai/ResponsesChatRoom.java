package com.smartuxapi.ai.openai;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.ConversationHistory;

public class ResponsesChatRoom implements ChatRoom {

    private ResponsesAPIConnection connApi = null;
    private ResponsesChatting message = null;
    
    private final ActionQueueHandler aqHandler = new ActionQueueHandler();
    private final ConversationHistory conversationHistory = new ConversationHistory();
    private final String threadId = UUID.randomUUID().toString();
    
    /**
     * @param apiKey
     * @param modelName
     */
    public ResponsesChatRoom(String apiKey, String modelName) {
        this.connApi = new ResponsesAPIConnection(apiKey, modelName);
    }
    
    @Override
    public String getId() {
        return threadId;
    }
    
    @Override
    public Chatting getChatting() {
        if (this.message == null) {
            this.message = new ResponsesChatting(this.connApi);
        }
        this.message.setActionQueueHandler(this.aqHandler);
        return this.message;
    }
    
    @Override
    public boolean close() throws IOException, ParseException {
        this.connApi = null;
        this.message = null;
        this.conversationHistory.clearHistory();
        return true;
    }
    
    @Override
    public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {
        this.aqHandler.setCurViewInfo(viewInfoJson);
    }

    @Override
    public void clearCurrentViewInfo() throws IOException, ParseException {
        if (this.aqHandler != null) {
            this.aqHandler.setCurViewInfo(null);
        }
    }
}

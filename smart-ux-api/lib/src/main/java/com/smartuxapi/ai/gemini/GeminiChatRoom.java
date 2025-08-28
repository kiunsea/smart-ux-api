package com.smartuxapi.ai.gemini;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.ChatRoom;

public class GeminiChatRoom implements ChatRoom {

    private final ConversationHistory conversationHistory = new ConversationHistory();
    private final String threadId = UUID.randomUUID().toString();
    
    private GeminiAPIConnection connApi = null;
    private GeminiChatting message = null;
    private ActionQueueHandler aqHandler = null;

    
    /**
     * @param apiKey
     * @param modelName
     */
    public GeminiChatRoom(String apiKey, String modelName) {
        this.connApi = new GeminiAPIConnection(apiKey, modelName);
    }
    
    @Override
    public String getId() {
        return threadId;
    }
    
    @Override
    public Chatting getChatting() {
        if (this.message == null) {
            this.message = new GeminiChatting(this.connApi);
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
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
    }

    @Override
    public ActionQueueHandler getActionQueueHandler() {
        return this.aqHandler;
    }
}

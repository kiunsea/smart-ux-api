package com.smartuxapi.ai.gemini;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.MessageHistory;
import com.smartuxapi.ai.SmuMessage;
import com.smartuxapi.ai.SmuThread;

public class GeminiThread implements SmuThread {

    private GeminiAPIConnection connApi = null;
    private GeminiMessage message = null;
    
    private final ActionQueueHandler aqHandler = new ActionQueueHandler();
    private final MessageHistory messageHistory = new MessageHistory();
    private final String threadId = UUID.randomUUID().toString();
    
    public GeminiThread(String apiKey, String modelName) {
        this.connApi = new GeminiAPIConnection(apiKey, modelName);
    }
    
    @Override
    public String getId() {
        return threadId;
    }
    
    @Override
    public SmuMessage getMessage() {
        if (this.message == null) {
            this.message = new GeminiMessage(this.connApi);
        }
        this.message.setActionQueueHandler(this.aqHandler);
        return this.message;
    }
    
    @Override
    public boolean closeThread() throws IOException, ParseException {
        this.connApi = null;
        this.message = null;
        this.messageHistory.clearHistory();
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

package com.smartuxapi.ai.gemini;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.SmuThread;
import com.smartuxapi.ai.SmuMessages;

/**
 * TODO chatroom 은 chatting 을 생성하고 현재 화면 정보를 실시간 저장해야 한다.
 */
public class GeminiThread implements SmuThread {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SmuMessages getMessages() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean closeThread() throws IOException, ParseException {
        // TODO Auto-generated method stub
        return false;
    }

    public SmuMessages decorateUXInfo(SmuMessages chat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {
        // TODO Auto-generated method stub
        
    }

}

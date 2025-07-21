package com.smartuxapi.ai.gemini;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;

/**
 * TODO chatroom 은 chatting 을 생성하고 현재 화면 정보를 실시간 저장해야 한다.
 */
public class GeminiChatRoom implements ChatRoom {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Chatting getChatting() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean closeChat() throws IOException, ParseException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Chatting decorateUXInfo(Chatting chat) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCurrentViewInfo(String viewInfoJson) throws IOException, ParseException {
        // TODO Auto-generated method stub
        
    }

}

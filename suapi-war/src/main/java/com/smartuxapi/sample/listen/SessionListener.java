package com.smartuxapi.sample.listen;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ChatRoom;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class SessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
//        System.out.println("세션 생성됨: " + se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("세션 종료됨: " + se.getSession().getId());

        // ✅ 여기에 사용자 세션 종료 시 처리할 로직 작성
        Object usObj = se.getSession().getAttribute("CHAT_ROOM");
		if (usObj != null) {
			ChatRoom cr = (ChatRoom) usObj;
			try {
				System.out.println("Closed ChatRoom: " + cr.getId());
				cr.closeChat();
			} catch (IOException | ParseException e) {
				e.printStackTrace();
			}
		}
        
    }
}

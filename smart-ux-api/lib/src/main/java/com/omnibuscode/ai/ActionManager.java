package com.omnibuscode.ai;

import java.util.Queue;

import com.omnibuscode.ai.ux.Action;

import jakarta.servlet.http.HttpSession;

/**
 * UX 에 전달할 Action 목록을 생성하고 관리한다.
 */
public class ActionManager {

	public static String ACTION_QUEUE = "ACTIONS";
	private HttpSession sess = null;
	
	/**
	 * 서블릿 표준 세션 인터페이스의 HttpSession 인스턴스를 저장한다.
	 * @param sess
	 */
	public void setHttpSession(HttpSession sess) {
		this.sess = sess;
	}
	
	public Action pollAction() {
		if (this.sess != null) {
			Queue<Action> actionQueue = (Queue<Action>) this.sess.getAttribute(ActionManager.ACTION_QUEUE);
			if (actionQueue.isEmpty()) {
				return actionQueue.poll();
			}
		}

		return null;
	}
	
	public boolean isEmpty() {
		if (this.sess != null) {
			Queue<Action> actionQueue = (Queue<Action>) this.sess.getAttribute(ActionManager.ACTION_QUEUE);
			return actionQueue.isEmpty();
		}

		return true;
	}
	
}

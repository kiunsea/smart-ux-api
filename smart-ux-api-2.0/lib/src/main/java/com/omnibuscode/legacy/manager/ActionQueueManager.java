package com.omnibuscode.legacy.manager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import jakarta.servlet.http.HttpSession;

/**
 * UX 에 전달할 Action Queue를 사용자 세션에 생성하고 관리한다.
 */
public class ActionQueueManager {

    public static String ACTION_QUEUE = "ACTIONS";
    
    private HttpSession sess = null;
    private JSONArray actionQueue = null;
    
    public ActionQueueManager(HttpSession sess) {
        this.sess = sess;
        Object aqObj = this.sess.getAttribute(ACTION_QUEUE);
        if (aqObj != null) {
            this.actionQueue = (JSONArray) aqObj;
        }
    }
            
	public ActionQueueManager(HttpSession sess, JSONArray actionArr) {
		this.sess = sess;
		this.setQueue(actionArr);
	}
    
    /**
     * action queue 를 초기화
     * @param actionArr
     */
	public void setQueue(JSONArray actionArr) {
		this.actionQueue = actionArr;
		if (this.sess != null) {
			this.sess.setAttribute(ACTION_QUEUE, this.actionQueue);
		}
	}

	/**
	 * 세션에 저장된 action queue 에서 현재 진행해야 할 queue 를 반환
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONArray currentQueue() {
		JSONArray currQueue = null;

		if (this.sess != null) {
			this.actionQueue = (JSONArray) this.sess.getAttribute(ACTION_QUEUE);
			if (this.actionQueue != null) {
				while (!this.actionQueue.isEmpty()) {
					if (currQueue == null) {
						currQueue = new JSONArray();
					}
					JSONObject actionJson = (JSONObject) this.actionQueue.remove(0);
					currQueue.add(actionJson);
					String type = actionJson.containsKey("type") ? actionJson.get("type").toString() : null;
					if ("navigate".equals(type)) {
						break;
					}
				}
			}
		}
		return currQueue;
	}
    
	public boolean isEmpty() {
		if (this.sess != null) {
			JSONArray actionQueue = (JSONArray) this.sess.getAttribute(ACTION_QUEUE);
			return (actionQueue == null || actionQueue.isEmpty());
		}

		return true;
	}
    
}

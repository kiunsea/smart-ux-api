package com.omnibuscode.legacy.ux.action;

import com.omnibuscode.legacy.ux.Action;

/**
 * target 과 message 를 관리하는 클래스
 */
public class MessageAction implements Action {

	@Override
	public String getActionType() {
		return this.ACTION_TYPE_MESSAGE;
	}

}

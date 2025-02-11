package com.omnibuscode.ai.ux.action;

import com.omnibuscode.ai.ux.Action;

/**
 * target 과 message 를 관리하는 클래스<br/>
 */
public class MessageAction implements Action {

	@Override
	public String getActionType() {
		return this.ACTION_TYPE_MESSAGE;
	}

}

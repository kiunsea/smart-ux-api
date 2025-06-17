package com.omnibuscode.ai.ux.action;

import com.omnibuscode.ai.ux.Action;

/**
 * CHECK 입력값 검증은 어디에 사용할지 몰라서 일단 보류한다
 */
public class ValidAction implements Action {

	@Override
	public String getActionType() {
		return this.ACTION_TYPE_VALID;
	}

}

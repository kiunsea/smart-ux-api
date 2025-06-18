package com.omnibuscode.legacy.ux.action;

import com.omnibuscode.legacy.ux.Action;

/**
 * 페이지 이동
 */
public class NavigateAction implements Action {

	@Override
	public String getActionType() {
		return this.ACTION_TYPE_NAVIGATE;
	}

}

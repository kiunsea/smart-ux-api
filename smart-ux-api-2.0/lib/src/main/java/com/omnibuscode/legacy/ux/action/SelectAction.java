package com.omnibuscode.legacy.ux.action;

import com.omnibuscode.legacy.ux.Action;

/**
 * 콤보나 리스트의 아이템을 선택
 */
public class SelectAction implements Action {

	@Override
	public String getActionType() {
		return this.ACTION_TYPE_SELECT;
	}

}

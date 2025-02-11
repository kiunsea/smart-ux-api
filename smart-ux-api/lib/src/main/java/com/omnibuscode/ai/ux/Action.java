package com.omnibuscode.ai.ux;

public interface Action {
	
	public static String ACTION_TYPE_AUTOFILL = "AutofillAction";
	public static String ACTION_TYPE_CHECKTX = "CheckTxAction";
	public static String ACTION_TYPE_NAVIGATE = "NavigateAction";
	public static String ACTION_TYPE_MESSAGE = "MessageAction";
	public static String ACTION_TYPE_SELECT = "SelectAction";
	public static String ACTION_TYPE_VALID = "ValidAction";
	

	/**
	 * @return autofill, checktx, navigate, message, select, valid
	 */
	public String getActionType();
}

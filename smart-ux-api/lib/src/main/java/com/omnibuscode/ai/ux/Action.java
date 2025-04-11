package com.omnibuscode.ai.ux;

public interface Action {
    
    public static String ACTION_TYPE_INPUTVAL = "InputValAction";
    public static String ACTION_TYPE_CHECKTX = "CheckTxAction";
    public static String ACTION_TYPE_NAVIGATE = "NavigateAction";
    public static String ACTION_TYPE_MESSAGE = "MessageAction";
    public static String ACTION_TYPE_SELECT = "SelectAction";
    public static String ACTION_TYPE_VALID = "ValidAction";

    /**
     * @return inputval, checktx, navigate, message, select, valid
     */
    public String getActionType();
}

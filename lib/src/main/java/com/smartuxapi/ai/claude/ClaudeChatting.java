package com.smartuxapi.ai.claude;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.NoOpCacheStrategy;
import com.smartuxapi.ai.debug.DebugLogger;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.tools.ToolCall;
import com.smartuxapi.ai.tools.ToolDefinition;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolResult;
import com.smartuxapi.ai.tools.ToolTurnResult;

/**
 * Anthropic Claude Messages API를 사용하는 {@link Chatting} 구현체.
 *
 * @since 0.10.0
 */
public class ClaudeChatting implements Chatting {

    private static final Logger LOG = LogManager.getLogger(ClaudeChatting.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private ClaudeAPIConnection connApi = null;
    private ConversationHistory conversationHistory = null;
    private ActionQueueHandler aqHandler = null;
    private DebugLogger debugLogger = null;
    private String chatRoomId = null;
    private CacheStrategy cacheStrategy = NoOpCacheStrategy.INSTANCE;

    public ClaudeChatting(ClaudeAPIConnection connApi) {
        this.connApi = connApi;
        this.conversationHistory = new ConversationHistory();
    }

    /**
     * 디버그 로거 설정
     */
    public void setDebugLogger(DebugLogger debugLogger, String chatRoomId) {
        this.debugLogger = debugLogger;
        this.chatRoomId = chatRoomId;
    }

    @Override
    public org.json.simple.JSONObject sendPrompt(String userMsg) throws Exception {
        return sendInternal(userMsg, null);
    }

    /**
     * Structured Output — JSON Schema 강제 응답.
     * @since 0.10.0
     */
    @Override
    public org.json.simple.JSONObject sendPromptWithSchema(String userMsg, ResponseSchema schema) throws Exception {
        if (schema == null) return sendPrompt(userMsg);
        return sendInternal(userMsg, schema);
    }

    private org.json.simple.JSONObject sendInternal(String userMsg, ResponseSchema schema) throws Exception {

        boolean reqActionQueue = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();

        String usrPrompt, curViewPrompt = null;

        if (reqActionQueue) {
            usrPrompt = this.aqHandler.getActionQueuePrompt(userMsg);
            curViewPrompt = this.aqHandler.getCurViewPrompt();
        } else {
            usrPrompt = userMsg;
        }

        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.startTurn(chatRoomId, userMsg, usrPrompt, curViewPrompt);
        }

        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);

        String aiResponse = this.connApi.generateContent(convHistory, this.cacheStrategy, schema);

        this.conversationHistory.addModelResponse(aiResponse);

        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        org.json.simple.JSONObject resJson = new org.json.simple.JSONObject();
        resJson.put("message", aiResponse);

        JsonNode actionQueue = null;
        if (this.aqHandler != null) {
            JsonNode aqObj = this.aqHandler.getActionQueue(aiResponse);
            if (aqObj != null && aqObj.hasNonNull("action_queue")) {
                actionQueue = aqObj.get("action_queue");
                resJson.put("action_queue", actionQueue);
            } else {
                actionQueue = aqObj;
                resJson.put("action_queue", aqObj);
            }
        }

        if (schema != null) {
            JsonNode structured = null;
            try {
                structured = JSON_MAPPER.readTree(aiResponse);
            } catch (Exception parseEx) {
                LOG.warn("Structured output JSON 파싱 실패 (schema={}): {}", schema.getName(), parseEx.getMessage());
            }
            resJson.put("structured", structured);
        }

        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.completeTurn(chatRoomId, aiResponse, actionQueue);
        }

        return resJson;
    }

    /**
     * Dummy — Claude API에서는 불필요한 함수이다.
     */
    @Override
    public Set<String> getMessageIdSet() {
        return null;
    }

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
    }

    @Override
    public void setCacheStrategy(CacheStrategy strategy) {
        this.cacheStrategy = (strategy == null) ? NoOpCacheStrategy.INSTANCE : strategy;
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return this.cacheStrategy;
    }

    /**
     * 캐시 힌트를 적용한다. Claude 경로에서는 전략의 prime() 호출만 수행하며,
     * 실제 {@code cache_control} 부여는 {@link ClaudeAPIConnection} 이 요청 빌드 시 처리한다.
     * null 전달 시 기존 캐시를 해제(invalidate)한다.
     */
    @Override
    public void applyCacheHint(CacheHint hint) throws Exception {
        if (hint == null) {
            this.cacheStrategy.invalidate();
            return;
        }
        this.cacheStrategy.prime(hint);
    }

    // ========================================================================
    // Tool Use
    // ========================================================================

    /**
     * Auto-loop tool use. 최대 {@link Chatting#DEFAULT_MAX_TOOL_ROUNDS} 라운드까지 반복.
     */
    @Override
    public org.json.simple.JSONObject sendPromptWithTools(String userMsg, ToolRegistry tools) throws Exception {
        if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);

        boolean reqActionQueue = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();
        String usrPrompt = reqActionQueue ? this.aqHandler.getActionQueuePrompt(userMsg) : userMsg;
        String curViewPrompt = reqActionQueue ? this.aqHandler.getCurViewPrompt() : null;
        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.startTurn(chatRoomId, userMsg, usrPrompt, curViewPrompt);
        }
        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);
        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        List<org.json.simple.JSONObject> executedCalls = new ArrayList<>();
        String finalText = null;
        for (int round = 0; round < DEFAULT_MAX_TOOL_ROUNDS; round++) {
            JSONArray reqMessages = round == 0 ? convHistory : this.conversationHistory.getHistory();
            ToolTurnResult turn = this.connApi.generateContentWithTools(reqMessages, this.cacheStrategy, tools);

            if (turn.isFinal()) {
                finalText = turn.getFinalText();
                this.conversationHistory.addModelResponse(finalText);
                break;
            }
            // assistant content blocks (tool_use 포함) 를 히스토리에 보존
            this.conversationHistory.addAssistantContentBlocks(new JSONArray(turn.getRawAssistantPayload()));

            // 한 턴의 모든 tool_result 를 단일 user 메시지로 묶는다
            JSONArray toolResults = new JSONArray();
            for (ToolCall call : turn.getToolCalls()) {
                ToolResult result = executeToolCall(call, tools);
                toolResults.put(buildToolResultBlock(call.getId(), result));
                executedCalls.add(toExecutedCallJson(call, result));
            }
            this.conversationHistory.addToolResults(toolResults);
        }

        if (finalText == null) {
            LOG.warn("Tool use max_tool_rounds={} 초과. 현재 누적 호출 {} 개, 마지막 텍스트 응답 요청.",
                    DEFAULT_MAX_TOOL_ROUNDS, executedCalls.size());
            finalText = this.connApi.generateContent(this.conversationHistory.getHistory(), this.cacheStrategy);
            this.conversationHistory.addModelResponse(finalText);
        }

        return buildFinalJson(finalText, executedCalls);
    }

    /**
     * Manual mode — 첫 라운드만 실행하고 tool_calls 가 있으면 반환한다.
     */
    @Override
    public org.json.simple.JSONObject sendPromptExpectingToolCalls(String userMsg, ToolRegistry tools) throws Exception {
        if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);

        boolean reqActionQueue = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();
        String usrPrompt = reqActionQueue ? this.aqHandler.getActionQueuePrompt(userMsg) : userMsg;
        String curViewPrompt = reqActionQueue ? this.aqHandler.getCurViewPrompt() : null;
        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);
        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        ToolTurnResult turn = this.connApi.generateContentWithTools(convHistory, this.cacheStrategy, tools);
        if (turn.isFinal()) {
            this.conversationHistory.addModelResponse(turn.getFinalText());
            return buildFinalJson(turn.getFinalText(), new ArrayList<>());
        }
        this.conversationHistory.addAssistantContentBlocks(new JSONArray(turn.getRawAssistantPayload()));
        return buildPendingJson(turn.getToolCalls());
    }

    /**
     * Manual mode — 호출자가 실행한 ToolResult 들을 제출하고 한 라운드 진행.
     */
    @Override
    public org.json.simple.JSONObject continueWithToolResults(List<ToolResult> results, ToolRegistry tools) throws Exception {
        if (results != null && !results.isEmpty()) {
            JSONArray toolResults = new JSONArray();
            for (ToolResult r : results) {
                toolResults.put(buildToolResultBlock(r.getCallId(), r));
            }
            this.conversationHistory.addToolResults(toolResults);
        }
        ToolTurnResult turn = this.connApi.generateContentWithTools(
                this.conversationHistory.getHistory(), this.cacheStrategy, tools);
        if (turn.isFinal()) {
            this.conversationHistory.addModelResponse(turn.getFinalText());
            return buildFinalJson(turn.getFinalText(), new ArrayList<>());
        }
        this.conversationHistory.addAssistantContentBlocks(new JSONArray(turn.getRawAssistantPayload()));
        return buildPendingJson(turn.getToolCalls());
    }

    // ----- tool use 내부 헬퍼 -----

    private ToolResult executeToolCall(ToolCall call, ToolRegistry tools) {
        ToolDefinition def = tools.get(call.getToolName());
        if (def == null) {
            return ToolResult.error(call.getId(), "Unregistered tool: " + call.getToolName());
        }
        try {
            return enforceOutputSizeCap(def.getHandler().invoke(call));
        } catch (Exception ex) {
            LOG.warn("Tool handler 실패 [{}]: {}", call.getToolName(), ex.getMessage());
            return ToolResult.error(call.getId(), ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private ToolResult enforceOutputSizeCap(ToolResult r) {
        if (r == null || r.getOutput() == null) return r;
        String s = r.getOutput().toString();
        if (s.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= ToolResult.MAX_OUTPUT_BYTES) {
            return r;
        }
        LOG.warn("Tool output 크기 상한 {}B 초과 — 자동 축약", ToolResult.MAX_OUTPUT_BYTES);
        return ToolResult.error(r.getCallId(),
                "output truncated: size exceeded " + ToolResult.MAX_OUTPUT_BYTES + " bytes");
    }

    /**
     * Anthropic {@code tool_result} content 블록 생성.
     */
    private JSONObject buildToolResultBlock(String toolUseId, ToolResult result) {
        JSONObject block = new JSONObject();
        block.put("type", "tool_result");
        block.put("tool_use_id", toolUseId);
        if (result.isError()) {
            block.put("is_error", true);
            block.put("content", result.getErrorMessage() == null ? "error" : result.getErrorMessage());
        } else {
            block.put("content", result.getOutput() == null ? "" : result.getOutput().toString());
        }
        return block;
    }

    private org.json.simple.JSONObject toExecutedCallJson(ToolCall call, ToolResult result) {
        org.json.simple.JSONObject o = new org.json.simple.JSONObject();
        o.put("id", call.getId());
        o.put("toolName", call.getToolName());
        o.put("arguments", call.getArguments() == null ? null : call.getArguments().toString());
        o.put("result", result.getOutput() == null ? null : result.getOutput().toString());
        o.put("isError", result.isError());
        return o;
    }

    @SuppressWarnings("unchecked")
    private org.json.simple.JSONObject buildFinalJson(String finalText, List<org.json.simple.JSONObject> executedCalls) {
        org.json.simple.JSONObject res = new org.json.simple.JSONObject();
        res.put("message", finalText);
        if (this.aqHandler != null) {
            JsonNode aq = this.aqHandler.getActionQueue(finalText);
            if (aq != null && aq.hasNonNull("action_queue")) {
                res.put("action_queue", aq.get("action_queue"));
            } else {
                res.put("action_queue", aq);
            }
        }
        org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
        arr.addAll(executedCalls);
        res.put("tool_calls", arr);
        return res;
    }

    @SuppressWarnings("unchecked")
    private org.json.simple.JSONObject buildPendingJson(List<ToolCall> calls) {
        org.json.simple.JSONObject res = new org.json.simple.JSONObject();
        res.put("message", null);
        org.json.simple.JSONArray arr = new org.json.simple.JSONArray();
        for (ToolCall c : calls) {
            org.json.simple.JSONObject o = new org.json.simple.JSONObject();
            o.put("id", c.getId());
            o.put("toolName", c.getToolName());
            o.put("arguments", c.getArguments() == null ? null : c.getArguments().toString());
            arr.add(o);
        }
        res.put("tool_calls", arr);
        res.put("pending", true);
        return res;
    }

    /** 디버깅/테스트용 내부 대화 기록 접근자. */
    ConversationHistory getConversationHistory() {
        return this.conversationHistory;
    }
}

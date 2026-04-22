package com.smartuxapi.ai.openai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * OpenAI Responses API를 사용하는 Chatting 구현체
 */
public class ResponsesChatting implements Chatting {

    private static final Logger LOG = LogManager.getLogger(ResponsesChatting.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private ResponsesAPIConnection connApi = null;
    private ConversationHistory conversationHistory = null;
    private ActionQueueHandler aqHandler = null;
    private DebugLogger debugLogger = null;
    private String chatRoomId = null;
    private CacheStrategy cacheStrategy = NoOpCacheStrategy.INSTANCE;

    public ResponsesChatting(ResponsesAPIConnection connApi) {
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

    /**
     * 대화 히스토리를 관리하고 AI에게 메세지 전송시 마지막에 현재 화면 정보도 함께 전달
     */
    @Override
    public org.json.simple.JSONObject sendPrompt(String userMsg) throws Exception {
        return sendInternal(userMsg, null);
    }

    /**
     * Structured Output — JSON Schema 강제 응답.
     * @since 0.8.0
     */
    @Override
    public org.json.simple.JSONObject sendPromptWithSchema(String userMsg, ResponseSchema schema) throws Exception {
        if (schema == null) return sendPrompt(userMsg);
        return sendInternal(userMsg, schema);
    }

    private org.json.simple.JSONObject sendInternal(String userMsg, ResponseSchema schema) throws Exception {

        boolean reqActionQueue = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();

        String usrPrompt, curViewPrompt = null;

        // Action Queue 요청 Prompt 작성 및 전달
        if (reqActionQueue) {
            usrPrompt = this.aqHandler.getActionQueuePrompt(userMsg);
            curViewPrompt = this.aqHandler.getCurViewPrompt();
        } else {
            usrPrompt = userMsg;
        }

        // 디버그 로깅: 턴 시작
        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.startTurn(chatRoomId, userMsg, usrPrompt, curViewPrompt);
        }

        // 1. 사용자 메시지를 대화 기록에 추가하고, AI에 보낼 전체 기록을 가져옴
        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);

        // 2. Responses API 호출 (전체 대화 기록 전송, 캐시 전략 + 선택적 응답 스키마 주입)
        String aiResponse = this.connApi.generateContent(convHistory, this.cacheStrategy, schema);

        // 3. AI 응답을 대화 기록에 추가
        this.conversationHistory.addModelResponse(aiResponse);

        // 4. 화면 정보가 변경되어 프롬프트에 포함된 경우, 전송 완료로 표시
        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        // Action Queue 메세지 전달
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

        // Structured Output — 응답 원문을 JsonNode 로 파싱하여 structured 필드에 병기
        if (schema != null) {
            JsonNode structured = null;
            try {
                structured = JSON_MAPPER.readTree(aiResponse);
            } catch (Exception parseEx) {
                LOG.warn("Structured output JSON 파싱 실패 (schema={}): {}", schema.getName(), parseEx.getMessage());
            }
            resJson.put("structured", structured);
        }

        // 디버그 로깅: 턴 완료
        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.completeTurn(chatRoomId, aiResponse, actionQueue);
        }

        return resJson;
    }

    /**
     * Dummy
     * OpenAI Responses API에서는 불필요한 함수이다.
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
     * 캐시 힌트를 적용한다. OpenAI 경로에서는 전략의 prime() 호출과 함께
     * {@link ConversationHistory#setCacheablePrefix(String)} 로 대화 프리픽스에도 반영한다.
     * null 전달 시 양쪽 모두 해제.
     */
    @Override
    public void applyCacheHint(CacheHint hint) throws Exception {
        if (hint == null) {
            this.conversationHistory.setCacheablePrefix(null);
            this.cacheStrategy.invalidate();
            return;
        }
        this.cacheStrategy.prime(hint);
        this.conversationHistory.setCacheablePrefix(hint.getContent());
    }

    // ========================================================================
    // Tool Use (v0.8.0 T2-b)
    // ========================================================================

    /**
     * Auto-loop tool use. 최대 {@link Chatting#DEFAULT_MAX_TOOL_ROUNDS} 라운드까지 반복.
     */
    @Override
    public org.json.simple.JSONObject sendPromptWithTools(String userMsg, ToolRegistry tools) throws Exception {
        if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);

        // 1. 사용자 메시지 히스토리 추가 (curView 포함)
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

        // 2. Tool-use 루프
        List<org.json.simple.JSONObject> executedCalls = new ArrayList<>();
        String finalText = null;
        for (int round = 0; round < DEFAULT_MAX_TOOL_ROUNDS; round++) {
            // 히스토리 snapshot 을 convHistory 로 재빌드 — 이미 누적된 상태
            JSONArray requestInput = snapshotForTools(round, convHistory);
            ToolTurnResult turn = this.connApi.generateContentWithTools(requestInput, this.cacheStrategy, tools);

            if (turn.isFinal()) {
                finalText = turn.getFinalText();
                this.conversationHistory.addModelResponse(finalText);
                break;
            }
            // tool 호출 — 원본 응답 output 을 히스토리에 보존 (다음 라운드 재전송 위해)
            JSONArray assistantOutput = new JSONArray(turn.getRawAssistantPayload());
            this.conversationHistory.addAssistantOutputItems(assistantOutput);

            for (ToolCall call : turn.getToolCalls()) {
                ToolResult result = executeToolCall(call, tools);
                String outputStr = serializeToolOutput(result);
                this.conversationHistory.addToolResult(call.getId(), outputStr);
                executedCalls.add(toExecutedCallJson(call, result));
            }
        }

        if (finalText == null) {
            // max rounds 초과 — 마지막 응답을 강제 요청
            LOG.warn("Tool use max_tool_rounds={} 초과. 현재 누적 호출 {} 개, 마지막 텍스트 응답 요청.",
                    DEFAULT_MAX_TOOL_ROUNDS, executedCalls.size());
            // tool 없이 마지막 호출
            String textOnly = this.connApi.generateContent(this.conversationHistory.getHistory(), this.cacheStrategy);
            finalText = textOnly;
            this.conversationHistory.addModelResponse(finalText);
        }

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
        org.json.simple.JSONArray callsArr = new org.json.simple.JSONArray();
        callsArr.addAll(executedCalls);
        res.put("tool_calls", callsArr);

        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.completeTurn(chatRoomId, finalText, null);
        }
        return res;
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
            return finalResponseJson(turn.getFinalText());
        }
        // tool 호출 pending — 히스토리에 assistant output 저장
        this.conversationHistory.addAssistantOutputItems(new JSONArray(turn.getRawAssistantPayload()));
        return pendingResponseJson(turn.getToolCalls());
    }

    /**
     * Manual mode — 호출자가 실행한 ToolResult 들을 제출하고 한 라운드 진행.
     */
    @Override
    public org.json.simple.JSONObject continueWithToolResults(List<ToolResult> results, ToolRegistry tools) throws Exception {
        if (results != null) {
            for (ToolResult r : results) {
                this.conversationHistory.addToolResult(r.getCallId(), serializeToolOutput(r));
            }
        }
        ToolTurnResult turn = this.connApi.generateContentWithTools(
                this.conversationHistory.getHistory(), this.cacheStrategy, tools);
        if (turn.isFinal()) {
            this.conversationHistory.addModelResponse(turn.getFinalText());
            return finalResponseJson(turn.getFinalText());
        }
        this.conversationHistory.addAssistantOutputItems(new JSONArray(turn.getRawAssistantPayload()));
        return pendingResponseJson(turn.getToolCalls());
    }

    // ----- tool use 내부 헬퍼 -----

    private JSONArray snapshotForTools(int round, JSONArray firstRoundHistory) {
        // 1 번째 라운드 는 addUserPrompt 가 만들어준 배열 사용 (curView 프리픽스 포함)
        // 2 번째 이후 라운드 는 누적된 내부 history 로 충분 (curView 는 이미 소비됨)
        return round == 0 ? firstRoundHistory : this.conversationHistory.getHistory();
    }

    private ToolResult executeToolCall(ToolCall call, ToolRegistry tools) {
        ToolDefinition def = tools.get(call.getToolName());
        if (def == null) {
            return ToolResult.error(call.getId(), "Unregistered tool: " + call.getToolName());
        }
        try {
            ToolResult out = def.getHandler().invoke(call);
            return enforceOutputSizeCap(out);
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

    private String serializeToolOutput(ToolResult r) {
        if (r.isError()) return r.getErrorMessage() == null ? "error" : r.getErrorMessage();
        return r.getOutput() == null ? "" : r.getOutput().toString();
    }

    private org.json.simple.JSONObject toExecutedCallJson(ToolCall call, ToolResult result) {
        org.json.simple.JSONObject obj = new org.json.simple.JSONObject();
        obj.put("id", call.getId());
        obj.put("toolName", call.getToolName());
        obj.put("arguments", call.getArguments() == null ? null : call.getArguments().toString());
        obj.put("result", result.getOutput() == null ? null : result.getOutput().toString());
        obj.put("isError", result.isError());
        return obj;
    }

    @SuppressWarnings("unchecked")
    private org.json.simple.JSONObject finalResponseJson(String text) {
        org.json.simple.JSONObject res = new org.json.simple.JSONObject();
        res.put("message", text);
        if (this.aqHandler != null) {
            JsonNode aq = this.aqHandler.getActionQueue(text);
            if (aq != null && aq.hasNonNull("action_queue")) {
                res.put("action_queue", aq.get("action_queue"));
            } else {
                res.put("action_queue", aq);
            }
        }
        res.put("tool_calls", new org.json.simple.JSONArray());
        return res;
    }

    @SuppressWarnings("unchecked")
    private org.json.simple.JSONObject pendingResponseJson(List<ToolCall> calls) {
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
package com.smartuxapi.ai.gemini;

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
 * Gemini API를 사용하는 Chatting 구현체
 */
public class GeminiChatting implements Chatting {

    private static final Logger LOG = LogManager.getLogger(GeminiChatting.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private GeminiAPIConnection connApi = null;
    private ConversationHistory conversationHistory = null;
    private ActionQueueHandler aqHandler = null;
    private DebugLogger debugLogger = null;
    private String chatRoomId = null;
    private CacheStrategy cacheStrategy = NoOpCacheStrategy.INSTANCE;

    public GeminiChatting(GeminiAPIConnection connApi) {
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
     * 대화 히스토리를 관리하고 Gemini에게 메세지 전송시 마지막에 현재 화면 정보도 함께 전달
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

        // 1. 사용자 메시지를 대화 기록에 추가하고, Gemini에 보낼 전체 기록을 가져옴
        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);

        // 2. Gemini API 호출 (전체 대화 기록 전송, 캐시 전략 + 선택적 응답 스키마 주입)
        String geminiResponse = this.connApi.generateContent(convHistory, this.cacheStrategy, schema);

        // 3. Gemini 응답을 대화 기록에 추가
        this.conversationHistory.addModelResponse(geminiResponse);

        // 4. 화면 정보가 변경되어 프롬프트에 포함된 경우, 전송 완료로 표시
        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        // Action Queue 메세지 전달
        org.json.simple.JSONObject resJson = new org.json.simple.JSONObject();
        resJson.put("message", geminiResponse);

        JsonNode actionQueue = null;
        if (this.aqHandler != null) {
            JsonNode aqObj = this.aqHandler.getActionQueue(geminiResponse);
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
                structured = JSON_MAPPER.readTree(geminiResponse);
            } catch (Exception parseEx) {
                LOG.warn("Structured output JSON 파싱 실패 (schema={}): {}", schema.getName(), parseEx.getMessage());
            }
            resJson.put("structured", structured);
        }

        // 디버그 로깅: 턴 완료
        if (debugLogger != null && debugLogger.isEnabled()) {
            debugLogger.completeTurn(chatRoomId, geminiResponse, actionQueue);
        }

        return resJson;
    }

    /**
     * Dummy
     * Gemini API에서는 불필요한 함수이다.
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
     * 캐시 힌트를 적용한다. Gemini 경로에서는 전략이 서버에 {@code cachedContents} 리소스를 생성한다.
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
    // Tool Use (v0.8.0 T2-b)
    // ========================================================================

    @Override
    public org.json.simple.JSONObject sendPromptWithTools(String userMsg, ToolRegistry tools) throws Exception {
        if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);

        boolean reqAq = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();
        String usrPrompt = reqAq ? this.aqHandler.getActionQueuePrompt(userMsg) : userMsg;
        String curViewPrompt = reqAq ? this.aqHandler.getCurViewPrompt() : null;
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
            JSONArray reqContents = round == 0 ? convHistory : this.conversationHistory.getHistory();
            ToolTurnResult turn = this.connApi.generateContentWithTools(reqContents, this.cacheStrategy, tools);

            if (turn.isFinal()) {
                finalText = turn.getFinalText();
                this.conversationHistory.addModelResponse(finalText);
                break;
            }
            // assistant functionCall content 를 히스토리에 보존
            JSONObject modelContent = new JSONObject(turn.getRawAssistantPayload());
            this.conversationHistory.addModelContent(modelContent);

            JSONArray fnResponses = new JSONArray();
            for (ToolCall call : turn.getToolCalls()) {
                ToolResult result = executeToolCall(call, tools);
                fnResponses.put(buildFunctionResponsePart(call, result));
                executedCalls.add(toExecutedCallJson(call, result));
            }
            this.conversationHistory.addToolResults(fnResponses);
        }

        if (finalText == null) {
            LOG.warn("Tool use max_tool_rounds={} 초과. 마지막 텍스트 응답 요청.", DEFAULT_MAX_TOOL_ROUNDS);
            finalText = this.connApi.generateContent(this.conversationHistory.getHistory(), this.cacheStrategy);
            this.conversationHistory.addModelResponse(finalText);
        }

        return buildFinalJson(finalText, executedCalls);
    }

    @Override
    public org.json.simple.JSONObject sendPromptExpectingToolCalls(String userMsg, ToolRegistry tools) throws Exception {
        if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);

        boolean reqAq = this.aqHandler != null && this.aqHandler.isCurrentViewInfo();
        String usrPrompt = reqAq ? this.aqHandler.getActionQueuePrompt(userMsg) : userMsg;
        String curViewPrompt = reqAq ? this.aqHandler.getCurViewPrompt() : null;
        JSONArray convHistory = this.conversationHistory.addUserPrompt(usrPrompt, curViewPrompt);
        if (this.aqHandler != null && curViewPrompt != null) {
            this.aqHandler.markViewInfoAsSent();
        }

        ToolTurnResult turn = this.connApi.generateContentWithTools(convHistory, this.cacheStrategy, tools);
        if (turn.isFinal()) {
            this.conversationHistory.addModelResponse(turn.getFinalText());
            return buildFinalJson(turn.getFinalText(), new ArrayList<>());
        }
        JSONObject modelContent = new JSONObject(turn.getRawAssistantPayload());
        this.conversationHistory.addModelContent(modelContent);
        return buildPendingJson(turn.getToolCalls());
    }

    @Override
    public org.json.simple.JSONObject continueWithToolResults(List<ToolResult> results, ToolRegistry tools) throws Exception {
        if (results != null && !results.isEmpty()) {
            // Gemini 는 functionResponse part 를 만들 때 이름이 필요. Registry 를 통해 추정 불가 시 callId 사용.
            // 호출자가 넘긴 결과에 대응하는 ToolCall 이름을 얻기 위해 registry 의 정의 이름을 fallback 으로 사용.
            JSONArray fnResponses = new JSONArray();
            for (ToolResult r : results) {
                // name 은 ToolResult 에 없으므로 registry 에서 가능한 첫 tool 이름 사용. 실용상 호출자가
                // Auto mode 를 쓰거나, Manual mode 에서 tool 당 결과를 한 번에 돌려준다는 전제.
                String name = firstRegisteredToolName(tools);
                fnResponses.put(buildFunctionResponsePartByName(name, r));
            }
            this.conversationHistory.addToolResults(fnResponses);
        }
        ToolTurnResult turn = this.connApi.generateContentWithTools(
                this.conversationHistory.getHistory(), this.cacheStrategy, tools);
        if (turn.isFinal()) {
            this.conversationHistory.addModelResponse(turn.getFinalText());
            return buildFinalJson(turn.getFinalText(), new ArrayList<>());
        }
        JSONObject modelContent = new JSONObject(turn.getRawAssistantPayload());
        this.conversationHistory.addModelContent(modelContent);
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

    private JSONObject buildFunctionResponsePart(ToolCall call, ToolResult result) {
        return buildFunctionResponsePartByName(call.getToolName(), result);
    }

    private JSONObject buildFunctionResponsePartByName(String toolName, ToolResult result) {
        JSONObject fnResp = new JSONObject();
        fnResp.put("name", toolName == null ? "unknown" : toolName);
        JSONObject response = new JSONObject();
        if (result.isError()) {
            response.put("error", result.getErrorMessage() == null ? "" : result.getErrorMessage());
        } else {
            response.put("result", result.getOutput() == null ? JSONObject.NULL
                    : new JSONObject(result.getOutput().toString()));
        }
        fnResp.put("response", response);
        JSONObject part = new JSONObject();
        part.put("functionResponse", fnResp);
        return part;
    }

    private String firstRegisteredToolName(ToolRegistry tools) {
        if (tools == null || tools.isEmpty()) return "unknown";
        return tools.all().iterator().next().getName();
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
}
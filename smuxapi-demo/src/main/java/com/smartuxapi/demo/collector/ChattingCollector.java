package com.smartuxapi.demo.collector;

import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolResult;

/**
 * {@link Chatting} Decorator — sendPrompt* 계열 호출 시 프롬프트/응답을
 * {@link PromptResponseCollector} 로 전달한다.
 *
 * <p>모든 Chatting 인터페이스 메서드를 delegate 에 위임하며, 실제 대화 턴
 * (sendPrompt / sendPromptWithSchema / sendPromptWithTools) 에서만 수집 로직을 삽입한다.
 *
 * @since smuxapi-demo 0.10.0
 */
public class ChattingCollector implements Chatting {

    private final Chatting delegate;
    private final PromptResponseCollector collector;

    public ChattingCollector(Chatting delegate, PromptResponseCollector collector) {
        this.delegate = delegate;
        this.collector = collector;
    }

    @Override
    public JSONObject sendPrompt(String userMsg) throws Exception {
        collector.startNewTurn();
        collector.captureUserPrompt(userMsg);
        JSONObject response = delegate.sendPrompt(userMsg);
        captureResponse(response);
        return response;
    }

    @Override
    public JSONObject sendPromptWithSchema(String userMsg, ResponseSchema schema) throws Exception {
        collector.startNewTurn();
        collector.captureUserPrompt(userMsg);
        JSONObject response = delegate.sendPromptWithSchema(userMsg, schema);
        captureResponse(response);
        return response;
    }

    @Override
    public JSONObject sendPromptWithTools(String userMsg, ToolRegistry tools) throws Exception {
        collector.startNewTurn();
        collector.captureUserPrompt(userMsg);
        JSONObject response = delegate.sendPromptWithTools(userMsg, tools);
        captureResponse(response);
        return response;
    }

    @Override
    public JSONObject sendPromptExpectingToolCalls(String userMsg, ToolRegistry tools) throws Exception {
        collector.startNewTurn();
        collector.captureUserPrompt(userMsg);
        JSONObject response = delegate.sendPromptExpectingToolCalls(userMsg, tools);
        captureResponse(response);
        return response;
    }

    @Override
    public JSONObject continueWithToolResults(List<ToolResult> results, ToolRegistry tools) throws Exception {
        // 수동 dispatch 연속 호출 — 별도 턴으로 취급
        collector.startNewTurn();
        JSONObject response = delegate.continueWithToolResults(results, tools);
        captureResponse(response);
        return response;
    }

    private void captureResponse(JSONObject response) {
        if (response == null) {
            collector.captureResponse(null, null);
            return;
        }
        Object msg = response.get("message");
        Object aq = response.get("action_queue");
        collector.captureResponse(msg == null ? null : msg.toString(), aq);
    }

    // ----- delegate pass-through -----

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        delegate.setActionQueueHandler(aqHandler);
    }

    @Override
    public Set<String> getMessageIdSet() {
        return delegate.getMessageIdSet();
    }

    @Override
    public void setCacheStrategy(CacheStrategy strategy) {
        delegate.setCacheStrategy(strategy);
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return delegate.getCacheStrategy();
    }

    @Override
    public void applyCacheHint(CacheHint hint) throws Exception {
        delegate.applyCacheHint(hint);
    }

    /** Delegate 접근자 — 테스트/고급 사용. */
    public Chatting getDelegate() {
        return delegate;
    }
}

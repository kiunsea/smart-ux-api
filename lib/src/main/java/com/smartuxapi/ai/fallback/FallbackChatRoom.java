package com.smartuxapi.ai.fallback;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.debug.DebugLogger;
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.tools.ToolRegistry;
import com.smartuxapi.ai.tools.ToolResult;

/**
 * {@link ChatRoom} 데코레이터 — {@link FallbackPolicy} 에 따라 여러 provider 를 순차 시도.
 *
 * <p>특정 provider 실패 시 {@link FailureReason} 을 분류하여 policy 에 등록된 reason 이면
 * 다음 slot 으로 넘어가고, 아니면 즉시 throw.
 *
 * <p>대화 히스토리는 각 slot 의 ChatRoom 이 독립적으로 관리 — fallback 이 일어난 턴만 두 번째
 * slot 에 기록되며, 이후 성공한 경로의 히스토리는 해당 slot 에만 남는다.
 *
 * <p>Tool Use / Structured Output 호출도 동일한 chain 로직. 단, tool use 는 호출 단위로만
 * 전환 (한 라운드 내 중간 전환 없음).
 *
 * @since 0.9.1
 */
public final class FallbackChatRoom implements ChatRoom {

    private static final Logger log = LogManager.getLogger(FallbackChatRoom.class);

    private final FallbackPolicy policy;
    private final String id = UUID.randomUUID().toString();
    private ActionQueueHandler aqHandler;

    public FallbackChatRoom(FallbackPolicy policy) {
        if (policy == null) throw new IllegalArgumentException("policy is required");
        this.policy = policy;
    }

    public FallbackPolicy getPolicy() { return policy; }

    @Override
    public String getId() { return id; }

    @Override
    public Chatting getChatting() {
        return new FallbackChatting(this);
    }

    @Override
    public boolean close() throws IOException, ParseException {
        boolean allOk = true;
        for (ProviderSlot slot : policy.getChain()) {
            try {
                slot.getChatRoom().close();
            } catch (Exception e) {
                log.warn("Slot [{}] close() 실패 — 무시: {}", slot.getName(), e.getMessage());
                allOk = false;
            }
        }
        return allOk;
    }

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
        for (ProviderSlot slot : policy.getChain()) {
            slot.getChatRoom().setActionQueueHandler(aqHandler);
        }
    }

    @Override
    public ActionQueueHandler getActionQueueHandler() {
        return aqHandler;
    }

    @Override
    public void setDebugLogger(DebugLogger debugLogger) {
        for (ProviderSlot slot : policy.getChain()) {
            slot.getChatRoom().setDebugLogger(debugLogger);
        }
    }

    @Override
    public DebugLogger getDebugLogger() {
        ProviderSlot first = policy.getChain().get(0);
        return first.getChatRoom().getDebugLogger();
    }

    // ----- 캐시 관련 — 모든 slot 에 broadcast -----

    @Override
    public void setCacheStrategy(CacheStrategy strategy) {
        for (ProviderSlot slot : policy.getChain()) {
            slot.getChatRoom().setCacheStrategy(strategy);
        }
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return policy.getChain().get(0).getChatRoom().getCacheStrategy();
    }

    @Override
    public void markAsCacheable(CacheHint hint) throws Exception {
        Exception last = null;
        for (ProviderSlot slot : policy.getChain()) {
            try {
                slot.getChatRoom().markAsCacheable(hint);
            } catch (Exception e) {
                log.warn("Slot [{}] markAsCacheable 실패 — 다른 slot 은 계속 진행: {}", slot.getName(), e.getMessage());
                last = e;
            }
        }
        if (last != null) throw last;
    }

    @Override
    public CacheMetrics getLastCacheMetrics() {
        return policy.getChain().get(0).getChatRoom().getLastCacheMetrics();
    }

    // ----- Tool Registry — 모든 slot 에 broadcast -----

    @Override
    public void setToolRegistry(ToolRegistry registry) {
        for (ProviderSlot slot : policy.getChain()) {
            slot.getChatRoom().setToolRegistry(registry);
        }
    }

    @Override
    public ToolRegistry getToolRegistry() {
        return policy.getChain().get(0).getChatRoom().getToolRegistry();
    }

    // ========================================================================
    // Fallback 코어 — chain 순회
    // ========================================================================

    /**
     * chain 을 순회하며 호출. 결과를 반환하거나 모든 slot 실패 시 예외.
     *
     * <p>예외 정책:
     * <ul>
     *   <li>reason 이 trigger 목록에 없거나 slot 이 canHandle=false → <b>원본 예외</b> throw
     *       (사용자가 fallback 을 기대하지 않는 실패를 감싸지 않음)</li>
     *   <li>trigger 되지만 마지막 slot 실패 → {@link FallbackExhaustedException} 으로 감싸서 throw</li>
     *   <li>trigger 되고 다음 slot 있음 → 다음 slot 으로 이동</li>
     * </ul>
     */
    <T> T runWithFallback(String opName, SlotCallable<T> op) throws Exception {
        List<ProviderSlot> chain = policy.getChain();
        Throwable last = null;
        for (int i = 0; i < chain.size(); i++) {
            ProviderSlot slot = chain.get(i);
            boolean isLast = (i == chain.size() - 1);
            try {
                return op.call(slot);
            } catch (Throwable t) {
                last = t;
                FailureReason reason = FailureReason.classify(t);
                boolean triggerable = policy.getTriggerReasons().contains(reason);
                boolean canHandle = slot.canHandle(t);
                if (!triggerable || !canHandle) {
                    if (policy.isLogOnFallback()) {
                        log.warn("Fallback 중단 — slot [{}] 실패 reason={} trigger={} canHandle={}: {}",
                                slot.getName(), reason, triggerable, canHandle, t.getMessage());
                    }
                    throwAs(t);
                }
                if (policy.isLogOnFallback()) {
                    if (!isLast) {
                        log.warn("Slot [{}] {} 실패 reason={} → 다음 slot 으로 fallback: {}",
                                slot.getName(), opName, reason, t.getMessage());
                    } else {
                        log.warn("Slot [{}] {} 실패 reason={} — chain 끝 도달, exhaust: {}",
                                slot.getName(), opName, reason, t.getMessage());
                    }
                }
            }
        }
        FallbackExhaustedException fe = new FallbackExhaustedException(
                "All " + chain.size() + " provider(s) failed for " + opName, last);
        throw fe;
    }

    private static void throwAs(Throwable t) throws Exception {
        if (t instanceof Exception) throw (Exception) t;
        if (t instanceof Error) throw (Error) t;
        throw new Exception(t);
    }

    /**
     * 내부 chain 호출 시그니처 — throwable 허용.
     */
    @FunctionalInterface
    interface SlotCallable<T> {
        T call(ProviderSlot slot) throws Exception;
    }

    // ========================================================================
    // FallbackChatting — 위 runWithFallback 을 사용하여 Chatting 메서드 구현
    // ========================================================================

    static final class FallbackChatting implements Chatting {

        private final FallbackChatRoom owner;

        FallbackChatting(FallbackChatRoom owner) {
            this.owner = owner;
        }

        @Override
        public void setActionQueueHandler(ActionQueueHandler aqHandler) {
            owner.setActionQueueHandler(aqHandler);
        }

        @Override
        public org.json.simple.JSONObject sendPrompt(String userMsg) throws Exception {
            return owner.runWithFallback("sendPrompt",
                    slot -> slot.getChatRoom().getChatting().sendPrompt(userMsg));
        }

        @Override
        public java.util.Set<String> getMessageIdSet() {
            // 첫 slot 기준
            return owner.policy.getChain().get(0).getChatRoom().getChatting().getMessageIdSet();
        }

        @Override
        public void setCacheStrategy(CacheStrategy strategy) {
            owner.setCacheStrategy(strategy);
        }

        @Override
        public CacheStrategy getCacheStrategy() {
            return owner.getCacheStrategy();
        }

        @Override
        public void applyCacheHint(CacheHint hint) throws Exception {
            owner.markAsCacheable(hint);
        }

        @Override
        public org.json.simple.JSONObject sendPromptWithSchema(String userMsg, ResponseSchema schema) throws Exception {
            if (schema == null) return sendPrompt(userMsg);
            return owner.runWithFallback("sendPromptWithSchema",
                    slot -> slot.getChatRoom().getChatting().sendPromptWithSchema(userMsg, schema));
        }

        @Override
        public org.json.simple.JSONObject sendPromptWithTools(String userMsg, ToolRegistry tools) throws Exception {
            if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);
            return owner.runWithFallback("sendPromptWithTools",
                    slot -> slot.getChatRoom().getChatting().sendPromptWithTools(userMsg, tools));
        }

        @Override
        public org.json.simple.JSONObject sendPromptExpectingToolCalls(String userMsg, ToolRegistry tools) throws Exception {
            if (tools == null || tools.isEmpty()) return sendPrompt(userMsg);
            return owner.runWithFallback("sendPromptExpectingToolCalls",
                    slot -> slot.getChatRoom().getChatting().sendPromptExpectingToolCalls(userMsg, tools));
        }

        @Override
        public org.json.simple.JSONObject continueWithToolResults(List<ToolResult> results, ToolRegistry tools) throws Exception {
            return owner.runWithFallback("continueWithToolResults",
                    slot -> slot.getChatRoom().getChatting().continueWithToolResults(results, tools));
        }
    }
}

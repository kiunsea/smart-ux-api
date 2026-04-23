package com.smartuxapi.demo.collector;

import java.io.IOException;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.debug.DebugLogger;
import com.smartuxapi.ai.tools.ToolRegistry;

/**
 * {@link ChatRoom} Decorator — inner ChatRoom 의 getChatting() 호출 시 {@link ChattingCollector}
 * 로 감싸 반환한다. 다른 메서드는 모두 delegate 에 위임.
 *
 * <p>ActionQueueHandler 는 {@link ActionQueueHandlerCollector} 로 대체되어 wrapping 된다 —
 * setter 에서 Collector 로 wrapping 후 delegate 에 전달.
 *
 * @since smuxapi-demo 0.10.0
 */
public class CollectorChatRoom implements ChatRoom {

    private final ChatRoom delegate;
    private final PromptResponseCollector collector;

    /** Cached wrapped Chatting — getChatting() 결과를 캐시하여 매 호출마다 새로 생성 방지. */
    private volatile ChattingCollector cachedChatting;
    /** Cached wrapped AQH — 동일 이유로 캐시. */
    private volatile ActionQueueHandlerCollector cachedAq;

    public CollectorChatRoom(ChatRoom delegate, PromptResponseCollector collector) {
        if (delegate == null) throw new IllegalArgumentException("delegate is required");
        if (collector == null) throw new IllegalArgumentException("collector is required");
        this.delegate = delegate;
        this.collector = collector;
    }

    public ChatRoom getDelegate() { return delegate; }

    @Override
    public String getId() { return delegate.getId(); }

    @Override
    public Chatting getChatting() {
        Chatting inner = delegate.getChatting();
        if (inner == null) return null;
        ChattingCollector c = this.cachedChatting;
        if (c == null || c.getDelegate() != inner) {
            c = new ChattingCollector(inner, collector);
            this.cachedChatting = c;
        }
        return c;
    }

    @Override
    public boolean close() throws IOException, ParseException {
        return delegate.close();
    }

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        // 이미 collector 서브클래스이면 그대로, 아니면 기본 instance 를 replace — 단, 이 경로는
        // 주로 ChatRoomService 에서 처음 set 할 때 사용되고, 그 때 이미 AQH collector 를 만들어서 넘긴다.
        if (aqHandler instanceof ActionQueueHandlerCollector) {
            this.cachedAq = (ActionQueueHandlerCollector) aqHandler;
            delegate.setActionQueueHandler(aqHandler);
        } else {
            // 일반 AQH → 새 collector instance 로 대체 (기존 상태는 빈 상태 — caller 가 addCurrentViewInfo 등으로 재설정)
            ActionQueueHandlerCollector wrapped = new ActionQueueHandlerCollector(collector);
            this.cachedAq = wrapped;
            delegate.setActionQueueHandler(wrapped);
        }
    }

    @Override
    public ActionQueueHandler getActionQueueHandler() {
        return cachedAq != null ? cachedAq : delegate.getActionQueueHandler();
    }

    // ----- pass-through -----

    @Override
    public void setDebugLogger(DebugLogger debugLogger) {
        delegate.setDebugLogger(debugLogger);
    }

    @Override
    public DebugLogger getDebugLogger() {
        return delegate.getDebugLogger();
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
    public void markAsCacheable(CacheHint hint) throws Exception {
        delegate.markAsCacheable(hint);
    }

    @Override
    public CacheMetrics getLastCacheMetrics() {
        return delegate.getLastCacheMetrics();
    }

    @Override
    public void setToolRegistry(ToolRegistry registry) {
        delegate.setToolRegistry(registry);
    }

    @Override
    public ToolRegistry getToolRegistry() {
        return delegate.getToolRegistry();
    }
}

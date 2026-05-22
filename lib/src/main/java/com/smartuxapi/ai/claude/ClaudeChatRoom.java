package com.smartuxapi.ai.claude;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.cache.CacheHint;
import com.smartuxapi.ai.cache.CacheMetrics;
import com.smartuxapi.ai.cache.CacheStrategy;
import com.smartuxapi.ai.cache.NoOpCacheStrategy;
import com.smartuxapi.ai.cache.claude.ClaudePromptCacheStrategy;
import com.smartuxapi.ai.debug.DebugConfig;
import com.smartuxapi.ai.debug.DebugLogger;

/**
 * Anthropic Claude Messages API를 사용하는 {@link ChatRoom} 구현체.
 *
 * @since 0.10.0
 */
public class ClaudeChatRoom implements ChatRoom {

    private static final String AI_PROVIDER = "Claude";

    private final String threadId = UUID.randomUUID().toString();
    private final String modelName;

    private ClaudeAPIConnection connApi = null;
    private ClaudeChatting message = null;
    private ActionQueueHandler aqHandler = null;
    private DebugLogger debugLogger = null;
    private CacheStrategy cacheStrategy = new ClaudePromptCacheStrategy();

    /**
     * @param apiKey Anthropic API 키
     * @param modelName 모델명
     */
    public ClaudeChatRoom(String apiKey, String modelName) {
        this(apiKey, modelName, ClaudeAPIConnection.DEFAULT_ANTHROPIC_VERSION);
    }

    /**
     * @param apiKey Anthropic API 키
     * @param modelName 모델명
     * @param anthropicVersion {@code anthropic-version} 헤더 값 (null/빈 문자열이면 기본값)
     */
    public ClaudeChatRoom(String apiKey, String modelName, String anthropicVersion) {
        this.connApi = new ClaudeAPIConnection(apiKey, modelName, anthropicVersion);
        this.modelName = modelName;

        // 디버그 모드가 활성화되어 있으면 자동으로 DebugLogger 생성
        if (DebugConfig.getInstance().isDebugMode()) {
            this.debugLogger = new DebugLogger();
            this.debugLogger.startSession(threadId, AI_PROVIDER, modelName);
        }
    }

    @Override
    public String getId() {
        return threadId;
    }

    @Override
    public Chatting getChatting() {
        if (this.message == null) {
            this.message = new ClaudeChatting(this.connApi);
        }
        this.message.setActionQueueHandler(this.aqHandler);
        this.message.setDebugLogger(this.debugLogger, this.threadId);
        this.message.setCacheStrategy(this.cacheStrategy);
        return this.message;
    }

    @Override
    public boolean close() throws IOException, ParseException {
        if (this.debugLogger != null) {
            this.debugLogger.endSession(threadId);
        }
        // 캐시 전략 해제 (Claude 는 local no-op)
        try {
            this.cacheStrategy.invalidate();
        } catch (Exception e) {
            // 캐시 해제 실패는 close 를 막지 않는다
        }
        this.connApi = null;
        this.message = null;
        return true;
    }

    @Override
    public void setActionQueueHandler(ActionQueueHandler aqHandler) {
        this.aqHandler = aqHandler;
    }

    @Override
    public ActionQueueHandler getActionQueueHandler() {
        return this.aqHandler;
    }

    @Override
    public void setDebugLogger(DebugLogger debugLogger) {
        this.debugLogger = debugLogger;
        if (this.debugLogger != null && this.debugLogger.isEnabled()) {
            this.debugLogger.startSession(threadId, AI_PROVIDER, modelName);
        }
    }

    @Override
    public DebugLogger getDebugLogger() {
        return this.debugLogger;
    }

    // ----- 캐시 전략 -----

    @Override
    public void setCacheStrategy(CacheStrategy strategy) {
        this.cacheStrategy = (strategy == null) ? NoOpCacheStrategy.INSTANCE : strategy;
        if (this.message != null) {
            this.message.setCacheStrategy(this.cacheStrategy);
        }
    }

    @Override
    public CacheStrategy getCacheStrategy() {
        return this.cacheStrategy;
    }

    @Override
    public void markAsCacheable(CacheHint hint) throws Exception {
        ClaudeChatting ch = (ClaudeChatting) getChatting();
        ch.applyCacheHint(hint);
    }

    @Override
    public CacheMetrics getLastCacheMetrics() {
        return this.cacheStrategy.getLastMetrics();
    }
}

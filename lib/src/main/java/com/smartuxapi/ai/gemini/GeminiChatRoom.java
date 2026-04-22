package com.smartuxapi.ai.gemini;

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
import com.smartuxapi.ai.cache.gemini.GeminiContextCacheStrategy;
import com.smartuxapi.ai.debug.DebugConfig;
import com.smartuxapi.ai.debug.DebugLogger;

public class GeminiChatRoom implements ChatRoom {

    private static final String AI_PROVIDER = "Gemini";

    private final ConversationHistory conversationHistory = new ConversationHistory();
    private final String threadId = UUID.randomUUID().toString();
    private final String modelName;
    private final String apiKey;

    private GeminiAPIConnection connApi = null;
    private GeminiChatting message = null;
    private ActionQueueHandler aqHandler = null;
    private DebugLogger debugLogger = null;
    private CacheStrategy cacheStrategy = null;

    /**
     * @param apiKey
     * @param modelName
     */
    public GeminiChatRoom(String apiKey, String modelName) {
        this.connApi = new GeminiAPIConnection(apiKey, modelName);
        this.modelName = modelName;
        this.apiKey = apiKey;
        this.cacheStrategy = new GeminiContextCacheStrategy(apiKey, modelName);

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
            this.message = new GeminiChatting(this.connApi);
        }
        this.message.setActionQueueHandler(this.aqHandler);
        this.message.setDebugLogger(this.debugLogger, this.threadId);
        this.message.setCacheStrategy(this.cacheStrategy);
        return this.message;
    }

    @Override
    public boolean close() throws IOException, ParseException {
        // 디버그 세션 종료
        if (this.debugLogger != null) {
            this.debugLogger.endSession(threadId);
        }

        // Gemini 캐시 리소스 해제 (TTL 로 자연 만료되지만 명시적 삭제로 비용 최소화)
        if (this.cacheStrategy != null) {
            try {
                this.cacheStrategy.invalidate();
            } catch (Exception e) {
                // 캐시 해제 실패는 close 를 막지 않는다
            }
        }

        this.connApi = null;
        this.message = null;
        this.conversationHistory.clearHistory();
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
        GeminiChatting ch = (GeminiChatting) getChatting();
        ch.applyCacheHint(hint);
    }

    @Override
    public CacheMetrics getLastCacheMetrics() {
        return this.cacheStrategy.getLastMetrics();
    }
}

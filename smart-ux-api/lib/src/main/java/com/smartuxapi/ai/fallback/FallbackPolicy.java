package com.smartuxapi.ai.fallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Fallback 정책 — chain 순서 + trigger reason + timeout + 로깅 옵션.
 *
 * <p>소비자는 {@link FallbackPolicies} 프리셋 또는 본 클래스 {@link Builder} 사용.
 *
 * @since 0.9.1
 */
public final class FallbackPolicy {

    private final List<ProviderSlot> chain;
    private final Set<FailureReason> triggerReasons;
    private final int primaryTimeoutMs;
    private final boolean logOnFallback;

    private FallbackPolicy(List<ProviderSlot> chain,
                           Set<FailureReason> triggerReasons,
                           int primaryTimeoutMs,
                           boolean logOnFallback) {
        if (chain == null || chain.isEmpty()) {
            throw new IllegalArgumentException("chain must not be empty");
        }
        this.chain = Collections.unmodifiableList(new ArrayList<>(chain));
        this.triggerReasons = Collections.unmodifiableSet(EnumSet.copyOf(
                triggerReasons == null ? defaultReasons() : triggerReasons));
        this.primaryTimeoutMs = Math.max(0, primaryTimeoutMs);
        this.logOnFallback = logOnFallback;
    }

    public List<ProviderSlot> getChain() { return chain; }
    public Set<FailureReason> getTriggerReasons() { return triggerReasons; }
    public int getPrimaryTimeoutMs() { return primaryTimeoutMs; }
    public boolean isLogOnFallback() { return logOnFallback; }

    /**
     * 기본 fallback trigger: timeout / rate limit / 5xx / 네트워크 오류. UNAUTHORIZED 미포함.
     */
    public static Set<FailureReason> defaultReasons() {
        return EnumSet.of(
                FailureReason.TIMEOUT,
                FailureReason.RATE_LIMIT,
                FailureReason.SERVER_ERROR,
                FailureReason.NETWORK_ERROR);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<ProviderSlot> chain = new ArrayList<>();
        private Set<FailureReason> triggerReasons;
        private int primaryTimeoutMs = 0;
        private boolean logOnFallback = true;

        public Builder addSlot(ProviderSlot slot) {
            if (slot == null) throw new IllegalArgumentException("slot must not be null");
            chain.add(slot);
            return this;
        }

        public Builder addSlots(ProviderSlot... slots) {
            if (slots != null) chain.addAll(Arrays.asList(slots));
            return this;
        }

        public Builder triggerReasons(FailureReason... reasons) {
            if (reasons == null || reasons.length == 0) {
                this.triggerReasons = EnumSet.noneOf(FailureReason.class);
            } else {
                this.triggerReasons = EnumSet.copyOf(Arrays.asList(reasons));
            }
            return this;
        }

        public Builder timeoutMs(int ms) {
            this.primaryTimeoutMs = ms;
            return this;
        }

        public Builder logOnFallback(boolean log) {
            this.logOnFallback = log;
            return this;
        }

        public FallbackPolicy build() {
            return new FallbackPolicy(chain, triggerReasons, primaryTimeoutMs, logOnFallback);
        }
    }
}

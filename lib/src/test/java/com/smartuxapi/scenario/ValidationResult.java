package com.smartuxapi.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action Queue 비교 결과 — 차이 목록 + 매칭 여부.
 *
 * @since lib 0.9.5
 */
public final class ValidationResult {

    private final List<ActionDiff> diffs;

    private ValidationResult(List<ActionDiff> diffs) {
        this.diffs = diffs == null ? Collections.emptyList()
                : Collections.unmodifiableList(diffs);
    }

    public static ValidationResult exactMatch() {
        return new ValidationResult(Collections.emptyList());
    }

    public static ValidationResult of(List<ActionDiff> diffs) {
        return new ValidationResult(new ArrayList<>(diffs));
    }

    public List<ActionDiff> getDiffs() { return diffs; }

    public boolean isExactMatch() {
        return diffs.isEmpty();
    }

    public int diffCount() {
        return diffs.size();
    }

    public int countByKind(ActionDiff.Kind kind) {
        return (int) diffs.stream().filter(d -> d.getKind() == kind).count();
    }

    @Override
    public String toString() {
        if (isExactMatch()) return "ValidationResult{exactMatch}";
        return "ValidationResult{diffs=" + diffs.size() + "}";
    }
}

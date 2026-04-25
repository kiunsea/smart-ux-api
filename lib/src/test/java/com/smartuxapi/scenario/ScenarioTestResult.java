package com.smartuxapi.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 시나리오(파일) 단위 테스트 결과 — 다수의 {@link TurnTestResult} 집계.
 *
 * @since lib 0.9.5
 */
public final class ScenarioTestResult {

    private final String sourceFileName;
    private final String sessionId;
    private final String aiModel;
    private final List<TurnTestResult> turnResults;

    public ScenarioTestResult(String sourceFileName, String sessionId, String aiModel,
                              List<TurnTestResult> turnResults) {
        this.sourceFileName = sourceFileName;
        this.sessionId = sessionId;
        this.aiModel = aiModel;
        this.turnResults = turnResults == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(turnResults));
    }

    public String getSourceFileName() { return sourceFileName; }
    public String getSessionId() { return sessionId; }
    public String getAiModel() { return aiModel; }
    public List<TurnTestResult> getTurnResults() { return turnResults; }

    public int total()   { return turnResults.size(); }
    public int passed()  { return countByStatus(TurnTestResult.Status.PASS); }
    public int failed()  { return countByStatus(TurnTestResult.Status.FAIL); }
    public int errored() { return countByStatus(TurnTestResult.Status.ERROR_RUNTIME); }
    public int skipped() { return countByStatus(TurnTestResult.Status.SKIPPED); }

    private int countByStatus(TurnTestResult.Status s) {
        return (int) turnResults.stream().filter(t -> t.getStatus() == s).count();
    }

    public boolean isAllPassed() {
        return failed() == 0 && errored() == 0 && passed() > 0;
    }

    public long totalElapsedMs() {
        return turnResults.stream().mapToLong(TurnTestResult::getElapsedMs).sum();
    }

    @Override
    public String toString() {
        return String.format(
                "ScenarioTestResult{file=%s, total=%d, pass=%d, fail=%d, error=%d, skip=%d, %dms}",
                sourceFileName, total(), passed(), failed(), errored(), skipped(), totalElapsedMs());
    }
}

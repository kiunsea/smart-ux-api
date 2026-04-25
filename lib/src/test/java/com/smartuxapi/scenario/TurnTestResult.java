package com.smartuxapi.scenario;

/**
 * 단일 턴의 테스트 결과 — expected vs actual 비교 결과 + 상태.
 *
 * @since lib 0.9.5
 */
public final class TurnTestResult {

    public enum Status {
        PASS,                 // ValidationResult.isExactMatch
        FAIL,                 // diff 존재
        ERROR_RUNTIME,        // 재현 도중 예외 (네트워크/API)
        SKIPPED               // 의도적 skip (예: API 키 부재)
    }

    private final int turnNo;
    private final Status status;
    private final ValidationResult validationResult;  // PASS/FAIL 일 때만 non-null
    private final String errorMessage;                // ERROR_RUNTIME / SKIPPED 일 때만 non-null
    private final String expectedActionQueue;         // 디버그용 직렬화 string
    private final String actualActionQueue;
    private final long elapsedMs;

    private TurnTestResult(int turnNo, Status status, ValidationResult vr,
                           String errMsg, String exp, String act, long elapsedMs) {
        this.turnNo = turnNo;
        this.status = status;
        this.validationResult = vr;
        this.errorMessage = errMsg;
        this.expectedActionQueue = exp;
        this.actualActionQueue = act;
        this.elapsedMs = elapsedMs;
    }

    public static TurnTestResult pass(int turnNo, String exp, String act, long elapsedMs) {
        return new TurnTestResult(turnNo, Status.PASS, ValidationResult.exactMatch(),
                null, exp, act, elapsedMs);
    }

    public static TurnTestResult fail(int turnNo, ValidationResult vr,
                                      String exp, String act, long elapsedMs) {
        return new TurnTestResult(turnNo, Status.FAIL, vr, null, exp, act, elapsedMs);
    }

    public static TurnTestResult error(int turnNo, String errMsg, long elapsedMs) {
        return new TurnTestResult(turnNo, Status.ERROR_RUNTIME, null, errMsg, null, null, elapsedMs);
    }

    public static TurnTestResult skipped(int turnNo, String reason) {
        return new TurnTestResult(turnNo, Status.SKIPPED, null, reason, null, null, 0L);
    }

    public int getTurnNo() { return turnNo; }
    public Status getStatus() { return status; }
    public ValidationResult getValidationResult() { return validationResult; }
    public String getErrorMessage() { return errorMessage; }
    public String getExpectedActionQueue() { return expectedActionQueue; }
    public String getActualActionQueue() { return actualActionQueue; }
    public long getElapsedMs() { return elapsedMs; }

    public boolean isPass() { return status == Status.PASS; }

    @Override
    public String toString() {
        return String.format("Turn#%d %s%s",
                turnNo, status,
                validationResult == null ? "" : " " + validationResult);
    }
}

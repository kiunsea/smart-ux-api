package com.smartuxapi.scenario;

/**
 * 단일 차이 항목 — JSON pointer-style path + 두 값.
 *
 * <p>예:
 * <ul>
 *   <li>{@code /actions/0/type} : {@code "click"} ↔ {@code "tap"}</li>
 *   <li>{@code /actions/2} : {@code [missing]} ↔ {@code {...새 액션...}}</li>
 * </ul>
 *
 * @since lib 0.9.5
 */
public final class ActionDiff {

    public enum Kind {
        VALUE_DIFFERS,    // 동일 path 의 값이 다름
        EXPECTED_MISSING, // expected 에 있는 path 가 actual 에 없음
        ACTUAL_EXTRA      // actual 에만 있는 path
    }

    private final String path;
    private final Kind kind;
    private final String expected;
    private final String actual;

    public ActionDiff(String path, Kind kind, String expected, String actual) {
        this.path = path;
        this.kind = kind;
        this.expected = expected;
        this.actual = actual;
    }

    public String getPath() { return path; }
    public Kind getKind() { return kind; }
    public String getExpected() { return expected; }
    public String getActual() { return actual; }

    @Override
    public String toString() {
        return String.format("[%s] %s : expected=%s, actual=%s",
                kind, path, expected, actual);
    }
}

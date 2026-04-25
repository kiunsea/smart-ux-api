package com.smartuxapi.scenario;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 두 {@link JsonNode} (expected vs actual) 를 deep 비교하여 {@link ValidationResult} 생성.
 *
 * <p>JSON Pointer style path 로 차이 위치 표기:
 * <ul>
 *   <li>Object: {@code /key/subKey}</li>
 *   <li>Array : {@code /array/0/...}</li>
 * </ul>
 *
 * <p>비교 규칙:
 * <ul>
 *   <li>type mismatch (object vs array, string vs number 등) → VALUE_DIFFERS</li>
 *   <li>scalar 값이 같지 않음 → VALUE_DIFFERS</li>
 *   <li>expected 의 key 가 actual 에 없음 → EXPECTED_MISSING</li>
 *   <li>actual 의 key 가 expected 에 없음 → ACTUAL_EXTRA</li>
 *   <li>둘 다 null 이거나 missing → 매치</li>
 * </ul>
 *
 * @since lib 0.9.5
 */
public final class ActionQueueValidator {

    private ActionQueueValidator() {}

    public static ValidationResult validate(JsonNode expected, JsonNode actual) {
        List<ActionDiff> diffs = new ArrayList<>();
        compare("", expected, actual, diffs);
        return diffs.isEmpty() ? ValidationResult.exactMatch() : ValidationResult.of(diffs);
    }

    private static void compare(String path, JsonNode exp, JsonNode act, List<ActionDiff> out) {
        // null 또는 missing 처리
        boolean expIsNull = (exp == null) || exp.isNull() || exp.isMissingNode();
        boolean actIsNull = (act == null) || act.isNull() || act.isMissingNode();
        if (expIsNull && actIsNull) return;
        if (expIsNull && !actIsNull) {
            out.add(new ActionDiff(orRoot(path), ActionDiff.Kind.ACTUAL_EXTRA, "null", act.toString()));
            return;
        }
        if (!expIsNull && actIsNull) {
            out.add(new ActionDiff(orRoot(path), ActionDiff.Kind.EXPECTED_MISSING, exp.toString(), "null"));
            return;
        }

        // 타입 불일치
        if (exp.getNodeType() != act.getNodeType()) {
            out.add(new ActionDiff(orRoot(path), ActionDiff.Kind.VALUE_DIFFERS,
                    nodeTypeStr(exp), nodeTypeStr(act)));
            return;
        }

        if (exp.isObject()) {
            TreeSet<String> allKeys = new TreeSet<>();
            exp.fieldNames().forEachRemaining(allKeys::add);
            act.fieldNames().forEachRemaining(allKeys::add);
            for (String key : allKeys) {
                String childPath = path + "/" + key;
                JsonNode e = exp.get(key);
                JsonNode a = act.get(key);
                if (e == null) {
                    out.add(new ActionDiff(childPath, ActionDiff.Kind.ACTUAL_EXTRA, "[absent]", a.toString()));
                } else if (a == null) {
                    out.add(new ActionDiff(childPath, ActionDiff.Kind.EXPECTED_MISSING, e.toString(), "[absent]"));
                } else {
                    compare(childPath, e, a, out);
                }
            }
            return;
        }

        if (exp.isArray()) {
            int es = exp.size();
            int as = act.size();
            int min = Math.min(es, as);
            for (int i = 0; i < min; i++) {
                compare(path + "/" + i, exp.get(i), act.get(i), out);
            }
            for (int i = min; i < es; i++) {
                out.add(new ActionDiff(path + "/" + i, ActionDiff.Kind.EXPECTED_MISSING,
                        exp.get(i).toString(), "[absent]"));
            }
            for (int i = min; i < as; i++) {
                out.add(new ActionDiff(path + "/" + i, ActionDiff.Kind.ACTUAL_EXTRA,
                        "[absent]", act.get(i).toString()));
            }
            return;
        }

        // scalar
        if (!exp.equals(act)) {
            out.add(new ActionDiff(orRoot(path), ActionDiff.Kind.VALUE_DIFFERS,
                    exp.toString(), act.toString()));
        }
    }

    private static String orRoot(String path) {
        return path.isEmpty() ? "/" : path;
    }

    private static String nodeTypeStr(JsonNode n) {
        if (n == null) return "null";
        return n.getNodeType().name() + "(" + n.toString() + ")";
    }
}

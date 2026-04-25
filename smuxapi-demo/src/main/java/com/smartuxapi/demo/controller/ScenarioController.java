package com.smartuxapi.demo.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartuxapi.demo.collector.PromptResponseCollector;
import com.smartuxapi.demo.collector.ScenarioTurn;

/**
 * Scenario Collector 운영용 컨트롤러 — Phase 2 검증 시 저장 트리거 / 상태 조회 용.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /demo/scenario/status} — 활성화 여부, 누적 턴 수</li>
 *   <li>{@code POST /demo/scenario/save}   — 현재까지 누적된 턴을 JSON 파일로 저장</li>
 *   <li>{@code POST /demo/scenario/reset[?save=true|false]} — 누적 데이터 초기화 (옵션으로 저장 후 reset)</li>
 * </ul>
 *
 * @since smuxapi-demo 0.10.1
 */
@RestController
@RequestMapping("/demo/scenario")
public class ScenarioController {

    private static final Logger log = LogManager.getLogger(ScenarioController.class);

    @Autowired(required = false)
    private PromptResponseCollector collector;

    @GetMapping("/status")
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONObject> status() {
        JSONObject res = new JSONObject();
        if (collector == null) {
            res.put("present", false);
            res.put("message", "PromptResponseCollector bean is not available");
            return ResponseEntity.ok(res);
        }
        res.put("present", true);
        res.put("enabled", collector.isEnabled());
        res.put("sessionId", collector.getSessionId());
        res.put("aiModel", collector.getAiModel());
        res.put("turnCount", collector.turnCount());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/save")
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONObject> save() {
        JSONObject res = new JSONObject();
        if (collector == null || !collector.isEnabled()) {
            res.put("status", "skipped");
            res.put("message", "collector unavailable or not enabled");
            return ResponseEntity.status(HttpStatus.OK).body(res);
        }
        try {
            Path file = collector.saveToFile();
            if (file == null) {
                res.put("status", "empty");
                res.put("message", "no turns to save yet");
                res.put("turnCount", 0);
            } else {
                res.put("status", "saved");
                res.put("filePath", file.toAbsolutePath().toString());
                res.put("turnCount", collector.turnCount());
            }
            return ResponseEntity.ok(res);
        } catch (IOException e) {
            log.error("scenario save 실패", e);
            res.put("status", "error");
            res.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    @PostMapping("/reset")
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONObject> reset(@RequestParam(name = "save", defaultValue = "false") boolean save) {
        JSONObject res = new JSONObject();
        if (collector == null) {
            res.put("status", "skipped");
            res.put("message", "collector unavailable");
            return ResponseEntity.ok(res);
        }
        if (save && collector.isEnabled()) {
            try {
                Path file = collector.saveToFile();
                if (file != null) res.put("savedTo", file.toAbsolutePath().toString());
            } catch (IOException e) {
                log.warn("save before reset 실패 — reset 은 계속 진행", e);
                res.put("savedError", e.getMessage());
            }
        }
        collector.reset();
        res.put("status", "reset");
        return ResponseEntity.ok(res);
    }

    /**
     * 디버그용 — 현재 메모리에 있는 turn 목록 미리보기 (필드 길이만, 실제 텍스트는 100자 truncate).
     */
    @GetMapping("/preview")
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONObject> preview() {
        JSONObject res = new JSONObject();
        if (collector == null || !collector.isEnabled()) {
            res.put("present", false);
            return ResponseEntity.ok(res);
        }
        res.put("present", true);
        res.put("enabled", true);
        res.put("turnCount", collector.turnCount());
        List<ScenarioTurn> turns = collector.snapshotTurns();
        JSONArray arr = new JSONArray();
        arr.addAll(turns.stream().map(ScenarioController::summarize).collect(Collectors.toList()));
        res.put("turns", arr);
        return ResponseEntity.ok(res);
    }

    @SuppressWarnings("unchecked")
    private static JSONObject summarize(ScenarioTurn t) {
        JSONObject o = new JSONObject();
        o.put("turnNo", t.getTurnNo());
        o.put("uiInfoLen", t.getUiInfo() == null ? 0 : t.getUiInfo().length());
        o.put("userPromptPreview", trim(t.getUserPrompt(), 100));
        o.put("apiPromptLen", t.getApiPrompt() == null ? 0 : t.getApiPrompt().length());
        o.put("resMsgPreview", trim(t.getResMsg(), 100));
        o.put("hasActionQueue", t.getActionQueue() != null);
        return o;
    }

    private static String trim(String s, int n) {
        if (s == null) return null;
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}

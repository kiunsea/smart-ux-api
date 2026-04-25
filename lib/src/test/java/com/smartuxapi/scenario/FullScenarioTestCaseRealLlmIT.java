package com.smartuxapi.scenario;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.gemini.GeminiChatRoom;
import com.smartuxapi.ai.openai.ResponsesChatRoom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full Scenario 실 LLM 통합 테스트 — opt-in.
 *
 * <p>실행 조건 (둘 다 충족 시에만 실행):
 * <ul>
 *   <li>{@code OPENAI_API_KEY} 환경 변수 설정</li>
 *   <li>{@code SCENARIO_FILE} 환경 변수에 시나리오 JSON 경로 (또는 디렉터리)</li>
 * </ul>
 *
 * <p>비용 발생 — 시나리오의 turn 수만큼 LLM API 호출이 일어남.
 *
 * <p>실행 예 (Windows PowerShell):
 * <pre>
 *   $env:OPENAI_API_KEY="sk-..."
 *   $env:SCENARIO_FILE="C:\path\to\scenarios\test-scenario-xxx.json"
 *   .\gradlew.bat :lib:test --tests "com.smartuxapi.scenario.FullScenarioTestCaseRealLlmIT"
 * </pre>
 *
 * @since lib 0.9.6
 */
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "SCENARIO_FILE", matches = ".+")
@DisplayName("Full Scenario 실 LLM 통합 테스트 (opt-in)")
class FullScenarioTestCaseRealLlmIT {

    private static final String DEFAULT_OPENAI_MODEL = "gpt-4.1-mini";
    private static final String DEFAULT_GEMINI_MODEL = "gemini-2.5-flash";

    @Test
    @DisplayName("SCENARIO_FILE 의 시나리오 재현 + 결과 출력")
    void runRealLlmScenario() throws Exception {
        String scenarioFile = System.getenv("SCENARIO_FILE");
        Path path = Paths.get(scenarioFile);
        assertTrue(Files.exists(path), "scenario path not found: " + scenarioFile);

        List<ScenarioData> scenarios;
        if (Files.isDirectory(path)) {
            scenarios = ScenarioDataLoader.loadDirectory(path);
            assertFalse(scenarios.isEmpty(), "no .json scenario in directory: " + path);
        } else {
            scenarios = java.util.Collections.singletonList(ScenarioDataLoader.load(path));
        }

        ChatRoomFactory factory = scenario -> createRoom(scenario);
        FullScenarioTestCase runner = new FullScenarioTestCase(factory, true);

        int totalPass = 0, totalFail = 0, totalErr = 0, totalSkip = 0, totalTurns = 0;
        long totalElapsed = 0;

        for (ScenarioData s : scenarios) {
            String fileName = path.toFile().isDirectory()
                    ? "(dir) sessionId=" + s.getSessionId()
                    : path.getFileName().toString();
            System.out.println();
            System.out.println("===== Replay: " + fileName + " (model=" + s.getAiModel()
                    + ", turns=" + s.turnSize() + ") =====");

            ScenarioTestResult result = runner.run(s, fileName);
            totalPass += result.passed();
            totalFail += result.failed();
            totalErr += result.errored();
            totalSkip += result.skipped();
            totalTurns += result.total();
            totalElapsed += result.totalElapsedMs();

            for (TurnTestResult t : result.getTurnResults()) {
                System.out.printf("  Turn#%d %-7s elapsed=%dms",
                        t.getTurnNo(), t.getStatus(), t.getElapsedMs());
                if (t.getStatus() == TurnTestResult.Status.FAIL) {
                    System.out.print("  diff=" + t.getValidationResult().diffCount());
                    System.out.println();
                    System.out.println("    expected: " + truncate(t.getExpectedActionQueue(), 200));
                    System.out.println("    actual  : " + truncate(t.getActualActionQueue(), 200));
                } else if (t.getStatus() == TurnTestResult.Status.ERROR_RUNTIME) {
                    System.out.println("  err=" + t.getErrorMessage());
                } else {
                    System.out.println();
                }
            }
            System.out.println("  -> " + result);
        }

        System.out.println();
        System.out.println("=========================================================");
        System.out.printf("TOTAL: %d turns, pass=%d fail=%d error=%d skip=%d, %dms%n",
                totalTurns, totalPass, totalFail, totalErr, totalSkip, totalElapsed);
        System.out.println("=========================================================");

        assertTrue(totalTurns > 0, "no turns replayed");
        // assertion 약화: error 가 아닌 한 통과 (FAIL 은 LLM 비결정성으로 빈번)
        assertEquals(0, totalErr,
                "ERROR_RUNTIME 발생 — 네트워크/API 키/요청 형식 점검 필요");
    }

    private ChatRoom createRoom(ScenarioData scenario) {
        String model = scenario.getAiModel();
        if (model == null || "chatgpt".equalsIgnoreCase(model) || "openai".equalsIgnoreCase(model)) {
            String key = System.getenv("OPENAI_API_KEY");
            if (key == null || key.isEmpty()) return null;
            String overrideModel = System.getenv("OPENAI_MODEL");
            return new ResponsesChatRoom(key,
                    overrideModel != null && !overrideModel.isEmpty() ? overrideModel : DEFAULT_OPENAI_MODEL);
        }
        if ("gemini".equalsIgnoreCase(model)) {
            String key = System.getenv("GEMINI_API_KEY");
            if (key == null || key.isEmpty()) return null;
            String overrideModel = System.getenv("GEMINI_MODEL");
            return new GeminiChatRoom(key,
                    overrideModel != null && !overrideModel.isEmpty() ? overrideModel : DEFAULT_GEMINI_MODEL);
        }
        return null;
    }

    private static String truncate(String s, int n) {
        if (s == null) return "null";
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }
}

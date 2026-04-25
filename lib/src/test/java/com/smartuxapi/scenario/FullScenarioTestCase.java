package com.smartuxapi.scenario;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;

/**
 * 시나리오 JSON 을 재현(replay) 하여 actual action_queue 를 생성하고
 * expected 와 비교하는 테스트 러너 (harness).
 *
 * <p>JUnit {@code @Test} 가 아닌 일반 클래스 — 실제 LLM 호출이 필요하므로 사용자가 의도적으로
 * 실행한다 (main 함수 또는 자체 테스트 스크립트에서). 호출 비용이 발생.
 *
 * <p>사용 예:
 * <pre>{@code
 *   FullScenarioTestCase runner = new FullScenarioTestCase(scenario -> {
 *       String key = System.getenv("OPENAI_API_KEY");
 *       return new ResponsesChatRoom(key, "gpt-4.1-mini");
 *   });
 *   ScenarioData data = ScenarioDataLoader.load(file);
 *   ScenarioTestResult result = runner.run(data);
 *   System.out.println(result);
 * }</pre>
 *
 * <p>현재 범위: 각 턴에서 {@code userPrompt} 를 {@code Chatting.sendPrompt} 로 보내고,
 * 응답의 {@code action_queue} 를 expected 와 비교.
 *
 * <p>다루지 않는 기능 (후속):
 * <ul>
 *   <li>{@code uiInfo} 를 ActionQueueHandler 에 주입하여 화면 컨텍스트 재현</li>
 *   <li>{@code resMsg} 텍스트 비교 (현재는 action_queue 만)</li>
 *   <li>로깅/리포팅 — {@link ScenarioTestResult} 만 반환, 파일 출력은 별도</li>
 * </ul>
 *
 * @since lib 0.9.5
 */
public class FullScenarioTestCase {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ChatRoomFactory chatRoomFactory;
    private final boolean injectUiInfo;

    public FullScenarioTestCase(ChatRoomFactory chatRoomFactory) {
        this(chatRoomFactory, true);
    }

    /**
     * @param chatRoomFactory 시나리오마다 호출되어 새 ChatRoom 생성
     * @param injectUiInfo true 면 각 턴에서 uiInfo 가 있을 때 ActionQueueHandler.setCurrentViewInfo
     *                     로 주입 시도 (실패해도 계속 진행)
     */
    public FullScenarioTestCase(ChatRoomFactory chatRoomFactory, boolean injectUiInfo) {
        if (chatRoomFactory == null) throw new IllegalArgumentException("chatRoomFactory is required");
        this.chatRoomFactory = chatRoomFactory;
        this.injectUiInfo = injectUiInfo;
    }

    /**
     * 한 시나리오를 재현하고 결과 반환. ChatRoom 은 시나리오 단위로 새로 생성/종료.
     */
    public ScenarioTestResult run(ScenarioData scenario) throws Exception {
        return run(scenario, null);
    }

    /**
     * 소스 파일명을 명시적으로 전달 (디버깅 / 리포팅용).
     */
    public ScenarioTestResult run(ScenarioData scenario, String sourceFileName) throws Exception {
        if (scenario == null) throw new IllegalArgumentException("scenario is required");
        ChatRoom chatRoom = chatRoomFactory.create(scenario);
        if (chatRoom == null) {
            return new ScenarioTestResult(sourceFileName,
                    scenario.getSessionId(), scenario.getAiModel(),
                    skipAll(scenario, "chatRoomFactory returned null (API key missing?)"));
        }
        try {
            List<TurnTestResult> turnResults = new ArrayList<>();
            for (ScenarioTurn turn : scenario.getTurns()) {
                turnResults.add(replayTurn(chatRoom, turn));
            }
            return new ScenarioTestResult(sourceFileName,
                    scenario.getSessionId(), scenario.getAiModel(), turnResults);
        } finally {
            try {
                chatRoom.close();
            } catch (Exception e) {
                // close 실패는 무시 — 결과 영향 없음
            }
        }
    }

    private static List<TurnTestResult> skipAll(ScenarioData scenario, String reason) {
        List<TurnTestResult> list = new ArrayList<>();
        for (ScenarioTurn t : scenario.getTurns()) {
            list.add(TurnTestResult.skipped(t.getTurnNo(), reason));
        }
        return list;
    }

    private TurnTestResult replayTurn(ChatRoom chatRoom, ScenarioTurn turn) {
        long start = System.currentTimeMillis();
        try {
            // uiInfo 주입 (옵션)
            if (injectUiInfo && turn.getUiInfo() != null && !turn.getUiInfo().isEmpty()) {
                ActionQueueHandler aq = chatRoom.getActionQueueHandler();
                if (aq != null) {
                    try {
                        aq.setCurrentViewInfo(turn.getUiInfo());
                    } catch (Exception e) {
                        // uiInfo 가 JSON 이 아닌 텍스트일 수도 있으므로 실패 무시 — replay 계속
                    }
                }
            }

            String userMsg = turn.getUserPrompt();
            JSONObject response = chatRoom.getChatting().sendPrompt(userMsg);
            JsonNode actualAq = extractActionQueue(response);
            JsonNode expectedAq = turn.getActionQueue();

            ValidationResult vr = ActionQueueValidator.validate(expectedAq, actualAq);
            long elapsed = System.currentTimeMillis() - start;

            String expStr = expectedAq == null ? "null" : expectedAq.toString();
            String actStr = actualAq == null ? "null" : actualAq.toString();
            if (vr.isExactMatch()) {
                return TurnTestResult.pass(turn.getTurnNo(), expStr, actStr, elapsed);
            }
            return TurnTestResult.fail(turn.getTurnNo(), vr, expStr, actStr, elapsed);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            return TurnTestResult.error(turn.getTurnNo(),
                    e.getClass().getSimpleName() + ": " + e.getMessage(), elapsed);
        }
    }

    private static JsonNode extractActionQueue(JSONObject response) {
        if (response == null) return null;
        Object aq = response.get("action_queue");
        if (aq == null) return null;
        if (aq instanceof JsonNode) return (JsonNode) aq;
        try {
            return MAPPER.readTree(aq.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

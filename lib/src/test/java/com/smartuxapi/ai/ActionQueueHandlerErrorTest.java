package com.smartuxapi.ai;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionQueueHandler 에러 케이스 테스트")
public class ActionQueueHandlerErrorTest {

    private ActionQueueHandler aqh;

    @BeforeEach
    void setUp() {
        aqh = new ActionQueueHandler();
    }

    @Test
    @DisplayName("null viewInfo 설정 시 curViewInfo가 null이 되어야 함")
    void setNullViewInfo() throws ParseException {
        aqh.setCurrentViewInfo(null);

        assertFalse(aqh.isCurrentViewInfo(), "null 설정 후 화면 정보가 없어야 합니다");
        assertNull(aqh.getCurViewPrompt(), "화면 정보가 없으면 프롬프트가 null이어야 합니다");
    }

    @Test
    @DisplayName("빈 문자열 viewInfo 설정 시 예외 발생")
    void setEmptyViewInfo() {
        assertThrows(Exception.class, () -> {
            aqh.setCurrentViewInfo("");
        }, "빈 문자열은 예외를 발생시켜야 합니다");
    }

    @Test
    @DisplayName("잘못된 JSON viewInfo 설정 시 ParseException 발생")
    void setInvalidJsonViewInfo() {
        assertThrows(ParseException.class, () -> {
            aqh.setCurrentViewInfo("{invalid json}");
        }, "잘못된 JSON은 ParseException을 발생시켜야 합니다");
    }

    @Test
    @DisplayName("유효한 JSON 배열 viewInfo 설정 성공")
    void setValidJsonArrayViewInfo() throws ParseException {
        String jsonArray = "[{\"id\":\"btn1\",\"type\":\"click\",\"label\":\"버튼1\"}]";
        aqh.setCurrentViewInfo(jsonArray);

        assertTrue(aqh.isCurrentViewInfo(), "유효한 JSON 배열 설정 후 화면 정보가 있어야 합니다");
    }

    @Test
    @DisplayName("유효한 JSON 객체 viewInfo 설정 성공")
    void setValidJsonObjectViewInfo() throws ParseException {
        String jsonObj = "{\"viewInfo\":[{\"id\":\"btn1\"}]}";
        aqh.setCurrentViewInfo(jsonObj);

        assertTrue(aqh.isCurrentViewInfo(), "유효한 JSON 객체 설정 후 화면 정보가 있어야 합니다");
    }

    @Test
    @DisplayName("화면 정보 변경 감지 테스트")
    void viewInfoChangeDetection() throws ParseException {
        String viewInfo1 = "[{\"id\":\"btn1\",\"type\":\"click\"}]";
        String viewInfo2 = "[{\"id\":\"btn2\",\"type\":\"click\"}]";

        aqh.setCurrentViewInfo(viewInfo1);
        assertTrue(aqh.isViewInfoChanged(), "첫 설정 시 변경으로 감지되어야 합니다");

        aqh.markViewInfoAsSent();
        assertFalse(aqh.isViewInfoChanged(), "전송 후 변경 플래그가 false여야 합니다");

        aqh.setCurrentViewInfo(viewInfo2);
        assertTrue(aqh.isViewInfoChanged(), "다른 정보 설정 시 변경으로 감지되어야 합니다");
    }

    @Test
    @DisplayName("동일한 화면 정보 재설정 시 변경 미감지")
    void sameViewInfoNoChange() throws ParseException {
        String viewInfo = "[{\"id\":\"btn1\",\"type\":\"click\"}]";

        aqh.setCurrentViewInfo(viewInfo);
        aqh.markViewInfoAsSent();

        aqh.setCurrentViewInfo(viewInfo);
        assertFalse(aqh.isViewInfoChanged(), "동일한 정보 재설정 시 변경으로 감지되지 않아야 합니다");
    }

    @Test
    @DisplayName("clearCurrentViewInfo 후 상태 초기화 확인")
    void clearCurrentViewInfo() throws ParseException {
        String viewInfo = "[{\"id\":\"btn1\"}]";
        aqh.setCurrentViewInfo(viewInfo);
        assertTrue(aqh.isCurrentViewInfo());

        aqh.clearCurrentViewInfo();
        assertFalse(aqh.isCurrentViewInfo(), "clear 후 화면 정보가 없어야 합니다");
        assertFalse(aqh.isViewInfoChanged(), "clear 후 변경 플래그가 false여야 합니다");
    }

    @Test
    @DisplayName("curViewInfo 없이 getActionQueuePrompt 호출 시 null 반환")
    void getActionQueuePromptWithoutViewInfo() {
        String result = aqh.getActionQueuePrompt("테스트 메시지");
        assertNull(result, "화면 정보 없이 호출 시 null이어야 합니다");
    }

    @Test
    @DisplayName("addCurrentViewInfo null 입력 시 예외 없이 처리")
    void addNullViewInfo() {
        assertDoesNotThrow(() -> {
            aqh.addCurrentViewInfo(null);
        }, "null 추가 시 예외가 발생하지 않아야 합니다");
    }

    @Test
    @DisplayName("addCurrentViewInfo로 정보 병합 테스트")
    void addCurrentViewInfoMerge() throws Exception {
        String viewInfo = "[{\"id\":\"btn1\"}]";
        aqh.setCurrentViewInfo(viewInfo);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode additional = mapper.readTree("{\"extra\":\"data\",\"count\":5}");
        aqh.addCurrentViewInfo(additional);

        assertTrue(aqh.isCurrentViewInfo(), "병합 후에도 화면 정보가 있어야 합니다");
    }

    @Test
    @DisplayName("getActionQueue - 유효한 action_queue JSON 추출")
    void getActionQueueFromValidResponse() {
        String response = "다음은 action queue입니다: {\"action_queue\":[{\"type\":\"click\",\"id\":\"btn1\"}]}";
        JsonNode result = aqh.getActionQueue(response);

        assertNotNull(result, "유효한 응답에서 action_queue를 추출해야 합니다");
    }

    @Test
    @DisplayName("getActionQueue - JSON이 없는 응답에서 null 반환")
    void getActionQueueFromPlainText() {
        String response = "이것은 일반 텍스트 응답입니다. JSON이 포함되어 있지 않습니다.";
        JsonNode result = aqh.getActionQueue(response);

        assertNull(result, "JSON이 없는 응답에서 null을 반환해야 합니다");
    }
}

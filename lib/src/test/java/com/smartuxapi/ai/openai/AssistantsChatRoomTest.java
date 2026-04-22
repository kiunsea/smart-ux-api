package com.smartuxapi.ai.openai;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.Chatting;
import com.smartuxapi.ai.ConfigLoader;
import com.smartuxapi.ai.openai.assistants.Assistants;
import com.smartuxapi.ai.openai.assistants.AssistantsThread;
import com.smartuxapi.util.FileUtil;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAI Assistants ChatRoom 테스트")
public class AssistantsChatRoomTest {

    private AssistantsThread chatRoom = null;

    @BeforeEach
    void setUp() throws ParseException {
        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        if (config == null) {
            // 설정 파일이 없으면 테스트를 건너뜀
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "dev.apikey.json 파일이 클래스패스에 없습니다. 테스트를 건너뜁니다.");
            return;
        }
        
        String assistantId = config.get("OPENAI_ASSIST_ID").asText();
        String apiKey = config.get("OPENAI_API_KEY").asText();

        Assistants assist = new Assistants(assistantId);
        assist.setApiKey(apiKey);
        this.chatRoom = new AssistantsThread(assist);
        this.chatRoom.setActionQueueHandler(new ActionQueueHandler());
    }

    @AfterEach
    void tearDown() throws Exception {
        if (chatRoom != null) {
            chatRoom.close();
        }
    }

    @Test
    @DisplayName("ChatRoom 생성 테스트")
    public void testChatRoomCreation() {
        assertNotNull(chatRoom, "ChatRoom이 생성되어야 합니다");
        assertNotNull(chatRoom.getId(), "ChatRoom ID가 있어야 합니다");
    }

    @Test
    @DisplayName("일반 대화 테스트")
    @Disabled("API 키가 필요하므로 기본적으로 비활성화")
    public void testChat() throws Exception {
        String prompt = "너는 누구니?";
        testChatInternal(prompt);
    }

    @Test
    @DisplayName("Action Queue를 포함한 채팅 테스트")
    @Disabled("API 키가 필요하므로 기본적으로 비활성화")
    public void testChatAction() throws Exception {
        File currentDirFile = new File(".");
        StringBuilder sb = FileUtil
                .readFile(currentDirFile.getAbsolutePath() + "/src/test/resources/test.easy_kiosc_uif.json", null);
        this.chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + sb);

        String viewInfo = "[{\"id\":\"mega_top_bar_home\",\"type\":\"click\",\"label\":\"mega_top_bar_home\",\"selector\":\"#mega_top_bar_home\",\"xpath\":\"//*[@id='mega_top_bar_home']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"menu_bar_left\",\"type\":\"click\",\"label\":\"<\",\"selector\":\"#menu_bar_left\",\"xpath\":\"//*[@id='menu_bar_left']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"추천(음료)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"추천(디저트)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"커피(HOT)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"커피(ICE)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"menu_bar_right\",\"type\":\"click\",\"label\":\">\",\"selector\":\"#menu_bar_right\",\"xpath\":\"//*[@id='menu_bar_right']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_아메리카노\",\"type\":\"click\",\"label\":\"ice_아메리카노\",\"selector\":\"#ice_아메리카노\",\"xpath\":\"//*[@id='ice_아메리카노']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"유니콘프라페\",\"type\":\"click\",\"label\":\"유니콘프라페\",\"selector\":\"#유니콘프라페\",\"xpath\":\"//*[@id='유니콘프라페']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"딸기쿠키프라페\",\"type\":\"click\",\"label\":\"딸기쿠키프라페\",\"selector\":\"#딸기쿠키프라페\",\"xpath\":\"//*[@id='딸기쿠키프라페']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"커피프라페\",\"type\":\"click\",\"label\":\"커피프라페\",\"selector\":\"#커피프라페\",\"xpath\":\"//*[@id='커피프라페']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_토피넛마끼아또\",\"type\":\"click\",\"label\":\"hot_토피넛마끼아또\",\"selector\":\"#hot_토피넛마끼아또\",\"xpath\":\"//*[@id='hot_토피넛마끼아또']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_토피넛마끼아또\",\"type\":\"click\",\"label\":\"ice_토피넛마끼아또\",\"selector\":\"#ice_토피넛마끼아또\",\"xpath\":\"//*[@id='ice_토피넛마끼아또']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_레몬차\",\"type\":\"click\",\"label\":\"hot_레몬차\",\"selector\":\"#hot_레몬차\",\"xpath\":\"//*[@id='hot_레몬차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_레몬차\",\"type\":\"click\",\"label\":\"ice_레몬차\",\"selector\":\"#ice_레몬차\",\"xpath\":\"//*[@id='ice_레몬차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_사과유자차\",\"type\":\"click\",\"label\":\"hot_사과유자차\",\"selector\":\"#hot_사과유자차\",\"xpath\":\"//*[@id='hot_사과유자차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_사과유자차\",\"type\":\"click\",\"label\":\"ice_사과유자차\",\"selector\":\"#ice_사과유자차\",\"xpath\":\"//*[@id='ice_사과유자차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_허니자몽블랙티\",\"type\":\"click\",\"label\":\"hot_허니자몽블랙티\",\"selector\":\"#hot_허니자몽블랙티\",\"xpath\":\"//*[@id='hot_허니자몽블랙티']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_허니자몽블랙티\",\"type\":\"click\",\"label\":\"ice_허니자몽블랙티\",\"selector\":\"#ice_허니자몽블랙티\",\"xpath\":\"//*[@id='ice_허니자몽블랙티']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"total_price\",\"type\":\"click\",\"label\":\"0원\\n결제하기\",\"selector\":\"#total_price\",\"xpath\":\"//*[@id='total_price']\",\"properties\":{\"enabled\":true,\"visible\":true}}]\r\n";
        this.chatRoom.getActionQueueHandler().setCurrentViewInfo(viewInfo);
        String usrQ = "시원한 레몬차와 따뜻한 허니자몽블랙티를 주문해줘";

        JSONObject resJson = this.chatRoom.getChatting().sendPrompt(usrQ);
        
        assertNotNull(resJson, "응답이 null이 아니어야 합니다");
        assertTrue(resJson.containsKey("message"), "응답에 message가 포함되어야 합니다");
        
        if (resJson.containsKey("action_queue")) {
            assertNotNull(resJson.get("action_queue"), "Action Queue가 null이 아니어야 합니다");
        }
    }

    private void testChatInternal(String prompt) throws Exception {
        Chatting chat = this.chatRoom.getChatting();
        JSONObject resJson = chat.sendPrompt(prompt);
        
        assertNotNull(resJson, "응답이 null이 아니어야 합니다");
        assertTrue(resJson.containsKey("message"), "응답에 message가 포함되어야 합니다");
    }
}

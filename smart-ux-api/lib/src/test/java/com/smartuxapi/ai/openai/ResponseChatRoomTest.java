package com.smartuxapi.ai.openai;

import java.io.File;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.ConfigLoader;
import com.smartuxapi.util.FileUtil;

public class ResponseChatRoomTest {
    
    private ChatRoom chatRoom = null;
    
    public ResponseChatRoomTest() throws ParseException {

        JsonNode config = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String apiKey = config.get("OPENAI_API_KEY").asText();
        String model = config.get("OPENAI_MODEL").asText();
        this.chatRoom = new ResponsesChatRoom(apiKey, model);
        this.chatRoom.setActionQueueHandler(new ActionQueueHandler());
        System.out.println("* [" + this.chatRoom.getId() + "] 채팅방 생성");

    }

    public static void main(String args[]) throws Exception {
        ResponseChatRoomTest resTest = new ResponseChatRoomTest();
        resTest.testChatAction();
    }
    
    public void testChatAction() throws Exception {

        String currentDirectory = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + currentDirectory);
        // File 객체로도 얻을 수 있습니다.
        File currentDirFile = new File(".");
        System.out.println("Current Working Directory (File): " + currentDirFile.getAbsolutePath());
        StringBuilder sb = FileUtil
                .readFile(currentDirFile.getAbsolutePath() + "/src/test/resources/test.easy_kiosc_uif.json", null);
        this.chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + sb);

        String viewInfo = "[{\"id\":\"mega_top_bar_home\",\"type\":\"click\",\"label\":\"mega_top_bar_home\",\"selector\":\"#mega_top_bar_home\",\"xpath\":\"//*[@id='mega_top_bar_home']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"menu_bar_left\",\"type\":\"click\",\"label\":\"<\",\"selector\":\"#menu_bar_left\",\"xpath\":\"//*[@id='menu_bar_left']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"추천(음료)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"추천(디저트)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"커피(HOT)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":null,\"type\":\"click\",\"label\":\"커피(ICE)\",\"selector\":\".mega_menu_1\",\"xpath\":\"//div\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"menu_bar_right\",\"type\":\"click\",\"label\":\">\",\"selector\":\"#menu_bar_right\",\"xpath\":\"//*[@id='menu_bar_right']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_아메리카노\",\"type\":\"click\",\"label\":\"ice_아메리카노\",\"selector\":\"#ice_아메리카노\",\"xpath\":\"//*[@id='ice_아메리카노']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"유니콘프라페\",\"type\":\"click\",\"label\":\"유니콘프라페\",\"selector\":\"#유니콘프라페\",\"xpath\":\"//*[@id='유니콘프라페']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"딸기쿠키프라페\",\"type\":\"click\",\"label\":\"딸기쿠키프라페\",\"selector\":\"#딸기쿠키프라페\",\"xpath\":\"//*[@id='딸기쿠키프라페']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"커피프라페\",\"type\":\"click\",\"label\":\"커피프라페\",\"selector\":\"#커피프라페\",\"xpath\":\"//*[@id='커피프라페']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_토피넛마끼아또\",\"type\":\"click\",\"label\":\"hot_토피넛마끼아또\",\"selector\":\"#hot_토피넛마끼아또\",\"xpath\":\"//*[@id='hot_토피넛마끼아또']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_토피넛마끼아또\",\"type\":\"click\",\"label\":\"ice_토피넛마끼아또\",\"selector\":\"#ice_토피넛마끼아또\",\"xpath\":\"//*[@id='ice_토피넛마끼아또']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_레몬차\",\"type\":\"click\",\"label\":\"hot_레몬차\",\"selector\":\"#hot_레몬차\",\"xpath\":\"//*[@id='hot_레몬차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_레몬차\",\"type\":\"click\",\"label\":\"ice_레몬차\",\"selector\":\"#ice_레몬차\",\"xpath\":\"//*[@id='ice_레몬차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_사과유자차\",\"type\":\"click\",\"label\":\"hot_사과유자차\",\"selector\":\"#hot_사과유자차\",\"xpath\":\"//*[@id='hot_사과유자차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_사과유자차\",\"type\":\"click\",\"label\":\"ice_사과유자차\",\"selector\":\"#ice_사과유자차\",\"xpath\":\"//*[@id='ice_사과유자차']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"hot_허니자몽블랙티\",\"type\":\"click\",\"label\":\"hot_허니자몽블랙티\",\"selector\":\"#hot_허니자몽블랙티\",\"xpath\":\"//*[@id='hot_허니자몽블랙티']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"ice_허니자몽블랙티\",\"type\":\"click\",\"label\":\"ice_허니자몽블랙티\",\"selector\":\"#ice_허니자몽블랙티\",\"xpath\":\"//*[@id='ice_허니자몽블랙티']\",\"properties\":{\"enabled\":true,\"visible\":true}},{\"id\":\"total_price\",\"type\":\"click\",\"label\":\"0원\\n결제하기\",\"selector\":\"#total_price\",\"xpath\":\"//*[@id='total_price']\",\"properties\":{\"enabled\":true,\"visible\":true}}]\r\n";
        this.chatRoom.getActionQueueHandler().setCurrentViewInfo(viewInfo);
        String usrQ = "시원한 레몬차와 따뜻한 허니자몽블랙티를 주문해줘";

        System.out.println("* USER msg: " + usrQ);
        JSONObject resJson = this.chatRoom.getChatting().sendPrompt(usrQ);
        System.out.println("* AI msg(액션큐 응답): " + resJson.get("message"));

        if (resJson.containsKey("action_queue")) {
            System.out.println("* Action Queue: " + resJson.get("action_queue"));
        }

        Object usrFuncRst = null; // resJson.get(OpenAIChatRoom.USER_FUNCTIONS_RESULT);
        if (usrFuncRst != null) {
            Object onJbgRst = ((JSONObject) usrFuncRst).get("on_jangbogo");
            if (onJbgRst != null)
                System.out.println("* AI user_link: " + ((JSONObject) onJbgRst).get("user_link"));
        }
    }
}

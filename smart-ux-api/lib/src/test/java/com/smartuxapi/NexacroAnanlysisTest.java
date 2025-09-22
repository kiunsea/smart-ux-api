package com.smartuxapi;

import java.io.File;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ActionQueueHandler;
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.ConfigLoader;
import com.smartuxapi.ai.gemini.GeminiChatRoom;
import com.smartuxapi.util.FileUtil;

public class NexacroAnanlysisTest {

    private ChatRoom chatRoom = null;

    public NexacroAnanlysisTest() throws ParseException {

        String currentDirectory = System.getProperty("user.dir");
        System.out.println("Current Working Directory: " + currentDirectory);
        
        JsonNode configApikey = ConfigLoader.loadConfigFromClasspath("dev.apikey.json");
        String apiKey = configApikey.get("GEMINI_API_KEY").asText();
        
        JsonNode configPrompt = ConfigLoader.loadConfigFromClasspath("test.config.nexa.json");

        this.chatRoom = new GeminiChatRoom(apiKey, "gemini-2.5-flash");
        this.chatRoom.setActionQueueHandler(new ActionQueueHandler(ActionQueueHandler.FORMAT_NEXACRO, configPrompt));
        
        System.out.println("* [" + this.chatRoom.getId() + "] 채팅방 생성");

    }
    
    public static void main(String args[]) throws Exception {
        
        NexacroAnanlysisTest crTest = new NexacroAnanlysisTest();
        crTest.testNexacroAnalysis();
    }
    
    public void testNexacroAnalysis() throws Exception {

        // File 객체로도 얻을 수 있습니다.
        File currentDirFile = new File(".");
        
        System.out.println("Current Working Directory (File): " + currentDirFile.getAbsolutePath());
        StringBuilder sbUif = FileUtil
                .readFile(currentDirFile.getAbsolutePath() + "/src/test/resources/test.easy_kiosc_uif.json", null);
        this.chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + sbUif);
        
        System.out.println("Current Working Directory (File): " + currentDirFile.getAbsolutePath());
        StringBuilder sbNexaUi = FileUtil
                .readFile(currentDirFile.getAbsolutePath() + "/src/test/resources/test.nexacro-analysis.json", null);
        this.chatRoom.getActionQueueHandler().setCurrentViewInfo(sbNexaUi.toString());
        
        String usrQ = "시작하기 버튼 정보 알려줘";

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

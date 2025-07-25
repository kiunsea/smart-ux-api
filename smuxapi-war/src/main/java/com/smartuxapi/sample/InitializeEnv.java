package com.smartuxapi.sample;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.smartuxapi.ai.SmuThread;
import com.smartuxapi.ai.openai.assistants.Assistant;
import com.smartuxapi.ai.openai.assistants.OpenAIThread;
import com.smartuxapi.util.FileUtil;
import com.smartuxapi.util.PropertiesUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

@WebServlet(name = "loadConfig", urlPatterns = { "/init" }, loadOnStartup = 0)
public class InitializeEnv extends HttpServlet {

    private static final long serialVersionUID = 1L;
    /**
     * 서버 환경 변경시 log4j.properties 설정 정보도 함께 변경 필요
     */
    private Logger log = LogManager.getLogger(InitializeEnv.class);

    public void init() {
		PropertiesUtil.USER_PROPERTIES_PATH = this.getServletContext().getRealPath("/")
				+ "WEB-INF/classes/resources/smuapi.properties";
        
	    String openaiApiKey = PropertiesUtil.get("OPENAI_API_KEY");
	    String openaiAssistId = PropertiesUtil.get("OPENAI_ASSIST_ID");
	    
	    Assistant assist = new Assistant(openaiAssistId);
		assist.setApiKey(openaiApiKey);
		SmuThread chatRoom = null;
		try {
			chatRoom = new OpenAIThread(assist);
			StringBuilder sb = FileUtil.readFile(
					this.getServletContext().getRealPath("/") + "WEB-INF/classes/resources/easy_kiosc_uif.json", null);
			chatRoom.getMessage().sendPrompt("다음의 내용을 학습해 -> " + sb);
			chatRoom.closeThread();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		log.info("Easy KIOSC 초기화 설정 완료!!");
    }
    
}

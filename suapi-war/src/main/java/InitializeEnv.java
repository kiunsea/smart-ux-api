

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.parser.ParseException;

import com.omnibuscode.ai.ChatRoom;
import com.omnibuscode.ai.openai.OpenAIChatRoom;
import com.omnibuscode.ai.openai.assistants.Assistant;
import com.omnibuscode.utils.FileUtil;
import com.omnibuscode.utils.PropertiesUtil;

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
                + "WEB-INF/classes/res/SUAPI.PROPERTIES";
        
	    String openaiApiKey = PropertiesUtil.get("OPENAI_API_KEY");
	    String openaiAssistId = PropertiesUtil.get("OPENAI_ASSIST_ID");
	    
		Assistant assist = new Assistant(openaiAssistId);
		assist.setApiKey(openaiApiKey);
		ChatRoom cr = null;
		try {
			cr = new OpenAIChatRoom(assist);
			String clazzPath = this.getServletContext().getRealPath("/") + "WEB-INF/classes/";
			StringBuilder sb = FileUtil.readFile(clazzPath + "/ctrl/easy_kiosc_uif.json", null);
			cr.getChatting().sendMessage("다음의 내용을 학습해 -> " + sb);
			cr.closeChat();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.debug("초기화 설정 완료~");
    }
    
}

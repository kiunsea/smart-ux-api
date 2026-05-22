# Code Examples

Smart UX API를 사용하는 다양한 실전 예제 모음입니다.

---

## 📋 목차

- [기본 사용법](#기본-사용법)
- [OpenAI 예제](#openai-예제)
- [Gemini 예제](#gemini-예제)
- [Claude 예제](#claude-예제)
- [고급 시나리오](#고급-시나리오)
- [프레임워크 통합](#프레임워크-통합)
- [에러 처리](#에러-처리)

---

## 기본 사용법

### 완전한 예제: 간단한 챗봇

```java
import com.smartuxapi.ai.chatroom.ResponsesChatRoom;
import com.smartuxapi.ai.chatting.ResponsesChatting;

public class SimpleChatbot {
    public static void main(String[] args) throws Exception {
        // 1. ChatRoom 생성
        String apiKey = System.getenv("OPENAI_API_KEY");
        ResponsesChatRoom chatRoom = new ResponsesChatRoom(apiKey, "gpt-4");
        
        // 2. Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        // 3. 현재 화면 정보 설정 (실제 사용 시 JavaScript에서 전달받음)
        String currentViewInfo = getCurrentViewInfo();
        aqHandler.setCurrentViewInfo(currentViewInfo);
        
        // 4. Chatting 인스턴스 생성
        Chatting chatting = chatRoom.getChatting();
        
        // 5. 프롬프트 전송
        String prompt = "아메리카노 2잔 주문해줘";
        JSONObject response = chatting.sendPrompt(prompt);
        
        // 6. 결과 출력
        String message = (String) response.get("message");
        Object actionQueue = response.get("action_queue");
        System.out.println("AI 응답: " + message);
        System.out.println("Action Queue: " + actionQueue);
    }
    
    private static String loadUIFDocument() {
        // UIF 문서 로드 로직
        return "{ \"service\": \"키오스크 주문\" }";
    }
    
    private static String getCurrentViewInfo() {
        // 현재 화면 정보 (JavaScript에서 수집)
        return "{ \"elements\": [...] }";
    }
}
```

---

## OpenAI 예제

### 예제 1: Responses API 기본 사용

```java
public class OpenAIResponsesExample {
    
    public void chatWithAI() {
        // ChatRoom 생성
        ResponsesChatRoom chatRoom = new ResponsesChatRoom(
            "sk-...", 
            "gpt-4"
        );
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        // 현재 화면 정보 설정
        String currentView = getCurrentView();
        aqHandler.setCurrentViewInfo(currentView);
        
        // 대화 시작
        Chatting chatting = chatRoom.getChatting();
        
        // 첫 번째 프롬프트
        JSONObject response1 = chatting.sendPrompt("아메리카노 주문해줘");
        System.out.println("Response 1: " + response1);
        
        // 두 번째 프롬프트 (컨텍스트 유지)
        // 화면 정보가 변경되었다면 다시 설정
        aqHandler.setCurrentViewInfo(getCurrentView());
        JSONObject response2 = chatting.sendPrompt("2잔으로 바꿔줘");
        System.out.println("Response 2: " + response2);
    }
    
    private String getCurrentView() {
        return "{ \"currentScreen\": \"menu\" }";
    }
}
```

### 예제 2: Assistants API 사용

```java
public class OpenAIAssistantsExample {
    
    public void useAssistant() {
        // Assistant 생성
        Assistant assistant = new Assistant(
            "sk-...",
            "asst_abc123"  // Assistant ID
        );
        
        // Thread 생성 (대화 세션)
        AssistantsThread thread = new AssistantsThread(assistant);
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        thread.setActionQueueHandler(aqHandler);
        
        // 현재 화면 정보 설정
        aqHandler.setCurrentViewInfo(getCurrentView());
        
        // 메시지 전송
        Chatting chatting = thread.getChatting();
        JSONObject response = chatting.sendPrompt("아메리카노 주문하고 결제까지 해줘");
        
        System.out.println("Action Queue: " + response.get("action_queue"));
        
        // 같은 Thread에서 계속 대화 가능
        // 화면 정보가 변경되었다면 다시 설정
        aqHandler.setCurrentViewInfo(getCurrentView());
        JSONObject followUp = chatting.sendPrompt("사이즈를 Large로 변경해줘");
        System.out.println("Follow-up: " + followUp);
    }
    
    private String getCurrentView() {
        return "{ \"currentScreen\": \"menu\" }";
    }
}
```

### 예제 3: 고급 옵션 사용

```java
public class AdvancedOpenAIExample {
    
    public void customConfiguration() {
        // 현재 버전(0.6.0)에서는 Builder 패턴을 지원하지 않습니다.
        // 기본 생성자를 사용합니다.
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        // 현재 화면 정보 설정
        aqHandler.setCurrentViewInfo(getView());
        
        Chatting chatting = chatRoom.getChatting();
        JSONObject response = chatting.sendPrompt("주문해줘");
        
        System.out.println(response);
    }
}
```

---

## Gemini 예제

### 예제 1: Gemini API 기본 사용

```java
public class GeminiExample {
    
    public void chatWithGemini() {
        // GeminiChatRoom 생성
        GeminiChatRoom chatRoom = new GeminiChatRoom(
            "AIza...",
            "gemini-pro"
        );
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        // 현재 화면 정보 설정
        aqHandler.setCurrentViewInfo(getCurrentViewInfo());
        
        // Chatting 생성
        Chatting chatting = chatRoom.getChatting();
        
        // 프롬프트 전송
        JSONObject response = chatting.sendPrompt("라떼 한 잔 주문하고 싶어요");
        
        System.out.println("Gemini Response: " + response);
    }
    
    private String loadUIFDocument() {
        return "{ \"service\": \"커피 주문\" }";
    }
    
    private String getCurrentViewInfo() {
        return "{ \"elements\": [...] }";
    }
}
```

### 예제 2: Gemini 고급 옵션

```java
public class AdvancedGeminiExample {
    
    public void customGeminiConfig() {
        // 현재 버전(0.6.0)에서는 Builder 패턴을 지원하지 않습니다.
        // 기본 생성자를 사용합니다.
        GeminiChatRoom chatRoom = new GeminiChatRoom("AIza...", "gemini-1.5-pro");
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        // 현재 화면 정보 설정
        aqHandler.setCurrentViewInfo(getView());
        
        Chatting chatting = chatRoom.getChatting();
        JSONObject response = chatting.sendPrompt("메뉴 추천해줘");
        
        System.out.println(response);
    }
}
```

---

## Claude 예제

> v0.10.0 추가. Anthropic Claude provider 는 OpenAI / Gemini 와 동일한 `ChatRoom` / `Chatting`
> 인터페이스를 사용하므로, 클래스명만 `ClaudeChatRoom` 으로 바꾸면 됩니다.

### 예제 1: Claude API 기본 사용

```java
public class ClaudeExample {

    public void chatWithClaude() {
        // ClaudeChatRoom 생성 (apiKey 는 sk-ant-... 형식)
        ClaudeChatRoom chatRoom = new ClaudeChatRoom(
            "sk-ant-...",
            "claude-sonnet-4-6"
        );

        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);

        // 현재 화면 정보 설정
        aqHandler.setCurrentViewInfo(getCurrentViewInfo());

        // Chatting 생성
        Chatting chatting = chatRoom.getChatting();

        // 프롬프트 전송
        JSONObject response = chatting.sendPrompt("아이스 아메리카노 한 잔 주문해줘");

        System.out.println("Claude Response: " + response);
    }

    private String getCurrentViewInfo() {
        return "{ \"elements\": [...] }";
    }
}
```

### 예제 2: anthropic-version 헤더 지정

```java
public class ClaudeVersionExample {

    public void customApiVersion() {
        // anthropic-version 헤더를 명시적으로 지정 (보통은 생략 — 기본값 2023-06-01)
        ClaudeChatRoom chatRoom = new ClaudeChatRoom(
            "sk-ant-...",
            "claude-opus-4-7",
            "2023-06-01"
        );

        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        aqHandler.setCurrentViewInfo(getView());

        Chatting chatting = chatRoom.getChatting();
        JSONObject response = chatting.sendPrompt("메뉴 추천해줘");

        System.out.println(response);
    }

    private String getView() {
        return "{ \"currentScreen\": \"menu\" }";
    }
}
```

> 💡 Tool Use(`sendPromptWithTools`) · 구조화 응답(`sendPromptWithSchema`) · 프롬프트 캐싱
> (`markAsCacheable`) 사용법은 OpenAI / Gemini 예제와 동일합니다. `lib/doc/` 의 기능별 가이드를 참조하세요.

---

## 고급 시나리오

### 예제 1: 다중 턴 대화

```java
public class MultiTurnConversation {
    
    public void multiTurnChat() {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        Chatting chatting = chatRoom.getChatting();
        
        // 첫 번째 요청
        aqHandler.setCurrentViewInfo(getView());
        JSONObject step1 = chatting.sendPrompt("아메리카노 주문할게요");
        executeActions(step1);
        
        // 두 번째 요청 (컨텍스트 유지)
        aqHandler.setCurrentViewInfo(getView());
        JSONObject step2 = chatting.sendPrompt("핫으로 변경해주세요");
        executeActions(step2);
        
        // 세 번째 요청
        aqHandler.setCurrentViewInfo(getView());
        JSONObject step3 = chatting.sendPrompt("수량 2개로 늘려주세요");
        executeActions(step3);
        
        // 최종 주문
        aqHandler.setCurrentViewInfo(getView());
        JSONObject step4 = chatting.sendPrompt("결제하기");
        executeActions(step4);
    }
    
    private void executeActions(JSONObject response) {
        Object actionQueue = response.get("action_queue");
        System.out.println("Executing: " + actionQueue);
    }
    
    private void executeActions(String actionQueue) {
        // Action Queue 실행 로직
        System.out.println("Executing: " + actionQueue);
    }
}
```

### 예제 2: 에러 재시도 로직

```java
public class RetryExample {
    
    public JSONObject sendWithRetry(String prompt, int maxRetries) {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        Chatting chatting = chatRoom.getChatting();
        
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                return chatting.sendPrompt(prompt);
            } catch (Exception e) {
                attempt++;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt >= maxRetries) {
                    throw new RuntimeException(e);
                }
                
                // 지수 백오프
                try {
                    Thread.sleep((long) Math.pow(2, attempt) * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        
        throw new RuntimeException("Max retries exceeded");
    }
}
```

### 예제 3: Action Queue 후처리

```java
public class ActionQueueProcessing {
    
    public void processActionQueue() {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        aqHandler.setCurrentViewInfo(getView());
        
        Chatting chatting = chatRoom.getChatting();
        JSONObject response = chatting.sendPrompt("주문해줘");
        
        // Action Queue 추출
        Object actionQueueObj = response.get("action_queue");
        if (actionQueueObj == null) {
            System.out.println("Action Queue가 없습니다.");
            return;
        }
        
        // JSON 파싱 (action_queue가 JSON 문자열인 경우)
        org.json.JSONObject aqJson;
        if (actionQueueObj instanceof String) {
            aqJson = new org.json.JSONObject((String) actionQueueObj);
        } else {
            // 이미 JSON 객체인 경우
            aqJson = new org.json.JSONObject(actionQueueObj.toString());
        }
        
        org.json.JSONArray actions = aqJson.getJSONArray("action_queue");
        
        // 각 액션 처리
        for (int i = 0; i < actions.length(); i++) {
            org.json.JSONObject action = actions.getJSONObject(i);
            
            String elementId = action.getString("elementId");
            String actionType = action.getString("action");
            
            System.out.println("Element: " + elementId);
            System.out.println("Action: " + actionType);
            
            // 조건부 실행
            if (actionType.equals("click")) {
                clickElement(elementId);
            } else if (actionType.equals("setValue")) {
                String value = action.getString("value");
                setElementValue(elementId, value);
            }
        }
    }
    
    private void clickElement(String id) {
        System.out.println("Clicking: " + id);
    }
    
    private void setElementValue(String id, String value) {
        System.out.println("Setting " + id + " to " + value);
    }
}
```

---

## 프레임워크 통합

### 예제 1: Spring Boot Integration

```java
@RestController
@RequestMapping("/api/smartux")
public class SmartUXController {
    
    private final ResponsesChatRoom chatRoom;
    
    @Autowired
    public SmartUXController(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.model}") String model,
            ResourceLoader resourceLoader) throws IOException {
        
        this.chatRoom = new ResponsesChatRoom(apiKey, model);
        
        // Action Queue Handler 설정
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
    }
    
    @PostMapping("/chat")
    public ResponseEntity<ActionQueueResponse> chat(
            @RequestBody ChatRequest request) {
        
        try {
            // 현재 화면 정보 설정
            ActionQueueHandler aqHandler = chatRoom.getActionQueueHandler();
            if (request.getViewInfo() != null) {
                aqHandler.setCurrentViewInfo(request.getViewInfo());
            }
            
            Chatting chatting = chatRoom.getChatting();
            JSONObject response = chatting.sendPrompt(request.getPrompt());
            
            return ResponseEntity.ok(
                new ActionQueueResponse(response)
            );
            
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ActionQueueResponse(
                    "error", 
                    e.getMessage()
                ));
        }
    }
}

// DTO 클래스
record ChatRequest(String prompt, String viewInfo) {}
record ActionQueueResponse(org.json.simple.JSONObject response, String error) {
    ActionQueueResponse(org.json.simple.JSONObject response) {
        this(response, null);
    }
    ActionQueueResponse(String error, String message) {
        this(null, message);
    }
}
```

### 예제 2: Servlet Filter로 세션 관리

```java
@WebFilter("/api/*")
public class SmartUXSessionFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        
        // ChatRoom을 세션에 저장
        ResponsesChatRoom chatRoom = (ResponsesChatRoom) 
            session.getAttribute("chatRoom");
        
        if (chatRoom == null) {
            // 새 세션 - ChatRoom 생성
            String apiKey = getServletContext().getInitParameter("openai.api.key");
            String model = getServletContext().getInitParameter("openai.model");
            
            chatRoom = new ResponsesChatRoom(apiKey, model);
            
            // Action Queue Handler 설정
            ActionQueueHandler aqHandler = new ActionQueueHandler();
            chatRoom.setActionQueueHandler(aqHandler);
            
            session.setAttribute("chatRoom", chatRoom);
        }
        
        chain.doFilter(request, response);
    }
    
}
```

---

## 에러 처리

### 예제 1: 포괄적인 에러 처리

```java
public class ComprehensiveErrorHandling {
    
    public JSONObject sendMessageSafely(String prompt, String viewInfo) {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
        
        if (viewInfo != null) {
            try {
                aqHandler.setCurrentViewInfo(viewInfo);
            } catch (ParseException e) {
                System.err.println("화면 정보 파싱 실패: " + e.getMessage());
                throw new RuntimeException("Invalid view info format", e);
            }
        }
        
        Chatting chatting = chatRoom.getChatting();
        
        try {
            return chatting.sendPrompt(prompt);
            
        } catch (Exception e) {
            // 에러 처리
            String errorMsg = e.getMessage();
            if (errorMsg != null) {
                if (errorMsg.contains("401")) {
                    System.err.println("API Key가 유효하지 않습니다.");
                } else if (errorMsg.contains("429")) {
                    System.err.println("Rate limit 초과. 잠시 후 다시 시도하세요.");
                } else if (errorMsg.contains("500")) {
                    System.err.println("OpenAI 서버 오류.");
                } else {
                    System.err.println("API Error: " + errorMsg);
                }
            } else {
                System.err.println("예상치 못한 오류: " + e.getClass().getSimpleName());
            }
            throw new RuntimeException("Failed to send message", e);
        }
    }
}
```

---

## JavaScript 클라이언트 예제

### 예제 1: 전체 플로우

```javascript
// smart-ux-collector.js는 자동으로 실행되며 window.uiSnapshot에 UI 정보를 저장합니다
// 스크립트 태그로 로드하면 자동 실행됩니다:
// <script src="/lib/smart-ux-collector.js"></script>

// 사용자 프롬프트 가져오기
const userPrompt = document.getElementById('promptInput').value;

// 서버에 전송
fetch('/api/chat', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
    },
    body: JSON.stringify({
        prompt: userPrompt,
        viewInfo: JSON.stringify(window.uiSnapshot || [])  // 자동 수집된 정보 사용
    })
})
.then(response => response.json())
.then(data => {
    // Action Queue 실행
    // smart-ux-client.js를 모듈로 로드
    import('/lib/smart-ux-client.js').then(module => {
        const actions = Array.isArray(data.action_queue) 
            ? data.action_queue 
            : JSON.parse(data.action_queue);
        return module.doActions(actions);
    });
})
.catch(error => {
    console.error('Error:', error);
});
```

**참고:**
- `smart-ux-collector.js`는 자동 실행되므로 별도로 호출할 필요가 없습니다
- `smart-ux-client.js`는 ES6 모듈이므로 `import` 문 또는 `<script type="module">`로 로드해야 합니다

### 예제 2: 실시간 업데이트

```javascript
// smart-ux-client.js를 모듈로 로드
let doActions;
import('/lib/smart-ux-client.js').then(module => {
    doActions = module.doActions;
});

class SmartUXApp {
    async sendPrompt(prompt) {
        try {
            // smart-ux-collector.js가 자동으로 수집한 정보 사용
            // window.uiSnapshot에 이미 저장되어 있음
            const viewInfo = window.uiSnapshot || [];
            
            // 서버로 전송
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ 
                    prompt, 
                    viewInfo: JSON.stringify(viewInfo) 
                })
            });
            
            const data = await response.json();
            
            // Action Queue 실행
            const actions = Array.isArray(data.action_queue) 
                ? data.action_queue 
                : JSON.parse(data.action_queue);
            
            await doActions(actions);
            
            // 성공 알림
            this.showNotification('명령이 실행되었습니다.', 'success');
            
        } catch (error) {
            console.error('Error:', error);
            this.showNotification('오류가 발생했습니다.', 'error');
        }
    }
    
    showNotification(message, type) {
        console.log(`[${type.toUpperCase()}] ${message}`);
    }
}

// 사용
const app = new SmartUXApp();
app.sendPrompt('아메리카노 주문해줘');
```

---

## 더 많은 예제

완전한 동작 예제는 [smuxapi-demo 프로젝트](../smuxapi-demo/)를 참조하세요.

- 키오스크 주문 시스템
- 음성 명령 통합
- 다양한 UI 패턴

---

## 추가 리소스

- [API Reference](API.md)
- [설치 가이드](INSTALL.md)
- [문제 해결](TROUBLESHOOTING.md)


# Code Examples

Smart UX API를 사용하는 다양한 실전 예제 모음입니다.

---

## 📋 목차

- [기본 사용법](#기본-사용법)
- [OpenAI 예제](#openai-예제)
- [Gemini 예제](#gemini-예제)
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
        
        // 2. UIF 문서 로드
        String uifDocument = loadUIFDocument();
        chatRoom.addSystemMessage(uifDocument);
        
        // 3. Chatting 인스턴스 생성
        ResponsesChatting chatting = chatRoom.createChatting();
        
        // 4. 프롬프트 전송
        String prompt = "아메리카노 2잔 주문해줘";
        String viewInfo = getCurrentViewInfo();
        
        String actionQueue = chatting.sendMessage(prompt, viewInfo);
        
        // 5. 결과 출력
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
        
        // UIF 문서 추가
        String uif = """
        {
            "service": "메가커피 키오스크",
            "menus": [
                {"id": "americano", "name": "아메리카노", "price": 2000},
                {"id": "latte", "name": "라떼", "price": 3000}
            ]
        }
        """;
        chatRoom.addSystemMessage(uif);
        
        // 대화 시작
        ResponsesChatting chatting = chatRoom.createChatting();
        
        // 첫 번째 프롬프트
        String response1 = chatting.sendMessage(
            "아메리카노 주문해줘",
            getCurrentView()
        );
        System.out.println("Response 1: " + response1);
        
        // 두 번째 프롬프트 (컨텍스트 유지)
        String response2 = chatting.sendMessage(
            "2잔으로 바꿔줘",
            getCurrentView()
        );
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
        AssistantsThread thread = assistant.createThread();
        
        // 메시지 전송
        AssistantsMessage message = thread.createMessage();
        String actionQueue = message.sendMessage(
            "아메리카노 주문하고 결제까지 해줘",
            getCurrentView()
        );
        
        System.out.println("Action Queue: " + actionQueue);
        
        // 같은 Thread에서 계속 대화 가능
        String followUp = message.sendMessage(
            "사이즈를 Large로 변경해줘",
            getCurrentView()
        );
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
        ResponsesChatRoom chatRoom = new ResponsesChatRoom.Builder()
            .apiKey("sk-...")
            .model("gpt-4")
            .temperature(0.7)        // 창의성 조절
            .maxTokens(2000)         // 최대 응답 길이
            .timeout(30000)          // 30초 타임아웃
            .retryCount(3)           // 실패 시 3번 재시도
            .topP(0.9)               // Nucleus sampling
            .presencePenalty(0.6)    // 주제 다양성
            .frequencyPenalty(0.5)   // 반복 감소
            .build();
        
        chatRoom.addSystemMessage(loadUIF());
        
        ResponsesChatting chatting = chatRoom.createChatting();
        String response = chatting.sendMessage("주문해줘", getView());
        
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
        
        // UIF 문서 추가
        String uif = loadUIFDocument();
        chatRoom.addSystemMessage(uif);
        
        // Chatting 생성
        GeminiChatting chatting = chatRoom.createChatting();
        
        // 프롬프트 전송
        String actionQueue = chatting.sendMessage(
            "라떼 한 잔 주문하고 싶어요",
            getCurrentViewInfo()
        );
        
        System.out.println("Gemini Response: " + actionQueue);
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
        GeminiChatRoom chatRoom = new GeminiChatRoom.Builder()
            .apiKey("AIza...")
            .model("gemini-1.5-pro")
            .temperature(0.5)
            .topP(0.9)
            .topK(40)
            .maxOutputTokens(1024)
            .build();
        
        chatRoom.addSystemMessage(loadUIF());
        
        GeminiChatting chatting = chatRoom.createChatting();
        String response = chatting.sendMessage(
            "메뉴 추천해줘",
            getView()
        );
        
        System.out.println(response);
    }
}
```

---

## 고급 시나리오

### 예제 1: 다중 턴 대화

```java
public class MultiTurnConversation {
    
    public void multiTurnChat() {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        chatRoom.addSystemMessage(loadUIF());
        
        ResponsesChatting chatting = chatRoom.createChatting();
        
        // 첫 번째 요청
        String step1 = chatting.sendMessage(
            "아메리카노 주문할게요",
            getView()
        );
        executeActions(step1);
        
        // 두 번째 요청 (컨텍스트 유지)
        String step2 = chatting.sendMessage(
            "핫으로 변경해주세요",
            getView()
        );
        executeActions(step2);
        
        // 세 번째 요청
        String step3 = chatting.sendMessage(
            "수량 2개로 늘려주세요",
            getView()
        );
        executeActions(step3);
        
        // 최종 주문
        String step4 = chatting.sendMessage(
            "결제하기",
            getView()
        );
        executeActions(step4);
        
        // 대화 이력 조회
        List<Message> history = chatting.getConversationHistory();
        System.out.println("Total messages: " + history.size());
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
    
    public String sendWithRetry(String prompt, String viewInfo, int maxRetries) {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        chatRoom.addSystemMessage(loadUIF());
        ResponsesChatting chatting = chatRoom.createChatting();
        
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                return chatting.sendMessage(prompt, viewInfo);
            } catch (APIException e) {
                attempt++;
                System.err.println("Attempt " + attempt + " failed: " + e.getMessage());
                
                if (attempt >= maxRetries) {
                    throw e;
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
        chatRoom.addSystemMessage(loadUIF());
        ResponsesChatting chatting = chatRoom.createChatting();
        
        String rawResponse = chatting.sendMessage("주문해줘", getView());
        
        // JSON 파싱
        JSONObject response = new JSONObject(rawResponse);
        JSONArray actions = response.getJSONArray("actions");
        
        // 각 액션 처리
        for (int i = 0; i < actions.length(); i++) {
            JSONObject action = actions.getJSONObject(i);
            
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
        
        // UIF 문서 로드
        Resource resource = resourceLoader.getResource("classpath:uif.json");
        String uif = new String(
            resource.getInputStream().readAllBytes(),
            StandardCharsets.UTF_8
        );
        chatRoom.addSystemMessage(uif);
    }
    
    @PostMapping("/chat")
    public ResponseEntity<ActionQueueResponse> chat(
            @RequestBody ChatRequest request) {
        
        try {
            ResponsesChatting chatting = chatRoom.createChatting();
            String actionQueue = chatting.sendMessage(
                request.getPrompt(),
                request.getViewInfo()
            );
            
            return ResponseEntity.ok(
                new ActionQueueResponse(actionQueue)
            );
            
        } catch (APIException e) {
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
record ActionQueueResponse(String actionQueue, String error) {
    ActionQueueResponse(String actionQueue) {
        this(actionQueue, null);
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
            chatRoom.addSystemMessage(loadUIF());
            
            session.setAttribute("chatRoom", chatRoom);
        }
        
        chain.doFilter(request, response);
    }
    
    private String loadUIF() {
        // UIF 로드 로직
        return "{}";
    }
}
```

---

## 에러 처리

### 예제 1: 포괄적인 에러 처리

```java
public class ComprehensiveErrorHandling {
    
    public String sendMessageSafely(String prompt, String viewInfo) {
        ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
        chatRoom.addSystemMessage(loadUIF());
        ResponsesChatting chatting = chatRoom.createChatting();
        
        try {
            return chatting.sendMessage(prompt, viewInfo);
            
        } catch (APIException e) {
            // API 에러 처리
            switch (e.getStatusCode()) {
                case 401:
                    System.err.println("API Key가 유효하지 않습니다.");
                    break;
                case 429:
                    System.err.println("Rate limit 초과. 잠시 후 다시 시도하세요.");
                    break;
                case 500:
                    System.err.println("OpenAI 서버 오류.");
                    break;
                default:
                    System.err.println("API Error: " + e.getMessage());
            }
            throw e;
            
        } catch (NetworkException e) {
            // 네트워크 에러 처리
            System.err.println("네트워크 연결 실패: " + e.getMessage());
            throw e;
            
        } catch (JSONException e) {
            // JSON 파싱 에러
            System.err.println("응답 파싱 실패: " + e.getMessage());
            throw new RuntimeException("Invalid response format", e);
            
        } catch (Exception e) {
            // 기타 에러
            System.err.println("예상치 못한 오류: " + e.getMessage());
            throw new RuntimeException("Unexpected error", e);
        }
    }
}
```

---

## JavaScript 클라이언트 예제

### 예제 1: 전체 플로우

```javascript
// UI 정보 수집
const collector = new SmartUXCollector();
const viewInfo = collector.collectUIInfo();

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
        viewInfo: JSON.stringify(viewInfo)
    })
})
.then(response => response.json())
.then(data => {
    // Action Queue 실행
    const client = new SmartUXClient();
    client.executeActionQueue(data.actionQueue);
})
.catch(error => {
    console.error('Error:', error);
});
```

### 예제 2: 실시간 업데이트

```javascript
class SmartUXApp {
    constructor() {
        this.collector = new SmartUXCollector();
        this.client = new SmartUXClient();
    }
    
    async sendPrompt(prompt) {
        try {
            // 현재 화면 정보 수집
            const viewInfo = this.collector.collectUIInfo();
            
            // 서버로 전송
            const response = await fetch('/api/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt, viewInfo })
            });
            
            const data = await response.json();
            
            // Action Queue 실행
            await this.client.executeActionQueue(data.actionQueue);
            
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

완전한 동작 예제는 [smuxapi-war 프로젝트](../smuxapi-war/)를 참조하세요.

- 키오스크 주문 시스템
- 음성 명령 통합
- 다양한 UI 패턴

---

## 추가 리소스

- [API Reference](API.md)
- [설치 가이드](INSTALL.md)
- [문제 해결](TROUBLESHOOTING.md)


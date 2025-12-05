# Smart UX API - API 문서

## 목차

1. [개요](#개요)
2. [설치 및 설정](#설치-및-설정)
3. [핵심 인터페이스](#핵심-인터페이스)
4. [OpenAI Responses API](#openai-responses-api)
5. [Google Gemini API](#google-gemini-api)
6. [OpenAI Assistants API](#openai-assistants-api)
7. [ActionQueueHandler](#actionqueuehandler)
8. [ConfigLoader](#configloader)
9. [JavaScript API](#javascript-api)
10. [사용 예제](#사용-예제)

---

## 개요

**Smart UX API**는 AI 모델을 활용하여 웹 애플리케이션의 UI를 제어할 수 있는 Java 기반 라이브러리입니다. OpenAI의 Responses/Assistants API와 Google Gemini API를 지원하며, 사용자의 자연어 명령을 UI 액션으로 변환하는 기능을 제공합니다.

### 주요 기능

- **대화 세션 관리**: `ChatRoom`을 통한 대화 컨텍스트 유지
- **AI 모델 지원**: OpenAI Responses/Assistants API, Google Gemini API
- **Action Queue 생성**: 사용자 요청을 UI 액션으로 변환
- **현재 화면 정보 처리**: JavaScript 클라이언트를 통한 UI 상태 수집

---

## 설치 및 설정

### 1. JAR 파일 추가

`lib/build/libs/smart-ux-api-0.6.0.jar` 파일을 웹 애플리케이션의 `/WEB-INF/lib/` 디렉터리에 복사합니다.

### 2. JavaScript 파일 추가

`lib/src/main/js/*.js` 파일을 웹 애플리케이션의 `[DOC ROOT]/smuxapi` 디렉터리에 추가합니다.

웹 페이지에 다음 스크립트를 포함시킵니다:

```html
<script src="/smuxapi/smart-ux-client.js"></script>
<script src="/smuxapi/smart-ux-collector.js"></script>
```

### 3. 의존성

다음 의존성이 필요합니다:

- Java 17 이상
- Jackson Databind 2.15.3
- JSON Simple 1.1.1
- JSON (org.json) 20250517
- Apache Log4j 2.21.0
- Jakarta Servlet API 5.0.0

---

## 핵심 인터페이스

### ChatRoom

대화 세션을 관리하는 기본 인터페이스입니다. AI 모델과의 대화 컨텍스트를 유지합니다.

```java
public interface ChatRoom {
    public String getId();
    public Chatting getChatting();
    public boolean close() throws IOException, ParseException;
    public void setActionQueueHandler(ActionQueueHandler aqHandler);
    public ActionQueueHandler getActionQueueHandler();
}
```

#### 메서드

- `getId()`: ChatRoom의 고유 ID를 반환합니다.
- `getChatting()`: `Chatting` 인스턴스를 반환합니다.
- `close()`: ChatRoom을 종료하고 리소스를 정리합니다.
  - OpenAI Assistants API의 경우 Thread를 삭제합니다.
  - `IOException`, `ParseException`을 발생시킬 수 있습니다.
- `setActionQueueHandler(ActionQueueHandler)`: Action Queue 핸들러를 설정합니다.
- `getActionQueueHandler()`: 설정된 Action Queue 핸들러를 반환합니다.

#### 구현 클래스

- `ResponsesChatRoom`: OpenAI Responses API용
- `GeminiChatRoom`: Google Gemini API용
- `AssistantsThread`: OpenAI Assistants API용

### Chatting

개별 대화를 처리하는 인터페이스입니다. 사용자의 프롬프트를 전송하고 AI의 응답을 받습니다.

```java
public interface Chatting {
    public void setActionQueueHandler(ActionQueueHandler aqHandler);
    public JSONObject sendPrompt(String userMsg) throws Exception;
    public Set<String> getMessageIdSet();
}
```

#### 메서드

- `setActionQueueHandler(ActionQueueHandler)`: Action Queue 핸들러를 설정합니다.
- `sendPrompt(String userMsg)`: 사용자 메시지를 전송하고 응답을 받습니다.
  - 반환값: `{"message": String, "action_queue": JSON String, "userFunctionsResult": JSON String}` 형식의 JSONObject
  - `Exception`을 발생시킬 수 있습니다.
- `getMessageIdSet()`: 보유하고 있는 메시지 ID Set을 반환합니다.
  - Gemini API의 경우 `null`을 반환합니다.

#### 구현 클래스

- `ResponsesChatting`: OpenAI Responses API용
- `GeminiChatting`: Google Gemini API용
- `AssistantsMessage`: OpenAI Assistants API용

---

## OpenAI Responses API

### ResponsesChatRoom

OpenAI Responses API를 사용하는 ChatRoom 구현체입니다.

#### 생성자

```java
public ResponsesChatRoom(String apiKey, String modelName)
```

- `apiKey`: OpenAI API Key
- `modelName`: 사용할 모델 이름 (예: "gpt-4", "gpt-3.5-turbo")

#### 사용 예제

```java
// API Key 및 모델 설정
String apiKey = "your-openai-api-key";
String model = "gpt-4";

// ChatRoom 생성
ResponsesChatRoom chatRoom = new ResponsesChatRoom(apiKey, model);

// Chatting 인스턴스 생성
Chatting chatting = chatRoom.getChatting();

// Action Queue Handler 설정 (선택사항)
ActionQueueHandler aqHandler = new ActionQueueHandler();
chatRoom.setActionQueueHandler(aqHandler);

// 사용자 프롬프트 전송
JSONObject response = chatting.sendPrompt("아이스 아메리카노 2잔 주문해줘");
String message = (String) response.get("message");
Object actionQueue = response.get("action_queue");

// ChatRoom 종료
chatRoom.close();
```

### ResponsesChatting

OpenAI Responses API를 사용하는 Chatting 구현체입니다.

#### 주의사항

- `getMessageIdSet()` 메서드는 `null`을 반환합니다 (Gemini API와 동일).

---

## Google Gemini API

### GeminiChatRoom

Google Gemini API를 사용하는 ChatRoom 구현체입니다.

#### 생성자

```java
public GeminiChatRoom(String apiKey, String modelName)
```

- `apiKey`: Google Gemini API Key
- `modelName`: 사용할 모델 이름 (예: "gemini-pro", "gemini-1.5-pro")

#### 사용 예제

```java
// API Key 및 모델 설정
String apiKey = "your-gemini-api-key";
String model = "gemini-pro";

// ChatRoom 생성
GeminiChatRoom chatRoom = new GeminiChatRoom(apiKey, model);

// Chatting 인스턴스 생성
Chatting chatting = chatRoom.getChatting();

// Action Queue Handler 설정 (선택사항)
ActionQueueHandler aqHandler = new ActionQueueHandler();
chatRoom.setActionQueueHandler(aqHandler);

// 사용자 프롬프트 전송
JSONObject response = chatting.sendPrompt("아이스 아메리카노 2잔 주문해줘");
String message = (String) response.get("message");
Object actionQueue = response.get("action_queue");

// ChatRoom 종료
chatRoom.close();
```

### GeminiChatting

Google Gemini API를 사용하는 Chatting 구현체입니다.

#### 주의사항

- `getMessageIdSet()` 메서드는 `null`을 반환합니다.

---

## OpenAI Assistants API

### Assistants

OpenAI Assistants API의 Assistant 정보를 관리하는 클래스입니다.

#### 생성자

```java
public Assistants(String assistantId)
```

- `assistantId`: OpenAI Assistant ID

#### 메서드

- `getAssistantId()`: Assistant ID를 반환합니다.
- `getApiKey()`: API Key를 반환합니다.
- `setApiKey(String apiKey)`: API Key를 설정합니다.

#### 사용 예제

```java
String assistantId = "asst_xxxxxxxxxxxxx";
Assistants assistant = new Assistants(assistantId);
assistant.setApiKey("your-openai-api-key");
```

### AssistantsThread

OpenAI Assistants API의 Thread를 관리하는 ChatRoom 구현체입니다.

#### 생성자

```java
public AssistantsThread(Assistants assistInfo) throws ParseException
```

- `assistInfo`: `Assistants` 인스턴스

#### 메서드

- `setFunctionMap(Map usrFuncs)`: 사용자 함수 맵을 설정합니다 (TODO: 추후 구현 예정).

#### 사용 예제

```java
// Assistant 생성
Assistants assistant = new Assistants("asst_xxxxxxxxxxxxx");
assistant.setApiKey("your-openai-api-key");

// Thread 생성 (ChatRoom 역할)
AssistantsThread thread = new AssistantsThread(assistant);

// Chatting 인스턴스 생성
Chatting chatting = thread.getChatting();

// Action Queue Handler 설정 (선택사항)
ActionQueueHandler aqHandler = new ActionQueueHandler();
thread.setActionQueueHandler(aqHandler);

// 사용자 프롬프트 전송
JSONObject response = chatting.sendPrompt("아이스 아메리카노 2잔 주문해줘");
String message = (String) response.get("message");
Object actionQueue = response.get("action_queue");

// Thread 종료 (Thread 삭제)
thread.close();
```

### AssistantsMessage

OpenAI Assistants API의 메시지를 관리하는 Chatting 구현체입니다.

#### 생성자

```java
public AssistantsMessage(AssistantsAPIConnection connApi, String idThread)
public AssistantsMessage(Chatting chatting, AssistantsAPIConnection connApi, String idThread)
```

#### 주의사항

- `sendPrompt()` 메서드는 Thread에 메시지를 전송한 후 Run을 생성하고 완료될 때까지 대기합니다 (폴링 방식).
- Function Call 기능은 현재 TODO 상태입니다 (추후 구현 예정).

---

## ActionQueueHandler

Action Queue를 처리하는 클래스입니다. 현재 화면 정보를 관리하고, Action Queue 요청 프롬프트를 생성하며, AI 응답에서 Action Queue를 추출합니다.

### 생성자

```java
// 기본 생성자 (HTML Format, 기본 config.json 사용)
public ActionQueueHandler()

// 사용자 지정 생성자
public ActionQueueHandler(String formatUi, JsonNode configPrompt)
```

- `formatUi`: UI 포맷 (`ActionQueueHandler.FORMAT_HTML`)
- `configPrompt`: 설정 프롬프트 JSON (`JsonNode`)

### 상수

- `FORMAT_HTML`: HTML 포맷

### 메서드

#### `setCurrentViewInfo(String curViewInfo)`

현재 화면 정보를 저장합니다.

- 파라미터: `curViewInfo` - 현재 화면 정보 JSON 문자열 (배열 또는 객체)
- 예외: `ParseException`

#### `isCurrentViewInfo()`

현재 화면 정보가 저장되어 있는지 확인합니다.

- 반환값: `boolean` - 화면 정보 저장 여부

#### `getCurViewPrompt()`

현재 화면 정보 설정에 대한 프롬프트를 반환합니다.

- 반환값: `String` - Current View Prompt

#### `getActionQueuePrompt(String userMsg)`

Action Queue 생성 요청 프롬프트를 반환합니다.

- 파라미터: `userMsg` - 사용자 메시지
- 반환값: `String` - Action Queue Prompt
- 주의: 현재 화면 정보가 필수로 저장되어 있어야 합니다.

#### `getActionQueue(String resMsg)`

AI 응답 메시지에서 Action Queue를 추출합니다.

- 파라미터: `resMsg` - AI 응답 메시지
- 반환값: `JsonNode` - Action Queue JSON (또는 전체 응답 JSON)

#### `clearCurrentViewInfo()`

저장된 현재 화면 정보를 삭제합니다.

### 사용 예제

```java
// 기본 Action Queue Handler 생성
ActionQueueHandler aqHandler = new ActionQueueHandler();

// 현재 화면 정보 설정 (JavaScript에서 전달받은 JSON 문자열)
String currentViewInfo = getCurrentViewInfoFromClient();
aqHandler.setCurrentViewInfo(currentViewInfo);

// ChatRoom에 설정
chatRoom.setActionQueueHandler(aqHandler);

// 사용자 프롬프트 전송 시 자동으로 Action Queue가 처리됩니다
JSONObject response = chatting.sendPrompt("아이스 아메리카노 주문해줘");
Object actionQueue = response.get("action_queue");
```

### Config 파일 구조

`config.json` 파일은 클래스패스 루트 (`src/main/resources/`)에 위치해야 합니다.

```json
{
  "prompt": {
    "cur_view_info": [
      " 다음의 내용은 사용자가 현재 보고있는 화면에서 사용자 액션이 가능한 ui element structure 이다.",
      " `${CurViewInfo}`",
      " 이 화면 정보를 이용해서 action_queue 의 내용을 작성해야 한다."
    ],
    "action_queue": [
      " 사용자의 `${UserMsg}` 명령을 수행 할 수 있도록 id가 action_queue 인 JSON 내용을 작성해야 한다.",
      " action_queue의 내용은 현재 화면 정보의 ui element structure 를 참고해서 작성해야 한다.",
      " action_queue는 root에 작성하고 값은 array로 action queue내용이 담겨 있어야 한다.",
      " action_queue format은 다음과 같다. {'action_queue':[{'attribute':'value'}]}"
    ]
  }
}
```

---

## ConfigLoader

클래스패스에서 JSON 설정 파일을 로드하는 유틸리티 클래스입니다.

### 메서드

#### `loadConfigFromClasspath()`

기본 파일명 (`config.json`)으로 설정 파일을 로드합니다.

- 반환값: `JsonNode` - 로드된 설정 파일의 JsonNode

#### `loadConfigFromClasspath(String confFileName)`

지정한 파일명으로 설정 파일을 로드합니다.

- 파라미터: `confFileName` - 설정 파일명 (null인 경우 `config.json` 사용)
- 반환값: `JsonNode` - 로드된 설정 파일의 JsonNode (파일을 찾지 못하거나 파싱 오류 시 `null`)

### 사용 예제

```java
// 기본 config.json 로드
JsonNode config = ConfigLoader.loadConfigFromClasspath();

// 지정한 파일 로드
JsonNode customConfig = ConfigLoader.loadConfigFromClasspath("custom-config.json");

// Action Queue Handler에 설정 적용
ActionQueueHandler aqHandler = new ActionQueueHandler(ActionQueueHandler.FORMAT_HTML, config);
```

---

## JavaScript API

### smart-ux-collector.js

웹 페이지의 UI 정보를 수집하고 서버로 전송하는 클라이언트 스크립트입니다.

#### 주요 기능

- DOM 변경 감지 (MutationObserver)
- 이벤트 바인딩된 요소 수집
- 현재 화면 정보를 서버로 자동 전송

#### 설정

스크립트 내부의 `SERVER_ENDPOINT` 변수를 수정하여 서버 엔드포인트를 설정합니다.

```javascript
const SERVER_ENDPOINT = '/suapi/collect';  // 실제 서버 URL로 교체
```

#### 수집되는 정보

각 UI 요소에 대해 다음 정보를 수집합니다:

- `id`: 요소 ID
- `type`: 이벤트 타입 또는 태그명
- `label`: 라벨 (innerText, value, placeholder, id 순으로 우선순위)
- `selector`: CSS 선택자
- `xpath`: XPath
- `properties.enabled`: 활성화 여부
- `properties.visible`: 표시 여부

#### 동작 방식

1. 페이지 로드 시 2초 후 자동 수집 및 전송
2. DOM 변경 감지 시 2초 대기 후 자동 수집 및 전송
3. 수집된 정보를 `window.uiSnapshot`에 저장

### smart-ux-client.js

Action Queue를 실행하는 클라이언트 스크립트입니다.

#### 주요 함수

##### `doActions(actions)`

Action Queue를 실행합니다.

- 파라미터: `actions` - Action 배열

#### 지원하는 Action 타입

##### `click`

요소를 클릭합니다.

```javascript
{
  "type": "click",
  "id": "element-id"
}
```

##### `scroll`

페이지를 스크롤합니다.

```javascript
{
  "type": "scroll",
  "position": 500
}
```

##### `setAttribute`

요소의 속성을 설정합니다.

```javascript
{
  "type": "setAttribute",
  "id": "element-id",
  "attrName": "value",
  "attrValue": "new-value"
}
```

##### `navigate`

페이지를 이동합니다. 남은 액션은 localStorage에 저장되어 다음 페이지에서 실행됩니다.

```javascript
{
  "type": "navigate",
  "url": "/next-page.html"
}
```

#### 사용 예제

```javascript
// Action Queue 실행
const actions = [
  { "type": "click", "id": "menu_americano" },
  { "type": "setAttribute", "id": "order_count", "attrName": "value", "attrValue": "2" },
  { "type": "click", "id": "order_btn" }
];

await doActions(actions);
```

#### 페이지 이동 시 액션 유지

`navigate` 액션 실행 시, 남은 액션은 localStorage에 저장되어 다음 페이지 로드 시 자동으로 실행됩니다.

---

## 사용 예제

### 완전한 Servlet 구현 예제

#### Action Queue 응답 Servlet

```java
@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
    
    private ResponsesChatRoom chatRoom;
    private ActionQueueHandler aqHandler;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // API 설정 로드
        String apiKey = getServletContext().getInitParameter("openai.api.key");
        String model = getServletContext().getInitParameter("openai.model");
        
        // ChatRoom 초기화
        chatRoom = new ResponsesChatRoom(apiKey, model);
        
        // Action Queue Handler 초기화
        aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        try {
            // 요청 파라미터 추출
            String userPrompt = req.getParameter("prompt");
            String currentViewInfo = req.getParameter("viewInfo");
            
            // 현재 화면 정보 설정
            if (currentViewInfo != null) {
                aqHandler.setCurrentViewInfo(currentViewInfo);
            }
            
            // Chatting 생성 및 메시지 전송
            Chatting chatting = chatRoom.getChatting();
            JSONObject response = chatting.sendPrompt(userPrompt);
            
            // JSON 응답
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(response.toJSONString());
            
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }
    
    @Override
    public void destroy() {
        try {
            if (chatRoom != null) {
                chatRoom.close();
            }
        } catch (Exception e) {
            // 로그 처리
        }
    }
}
```

#### 현재 화면 정보 수집 Servlet

```java
@WebServlet("/suapi/collect")
public class UICollectorServlet extends HttpServlet {
    
    private Map<String, ResponsesChatRoom> sessionChatRooms = new ConcurrentHashMap<>();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // 세션 ID 가져오기
        String sessionId = req.getSession().getId();
        
        // 세션별 ChatRoom 가져오기 또는 생성
        ResponsesChatRoom chatRoom = sessionChatRooms.get(sessionId);
        if (chatRoom == null) {
            String apiKey = getServletContext().getInitParameter("openai.api.key");
            String model = getServletContext().getInitParameter("openai.model");
            chatRoom = new ResponsesChatRoom(apiKey, model);
            chatRoom.setActionQueueHandler(new ActionQueueHandler());
            sessionChatRooms.put(sessionId, chatRoom);
        }
        
        // 요청 본문에서 UI 정보 읽기
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        String viewInfo = sb.toString();
        
        // Action Queue Handler에 현재 화면 정보 설정
        ActionQueueHandler aqHandler = chatRoom.getActionQueueHandler();
        if (aqHandler != null) {
            try {
                aqHandler.setCurrentViewInfo(viewInfo);
            } catch (ParseException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid JSON format\"}");
                return;
            }
        }
        
        // 응답
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"status\":\"ok\"}");
    }
}
```

### JavaScript 클라이언트 사용 예제

```javascript
// UI 정보 수집 및 전송 (smart-ux-collector.js가 자동으로 처리)

// 프롬프트 전송 및 Action Queue 실행
async function sendPrompt(userInput) {
    try {
        // 프롬프트 전송
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: new URLSearchParams({
                prompt: userInput,
                viewInfo: JSON.stringify(window.uiSnapshot || [])
            })
        });
        
        const result = await response.json();
        
        // AI 응답 메시지 표시
        if (result.message) {
            displayMessage(result.message);
        }
        
        // Action Queue 실행
        if (result.action_queue) {
            const actions = Array.isArray(result.action_queue) 
                ? result.action_queue 
                : JSON.parse(result.action_queue);
            await doActions(actions);
        }
        
    } catch (error) {
        console.error('Error:', error);
    }
}

// 메시지 표시 함수
function displayMessage(message) {
    const messageDiv = document.getElementById('chat-messages');
    messageDiv.innerHTML += '<div>' + message + '</div>';
}

// 사용자 입력 처리
document.getElementById('send-btn').addEventListener('click', () => {
    const userInput = document.getElementById('user-input').value;
    sendPrompt(userInput);
});
```

### Gemini API 사용 예제

```java
@WebServlet("/api/chat/gemini")
public class GeminiChatServlet extends HttpServlet {
    
    private GeminiChatRoom chatRoom;
    private ActionQueueHandler aqHandler;
    
    @Override
    public void init() throws ServletException {
        String apiKey = getServletContext().getInitParameter("gemini.api.key");
        String model = getServletContext().getInitParameter("gemini.model");
        
        chatRoom = new GeminiChatRoom(apiKey, model);
        aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // Responses API와 동일한 방식으로 사용
        // ...
    }
}
```

### Assistants API 사용 예제

```java
@WebServlet("/api/chat/assistant")
public class AssistantChatServlet extends HttpServlet {
    
    private Map<String, AssistantsThread> sessionThreads = new ConcurrentHashMap<>();
    
    @Override
    public void init() throws ServletException {
        // Assistant 설정은 세션별로 Thread를 생성할 때 사용
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String sessionId = req.getSession().getId();
        
        // 세션별 Thread 가져오기 또는 생성
        AssistantsThread thread = sessionThreads.get(sessionId);
        if (thread == null) {
            Assistants assistant = new Assistants("asst_xxxxxxxxxxxxx");
            assistant.setApiKey(getServletContext().getInitParameter("openai.api.key"));
            thread = new AssistantsThread(assistant);
            thread.setActionQueueHandler(new ActionQueueHandler());
            sessionThreads.put(sessionId, thread);
        }
        
        String userPrompt = req.getParameter("prompt");
        String currentViewInfo = req.getParameter("viewInfo");
        
        // 현재 화면 정보 설정
        if (currentViewInfo != null) {
            ActionQueueHandler aqHandler = thread.getActionQueueHandler();
            if (aqHandler != null) {
                aqHandler.setCurrentViewInfo(currentViewInfo);
            }
        }
        
        // 메시지 전송
        Chatting chatting = thread.getChatting();
        JSONObject response = chatting.sendPrompt(userPrompt);
        
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(response.toJSONString());
    }
}
```

---

## 주의사항

### API Key 보안

- API Key는 절대 클라이언트 측에 노출하지 마세요.
- Servlet 초기화 파라미터나 환경 변수를 통해 관리하세요.
- 프로덕션 환경에서는 보안 설정을 강화하세요.

### 대화 세션 관리

- `ChatRoom`은 세션별로 생성하여 관리하는 것이 좋습니다.
- 세션 종료 시 `close()` 메서드를 호출하여 리소스를 정리하세요.
- Assistants API의 경우 Thread 삭제가 자동으로 수행됩니다.

### Action Queue 처리

- `ActionQueueHandler`는 `setCurrentViewInfo()`가 호출된 후에만 Action Queue를 생성할 수 있습니다.
- JavaScript 클라이언트가 정상적으로 동작하지 않으면 Action Queue가 생성되지 않을 수 있습니다.

### 에러 처리

- 모든 API 메서드는 예외를 발생시킬 수 있으므로 적절한 예외 처리가 필요합니다.
- 네트워크 오류, API Key 오류 등을 고려하여 재시도 로직을 구현하는 것을 권장합니다.

---

## 버전 정보

- **현재 버전**: 0.6.0
- **Java 버전**: 17 이상
- **라이선스**: Apache License 2.0

---

## 문의 및 지원

- 이메일: [kiunsea@gmail.com](mailto:kiunsea@gmail.com)
- 웹사이트: [https://www.omnibuscode.com](https://www.omnibuscode.com)
- 홈페이지: [https://jiniebox.com](https://jiniebox.com)

---

**Copyright © 2025 jiniebox.com**


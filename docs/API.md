# API Reference

Smart UX API의 상세한 API 레퍼런스 문서입니다.

---

## 📋 목차

- [Core Interfaces](#core-interfaces)
- [OpenAI Responses API](#openai-responses-api)
- [Google Gemini API](#google-gemini-api)
- [OpenAI Assistants API](#openai-assistants-api)
- [Utility Classes](#utility-classes)
- [JavaScript Client API](#javascript-client-api)

---

## Core Interfaces

### ChatRoom

대화 세션을 관리하는 기본 인터페이스입니다.

#### 구현체
- `ResponsesChatRoom`: OpenAI Responses API
- `GeminiChatRoom`: Google Gemini API

#### 메서드

##### `void addSystemMessage(String message)`
시스템 메시지(주로 UIF 문서)를 추가합니다.

**파라미터:**
- `message` - 시스템 메시지 내용

**예제:**
```java
chatRoom.addSystemMessage(uifDocument);
```

##### `Chatting createChatting()`
새로운 Chatting 인스턴스를 생성합니다.

**반환값:** `Chatting` 인스턴스

**예제:**
```java
Chatting chatting = chatRoom.createChatting();
```

##### `List<Message> getChatHistory()`
전체 대화 이력을 조회합니다.

**반환값:** 메시지 리스트

---

### Chatting

개별 대화를 처리하는 인터페이스입니다.

#### 구현체
- `ResponsesChatting`: OpenAI Responses API
- `GeminiChatting`: Google Gemini API

#### 메서드

##### `String sendMessage(String prompt, String viewInfo)`
프롬프트와 현재 화면 정보를 AI에 전송하고 Action Queue를 수신합니다.

**파라미터:**
- `prompt` - 사용자 프롬프트
- `viewInfo` - 현재 화면 UI 정보 (JSON 문자열)

**반환값:** Action Queue JSON 문자열

**예외:**
- `IOException` - 네트워크 오류
- `APIException` - API 호출 실패

**예제:**
```java
String prompt = "아메리카노 주문해줘";
String viewInfo = getCurrentViewInfo();
String actionQueue = chatting.sendMessage(prompt, viewInfo);
```

##### `List<Message> getConversationHistory()`
현재 대화의 이력을 조회합니다.

**반환값:** 메시지 리스트

---

## OpenAI Responses API

### ResponsesChatRoom

**생성자:**
```java
public ResponsesChatRoom(String apiKey, String model)
```

**파라미터:**
- `apiKey` - OpenAI API Key
- `model` - 사용할 모델 (예: "gpt-4", "gpt-3.5-turbo")

**예제:**
```java
ResponsesChatRoom chatRoom = new ResponsesChatRoom(
    "sk-...", 
    "gpt-4"
);
```

### ResponsesChatting

`ResponsesChatRoom.createChatting()`을 통해 생성됩니다.

#### 주요 메서드

##### `String sendMessage(String prompt, String viewInfo)`
OpenAI API를 호출하여 응답을 받습니다.

**API 엔드포인트:** `https://api.openai.com/v1/chat/completions`

**요청 형식:**
```json
{
  "model": "gpt-4",
  "messages": [
    {"role": "system", "content": "UIF 문서..."},
    {"role": "user", "content": "프롬프트 + 화면 정보"}
  ]
}
```

**응답 형식:**
```json
{
  "actions": [
    {
      "elementId": "menu_americano",
      "action": "click"
    }
  ]
}
```

---

## Google Gemini API

### GeminiChatRoom

**생성자:**
```java
public GeminiChatRoom(String apiKey, String model)
```

**파라미터:**
- `apiKey` - Google Gemini API Key
- `model` - 사용할 모델 (예: "gemini-pro", "gemini-1.5-pro")

**예제:**
```java
GeminiChatRoom chatRoom = new GeminiChatRoom(
    "AIza...", 
    "gemini-pro"
);
```

### GeminiChatting

`GeminiChatRoom.createChatting()`을 통해 생성됩니다.

#### 주요 메서드

##### `String sendMessage(String prompt, String viewInfo)`
Gemini API를 호출하여 응답을 받습니다.

**API 엔드포인트:** `https://generativelanguage.googleapis.com/v1/models/{model}:generateContent`

**요청 형식:**
```json
{
  "contents": [
    {
      "role": "user",
      "parts": [{"text": "프롬프트 + UIF + 화면 정보"}]
    }
  ]
}
```

---

## OpenAI Assistants API

### Assistant

OpenAI Assistants API를 사용하기 위한 클래스입니다.

**생성자:**
```java
public Assistant(String apiKey, String assistantId)
```

**파라미터:**
- `apiKey` - OpenAI API Key
- `assistantId` - Assistant ID (예: "asst_...")

**예제:**
```java
Assistant assistant = new Assistant(
    "sk-...", 
    "asst_abc123"
);
```

#### 메서드

##### `AssistantsThread createThread()`
새로운 Thread를 생성합니다.

**반환값:** `AssistantsThread` 인스턴스

**예제:**
```java
AssistantsThread thread = assistant.createThread();
```

##### `AssistantsThread retrieveThread(String threadId)`
기존 Thread를 조회합니다.

**파라미터:**
- `threadId` - Thread ID

**반환값:** `AssistantsThread` 인스턴스

---

### AssistantsThread

Thread는 Assistants API의 대화 세션을 나타냅니다.

#### 메서드

##### `AssistantsMessage createMessage()`
새로운 메시지를 생성합니다.

**반환값:** `AssistantsMessage` 인스턴스

##### `String getThreadId()`
Thread ID를 반환합니다.

**반환값:** Thread ID 문자열

---

### AssistantsMessage

메시지를 전송하고 응답을 받는 클래스입니다.

#### 메서드

##### `String sendMessage(String prompt, String viewInfo)`
Assistant에게 메시지를 전송하고 Run을 실행합니다.

**파라미터:**
- `prompt` - 사용자 프롬프트
- `viewInfo` - 현재 화면 정보

**반환값:** Action Queue JSON 문자열

**예제:**
```java
AssistantsMessage message = thread.createMessage();
String actionQueue = message.sendMessage(prompt, viewInfo);
```

---

## Utility Classes

### ActionQueueHandler

Action Queue를 파싱하고 실행하는 유틸리티 클래스입니다.

#### 메서드

##### `ActionQueue parse(String jsonString)`
JSON 문자열을 ActionQueue 객체로 파싱합니다.

**파라미터:**
- `jsonString` - Action Queue JSON

**반환값:** `ActionQueue` 객체

**예제:**
```java
ActionQueueHandler handler = new ActionQueueHandler();
ActionQueue queue = handler.parse(actionQueueJson);
```

##### `void execute(ActionQueue queue)`
Action Queue의 액션들을 순차적으로 실행합니다.

**파라미터:**
- `queue` - 실행할 ActionQueue

---

### JsonExtractor

JSON 데이터를 추출하는 유틸리티 클래스입니다.

#### 메서드

##### `String extractActionQueue(String response)`
AI 응답에서 Action Queue 부분만 추출합니다.

**파라미터:**
- `response` - AI 전체 응답

**반환값:** Action Queue JSON 문자열

---

## JavaScript Client API

### SmartUXCollector

웹 페이지의 UI 정보를 수집하는 JavaScript 클래스입니다.

#### 메서드

##### `collectUIInfo()`
현재 페이지의 모든 UI 요소를 스캔하여 JSON으로 반환합니다.

**반환값:** UI 정보 JSON 객체

**예제:**
```javascript
const collector = new SmartUXCollector();
const viewInfo = collector.collectUIInfo();
console.log(JSON.stringify(viewInfo));
```

**반환 형식:**
```json
{
  "elements": [
    {
      "id": "menu_americano",
      "tagName": "BUTTON",
      "type": "button",
      "text": "아메리카노",
      "className": "menu-item",
      "visible": true,
      "enabled": true
    }
  ]
}
```

---

### SmartUXClient

Action Queue를 실행하는 JavaScript 클래스입니다.

#### 메서드

##### `executeActionQueue(actionQueue)`
Action Queue의 액션들을 순차적으로 실행합니다.

**파라미터:**
- `actionQueue` - Action Queue JSON 객체 또는 문자열

**예제:**
```javascript
const client = new SmartUXClient();
client.executeActionQueue(actionQueue);
```

##### `executeAction(action)`
단일 액션을 실행합니다.

**파라미터:**
- `action` - 액션 객체

**예제:**
```javascript
client.executeAction({
    elementId: "menu_americano",
    action: "click"
});
```

#### 지원 액션 타입

- **click**: 요소 클릭
- **setValue**: 입력 필드에 값 설정
- **select**: 드롭다운 선택
- **scroll**: 스크롤
- **wait**: 대기

---

## 에러 처리

### 예외 클래스

#### APIException
API 호출 실패 시 발생하는 예외입니다.

**메서드:**
- `getStatusCode()` - HTTP 상태 코드
- `getErrorMessage()` - 에러 메시지
- `getErrorType()` - 에러 타입

#### NetworkException
네트워크 오류 시 발생하는 예외입니다.

### 예외 처리 예제

```java
try {
    String actionQueue = chatting.sendMessage(prompt, viewInfo);
} catch (APIException e) {
    System.err.println("API Error: " + e.getStatusCode());
    System.err.println("Message: " + e.getErrorMessage());
} catch (NetworkException e) {
    System.err.println("Network Error: " + e.getMessage());
}
```

---

## 설정 옵션

### ResponsesChatRoom 고급 옵션

```java
ResponsesChatRoom chatRoom = new ResponsesChatRoom.Builder()
    .apiKey("sk-...")
    .model("gpt-4")
    .temperature(0.7)          // 응답의 창의성 (0.0 ~ 2.0)
    .maxTokens(2000)           // 최대 토큰 수
    .timeout(30000)            // 타임아웃 (밀리초)
    .retryCount(3)             // 재시도 횟수
    .build();
```

### GeminiChatRoom 고급 옵션

```java
GeminiChatRoom chatRoom = new GeminiChatRoom.Builder()
    .apiKey("AIza...")
    .model("gemini-pro")
    .temperature(0.5)
    .topP(0.9)
    .topK(40)
    .build();
```

---

## 버전 호환성

| Smart UX API | Java | OpenAI API | Gemini API |
|--------------|------|------------|------------|
| 0.6.x        | 17+  | v1         | v1         |
| 0.5.x        | 17+  | v1         | v1         |
| 0.4.x        | 17+  | v1         | N/A        |
| 0.3.x        | 17+  | v1 (Beta)  | v1 (Beta)  |

---

## 추가 리소스

- [설치 가이드](INSTALL.md)
- [코드 예제](EXAMPLES.md)
- [문제 해결](TROUBLESHOOTING.md)
- [JavaDoc](javadoc/) (빌드 후 생성)

---

## 질문 및 지원

- [GitHub Issues](https://github.com/kiunsea/smux-api/issues)
- [Discussions](https://github.com/kiunsea/smux-api/discussions)
- Email: kiunsea@gmail.com


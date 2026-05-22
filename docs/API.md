# API Reference

Smart UX API의 상세한 API 레퍼런스 문서입니다.

---

## 📋 목차

- [Core Interfaces](#core-interfaces)
- [OpenAI Responses API](#openai-responses-api)
- [Google Gemini API](#google-gemini-api)
- [Anthropic Claude API](#anthropic-claude-api)
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
- `ClaudeChatRoom`: Anthropic Claude API
- `AssistantsThread`: OpenAI Assistants API

#### 메서드

##### `Chatting getChatting()`
Chatting 인스턴스를 반환합니다. 첫 호출 시 생성되며, 이후 호출 시 동일한 인스턴스를 반환합니다.

**반환값:** `Chatting` 인스턴스

**예제:**
```java
Chatting chatting = chatRoom.getChatting();
```

**참고:** 문서에서 언급된 `addSystemMessage()` 및 `createChatting()` 메서드는 현재 버전(0.6.0)에서는 지원되지 않습니다. 대신 `getChatting()` 메서드를 사용하여 Chatting 인스턴스를 얻고, `sendPrompt()` 메서드를 통해 사용자 메시지를 전송합니다.

---

### Chatting

개별 대화를 처리하는 인터페이스입니다.

#### 구현체
- `ResponsesChatting`: OpenAI Responses API
- `GeminiChatting`: Google Gemini API
- `ClaudeChatting`: Anthropic Claude API
- `AssistantsMessage`: OpenAI Assistants API

#### 메서드

##### `JSONObject sendPrompt(String userMsg)`
사용자 메시지를 전송하고 AI 응답을 받습니다.

**파라미터:**
- `userMsg` - 사용자 메시지 (프롬프트)

**반환값:** `JSONObject` - 다음 형식의 JSON 객체:
```json
{
  "message": "AI 응답 메시지",
  "action_queue": "Action Queue JSON 문자열 또는 JSON 객체"
}
```

**예외:**
- `Exception` - 네트워크 오류, API 호출 실패 등

**예제:**
```java
String prompt = "아메리카노 주문해줘";
JSONObject response = chatting.sendPrompt(prompt);
String message = (String) response.get("message");
Object actionQueue = response.get("action_queue");
```

**참고:** 
- 현재 화면 정보는 `ActionQueueHandler.setCurrentViewInfo()`를 통해 별도로 설정해야 합니다.
- 화면 정보가 설정되어 있으면 자동으로 프롬프트에 포함됩니다.

##### `Set<String> getMessageIdSet()`
보유하고 있는 메시지 ID Set을 반환합니다.

**반환값:** 메시지 ID Set (Responses API와 Gemini API의 경우 `null` 반환)

**참고:** 이 메서드는 주로 Assistants API에서 사용됩니다.

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

`ResponsesChatRoom.getChatting()`을 통해 생성됩니다.

#### 주요 메서드

##### `JSONObject sendPrompt(String userMsg)`
OpenAI Responses API를 호출하여 응답을 받습니다.

**API 엔드포인트:** `https://api.openai.com/v1/chat/completions`

**동작 방식:**
1. `ActionQueueHandler`가 설정되어 있고 현재 화면 정보가 있으면, 화면 정보와 사용자 메시지를 결합한 프롬프트를 생성합니다.
2. 대화 이력(ConversationHistory)에 사용자 메시지를 추가합니다.
3. OpenAI API를 호출하여 응답을 받습니다.
4. 응답을 대화 이력에 추가합니다.
5. `ActionQueueHandler`를 통해 Action Queue를 추출합니다.

**반환 형식:**
```json
{
  "message": "AI 응답 메시지",
  "action_queue": {
    "action_queue": [
      {
        "elementId": "menu_americano",
        "action": "click"
      }
    ]
  }
}
```

**참고:** 
- `getMessageIdSet()` 메서드는 `null`을 반환합니다 (Gemini API와 동일).

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

`GeminiChatRoom.getChatting()`을 통해 생성됩니다.

#### 주요 메서드

##### `JSONObject sendPrompt(String userMsg)`
Gemini API를 호출하여 응답을 받습니다.

**API 엔드포인트:** `https://generativelanguage.googleapis.com/v1/models/{model}:generateContent`

**동작 방식:**
1. `ActionQueueHandler`가 설정되어 있고 현재 화면 정보가 있으면, 화면 정보와 사용자 메시지를 결합한 프롬프트를 생성합니다.
2. 대화 이력(ConversationHistory)에 사용자 메시지를 추가합니다.
3. Gemini API를 호출하여 응답을 받습니다.
4. 응답을 대화 이력에 추가합니다.
5. `ActionQueueHandler`를 통해 Action Queue를 추출합니다.

**반환 형식:**
```json
{
  "message": "AI 응답 메시지",
  "action_queue": {
    "action_queue": [...]
  }
}
```

**참고:** 
- `getMessageIdSet()` 메서드는 `null`을 반환합니다.

---

## Anthropic Claude API

> v0.10.0 추가. Anthropic Messages API 기반 provider 입니다.

### ClaudeChatRoom

**생성자:**
```java
public ClaudeChatRoom(String apiKey, String model)
public ClaudeChatRoom(String apiKey, String model, String anthropicVersion)
```

**파라미터:**
- `apiKey` - Anthropic Claude API Key (`sk-ant-...`)
- `model` - 사용할 모델 (예: `"claude-sonnet-4-6"`, `"claude-opus-4-7"`, `"claude-haiku-4-5-20251001"`)
- `anthropicVersion` - (선택) `anthropic-version` 헤더 값. 생략 시 기본값 `2023-06-01`

**예제:**
```java
ClaudeChatRoom chatRoom = new ClaudeChatRoom(
    "sk-ant-...",
    "claude-sonnet-4-6"
);
```

### ClaudeChatting

`ClaudeChatRoom.getChatting()`을 통해 생성됩니다.

#### 주요 메서드

##### `JSONObject sendPrompt(String userMsg)`
Anthropic Messages API를 호출하여 응답을 받습니다.

**API 엔드포인트:** `https://api.anthropic.com/v1/messages`

**동작 방식:**
1. `ActionQueueHandler`가 설정되어 있고 현재 화면 정보가 있으면, 화면 정보와 사용자 메시지를 결합한 프롬프트를 생성합니다.
2. 대화 이력(ConversationHistory)에 사용자 메시지를 추가합니다.
3. Claude API를 호출하여 응답을 받습니다.
4. 응답을 대화 이력에 추가합니다.
5. `ActionQueueHandler`를 통해 Action Queue를 추출합니다.

**반환 형식:**
```json
{
  "message": "AI 응답 메시지",
  "action_queue": {
    "action_queue": [...]
  }
}
```

**참고:**
- `getMessageIdSet()` 메서드는 `null`을 반환합니다 (OpenAI Responses / Gemini API와 동일).
- `sendPromptWithTools()` (Tool Use), `sendPromptWithSchema()` (구조화 응답), `markAsCacheable()` (프롬프트 캐싱)를 OpenAI / Gemini provider 와 동일하게 지원합니다.
- 구조화 응답은 Anthropic 이 네이티브 JSON Schema 응답 필드를 제공하지 않으므로, 스키마를 단일 tool 의 `input_schema` 로 등록하고 `tool_choice` 로 호출을 강제하는 방식으로 구현됩니다.
- `max_tokens` 는 기본값 `4096` 으로 요청됩니다.

---

## OpenAI Assistants API

### Assistants

OpenAI Assistants API의 Assistant 정보를 관리하는 클래스입니다.

**생성자:**
```java
public Assistants(String assistantId)
```

**파라미터:**
- `assistantId` - Assistant ID (예: "asst_...")

**메서드:**
- `String getAssistantId()` - Assistant ID 반환
- `String getApiKey()` - API Key 반환
- `void setApiKey(String apiKey)` - API Key 설정

**예제:**
```java
Assistants assistant = new Assistants("asst_abc123");
assistant.setApiKey("sk-...");
```

**참고:** 
- `createThread()` 및 `retrieveThread()` 메서드는 `AssistantsThread` 클래스에서 제공됩니다.

---

### AssistantsThread

Thread는 Assistants API의 대화 세션을 나타내는 `ChatRoom` 구현체입니다.

**생성자:**
```java
public AssistantsThread(Assistants assistInfo) throws ParseException
```

**파라미터:**
- `assistInfo` - `Assistants` 인스턴스

**주요 메서드:**
- `String getId()` - Thread ID 반환
- `Chatting getChatting()` - Chatting 인스턴스 반환 (AssistantsMessage)
- `boolean close()` - Thread 삭제 및 리소스 정리
- `void setFunctionMap(Map usrFuncs)` - 사용자 함수 맵 설정 (TODO: 추후 구현 예정)

**예제:**
```java
Assistants assistant = new Assistants("asst_abc123");
assistant.setApiKey("sk-...");
AssistantsThread thread = new AssistantsThread(assistant);
Chatting chatting = thread.getChatting();
```

---

### AssistantsMessage

메시지를 전송하고 응답을 받는 `Chatting` 구현체입니다.

**생성자:**
```java
public AssistantsMessage(AssistantsAPIConnection connApi, String idThread)
public AssistantsMessage(Chatting chatting, AssistantsAPIConnection connApi, String idThread)
```

**주요 메서드:**
- `JSONObject sendPrompt(String userMsg)` - Assistant에게 메시지를 전송하고 Run을 실행합니다.

**동작 방식:**
1. Thread에 메시지를 추가합니다.
2. Run을 생성하고 실행합니다.
3. Run이 완료될 때까지 폴링 방식으로 대기합니다.
4. 완료된 Run의 응답을 반환합니다.

**반환 형식:**
```json
{
  "message": "AI 응답 메시지",
  "action_queue": {...}
}
```

**예제:**
```java
AssistantsThread thread = new AssistantsThread(assistant);
Chatting chatting = thread.getChatting();
JSONObject response = chatting.sendPrompt("아메리카노 주문해줘");
```

**참고:** 
- Function Call 기능은 현재 TODO 상태입니다 (추후 구현 예정).

---

## Utility Classes

### ActionQueueHandler

Action Queue를 파싱하고 실행하는 유틸리티 클래스입니다.

#### 메서드

##### `void setCurrentViewInfo(String curViewInfo)`
현재 화면 정보를 저장합니다.

**파라미터:**
- `curViewInfo` - 현재 화면 정보 JSON 문자열 (배열 또는 객체)

**예외:** `ParseException`

**예제:**
```java
ActionQueueHandler handler = new ActionQueueHandler();
String viewInfo = getCurrentViewInfoFromClient();
handler.setCurrentViewInfo(viewInfo);
```

#### `void addCurrentViewInfo(JsonNode additionalViewInfo)`
현재 화면 정보에 추가 정보를 병합합니다. (버전 0.6.0 추가)

**파라미터:**
- `additionalViewInfo` - 추가할 화면 정보 (JsonNode)

#### `boolean isCurrentViewInfo()`
현재 화면 정보가 저장되어 있는지 확인합니다.

**반환값:** `boolean` - 화면 정보 저장 여부

#### `String getCurViewPrompt()`
현재 화면 정보 설정에 대한 프롬프트를 반환합니다. 화면 정보가 변경되었을 때만 프롬프트를 반환합니다.

**반환값:** `String` - Current View Prompt (변경되지 않았으면 null)

#### `String getActionQueuePrompt(String userMsg)`
Action Queue 생성 요청 프롬프트를 반환합니다.

**파라미터:**
- `userMsg` - 사용자 메시지

**반환값:** `String` - Action Queue Prompt

**주의:** 현재 화면 정보가 필수로 저장되어 있어야 합니다.

#### `JsonNode getActionQueue(String resMsg)`
AI 응답 메시지에서 Action Queue를 추출합니다.

**파라미터:**
- `resMsg` - AI 응답 메시지

**반환값:** `JsonNode` - Action Queue JSON (또는 전체 응답 JSON)

#### `void clearCurrentViewInfo()`
저장된 현재 화면 정보를 삭제합니다.

#### `void markViewInfoAsSent()`
프롬프트 전송 후 호출하여 마지막 전송된 화면 정보를 업데이트합니다. (버전 0.6.0 추가)

#### `boolean isViewInfoChanged()`
화면 정보 변경 여부를 확인합니다. (버전 0.6.0 추가)

**반환값:** `boolean` - 변경 여부

---

### ConfigLoader

클래스패스에서 JSON 설정 파일을 로드하는 유틸리티 클래스입니다.

#### 메서드

##### `JsonNode loadConfigFromClasspath()`
기본 파일명 (`config.json`)으로 설정 파일을 로드합니다.

**반환값:** `JsonNode` - 로드된 설정 파일의 JsonNode

##### `JsonNode loadConfigFromClasspath(String confFileName)`
지정한 파일명으로 설정 파일을 로드합니다.

**파라미터:**
- `confFileName` - 설정 파일명 (null인 경우 `config.json` 사용)

**반환값:** `JsonNode` - 로드된 설정 파일의 JsonNode (파일을 찾지 못하거나 파싱 오류 시 `null`)

**예제:**
```java
JsonNode config = ConfigLoader.loadConfigFromClasspath();
JsonNode customConfig = ConfigLoader.loadConfigFromClasspath("custom-config.json");
```

---

## JavaScript Client API

### smart-ux-collector.js

웹 페이지의 UI 정보를 자동으로 수집하고 서버로 전송하는 클라이언트 스크립트입니다.

#### 주요 특징

- **자동 실행**: 스크립트가 로드되면 자동으로 UI 정보를 수집하고 전송합니다
- **DOM 변경 감지**: MutationObserver를 사용하여 DOM 변경 시 자동으로 재수집합니다
- **전역 변수**: 수집된 UI 정보는 `window.uiSnapshot`에 저장됩니다

#### 동작 방식

1. 페이지 로드 시 2초 후 자동으로 이벤트 바인딩된 요소 수집
2. DOM 변경 감지 시 2초 대기 후 자동으로 재수집
3. 수집된 정보를 `window.uiSnapshot`에 저장
4. `SERVER_ENDPOINT`로 자동 전송 (기본값: `/suapi/collect`)

#### 설정

스크립트 내부의 `SERVER_ENDPOINT` 변수를 수정하여 서버 엔드포인트를 설정합니다:

```javascript
const SERVER_ENDPOINT = '/suapi/collect';  // 실제 서버 URL로 교체
```

#### 수집되는 정보 형식

`window.uiSnapshot`에 저장되는 정보 형식:

```json
[
  {
    "id": "menu_americano",
    "type": "click",
    "label": "아메리카노",
    "selector": "#menu_americano",
    "xpath": "//*[@id='menu_americano']",
    "properties": {
      "enabled": true,
      "visible": true
    }
  }
]
```

#### 사용 예제

```html
<!-- HTML에 스크립트 포함 -->
<script src="/lib/smart-ux-collector.js"></script>

<!-- JavaScript에서 수집된 정보 사용 -->
<script>
  // 자동으로 window.uiSnapshot에 저장됨
  console.log(window.uiSnapshot);
  
  // 서버로 전송할 때 사용
  const viewInfo = JSON.stringify(window.uiSnapshot);
</script>
```

---

### smart-ux-client.js

Action Queue를 실행하는 클라이언트 스크립트입니다.

#### 주요 함수

##### `doActions(actions)`

Action Queue의 액션들을 순차적으로 실행합니다.

**파라미터:**
- `actions` - Action 배열 (배열 형식)

**반환값:** `Promise` - 비동기 함수입니다

**예제:**
```javascript
import { doActions } from '/lib/smart-ux-client.js';

// 또는 스크립트 태그로 로드한 경우
// <script type="module" src="/lib/smart-ux-client.js"></script>

const actions = [
  { type: "click", id: "menu_americano" },
  { type: "setAttribute", id: "order_count", attrName: "value", attrValue: "2" },
  { type: "click", id: "order_btn" }
];

await doActions(actions);
```

**참고:**
- `action.type` 또는 `action.action` 필드를 지원합니다 (하위 호환성)
- ES6 모듈로 export되어 있으므로 `type="module"`로 로드하거나 import 문을 사용해야 합니다

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

페이지를 이동합니다. 남은 액션은 localStorage에 저장되어 다음 페이지에서 자동 실행됩니다.

```javascript
{
  "type": "navigate",
  "url": "/next-page.html"
}
```

#### 페이지 이동 시 액션 유지

`navigate` 액션 실행 시, 남은 액션은 `localStorage.pendingActions`에 저장되어 다음 페이지 로드 시 자동으로 실행됩니다.

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

### 고급 옵션

현재 버전(0.6.0)에서는 Builder 패턴을 통한 고급 옵션 설정을 지원하지 않습니다. 기본 생성자를 사용하여 ChatRoom을 생성합니다:

```java
// OpenAI Responses API
ResponsesChatRoom chatRoom = new ResponsesChatRoom(apiKey, model);

// Google Gemini API
GeminiChatRoom chatRoom = new GeminiChatRoom(apiKey, model);
```

향후 버전에서 고급 옵션(온도, 최대 토큰, 타임아웃 등) 지원이 추가될 예정입니다.

---

## 버전 호환성

| Smart UX API | Java | OpenAI API | Gemini API | Anthropic API |
|--------------|------|------------|------------|---------------|
| 0.10.x       | 17+  | v1         | v1         | `2023-06-01`  |
| 0.6.x        | 17+  | v1         | v1         | N/A           |
| 0.5.x        | 17+  | v1         | v1         | N/A           |
| 0.4.x        | 17+  | v1         | N/A        | N/A           |
| 0.3.x        | 17+  | v1 (Beta)  | v1 (Beta)  | N/A           |

---

## 추가 리소스

- [설치 가이드](INSTALL.md)
- [코드 예제](EXAMPLES.md)
- [문제 해결](TROUBLESHOOTING.md)
- [JavaDoc](javadoc/) (빌드 후 생성)

---

## 질문 및 지원

- [GitHub Issues](https://github.com/kiunsea/smart-ux-api/issues)
- [Discussions](https://github.com/kiunsea/smart-ux-api/discussions)
- Email: kiunsea@gmail.com


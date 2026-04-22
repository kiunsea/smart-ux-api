# Smart UX API - 메인 라이브러리

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

> 📍 **현재 위치**: 이 파일은 `smart-ux-api/lib/` 디렉터리의 상위 디렉터리(`smart-ux-api/`)에 있습니다.  
> 💡 **프로젝트 구조**: GitHub 저장소 이름이 `smart-ux-api`이고, 메인 라이브러리 프로젝트 폴더도 `smart-ux-api/`입니다.  
> 🎯 **실제 라이브러리 소스**: `lib/` 디렉터리에 있습니다.

---

## 📁 이 디렉터리 구조

```
smart-ux-api/              ← 현재 위치 (메인 라이브러리 프로젝트 루트)
│
├── lib/                   ← 실제 라이브러리 모듈 (여기서 빌드!)
│   ├── src/main/java/    ← Java 소스 코드
│   ├── src/main/js/      ← JavaScript 클라이언트
│   └── build/libs/       ← 빌드된 JAR 파일
│
├── doc/                   ← 라이브러리 문서
├── bat/                   ← 배포 스크립트
└── README.md             ← 이 파일
```

**빌드를 시작하려면**: `lib/` 디렉터리로 이동하세요.
```bash
cd lib
./gradlew build
```

---

## 🖱️ 설치 방법

**Smart UX API**는 Java 기반 웹 애플리케이션(기존 또는 신규)에 손쉽게 통합할 수 있습니다.

### 1️⃣ JAR 파일 추가

* `smart-ux-api/lib/build/libs/smart-ux-api-0.6.1.jar` 파일을 웹 애플리케이션의 `/WEB-INF/lib/` 디렉터리에 복사합니다.

### 2️⃣ JS 라이브러리 포함

* `smart-ux-api/lib/src/main/js/*.js` 파일을 웹 애플리케이션의 `[DOC ROOT]/smuxapi` 디렉터리에 추가합니다.
* 웹 페이지에 다음 스크립트를 포함시킵니다:

```html
<script src="/smuxapi/smart-ux-client.js"></script>
<script src="/smuxapi/smart-ux-collector.js"></script>
```

---

## 🧊 주요 API 개요

### 1️⃣ ChatRoom

AI 대화 시 기존 대화 내용을 유지하는 **대화 저장소** 역할을 합니다.

### 2️⃣ Chatting

ChatRoom 내에서 진행되는 **하나의 대화 세트**를 의미합니다.
사용자의 프롬프트를 전달하고, AI의 응답을 받을 수 있습니다.

### 3️⃣ Assistant

OpenAI **Assistants API**를 사용하는 경우 필요한 기능을 제공합니다.

---

## 📋 사용 방법

**사용 예제**는 GitHub 저장소의 `smuxapi-demo/` 디렉터리(저장소 루트의 샘플 프로젝트)를 참고하세요.

> 💡 **경로 안내**: 저장소를 클론한 경우, `../smuxapi-demo/` 경로에 샘플 프로젝트가 있습니다.

### 1️⃣ AI 모델 API 등록

사용하는 AI 모델에 따라 해당 AI서비스 API Key가 필요합니다.

* OpenAI Responses / Assistants API
  - OpenAI API Key 발급 필요
    👉 [API Key 발급 링크](https://platform.openai.com/settings/organization/api-keys)
  - Assistants API를 사용할 경우 **Assistant ID**도 필요합니다.
    👉 [Assistants 문서](https://platform.openai.com/docs/assistants)

* Google Gemini API
  - [Google Cloud Console](https://console.cloud.google.com)에서 API Key를 생성하세요.

### 1️⃣-1 설정 파일 구성

라이브러리는 두 가지 설정 파일을 사용합니다:

#### config.json (선택)

디버그 모드 및 프롬프트 설정을 관리합니다.

```json
{
  "debug-mode": false,
  "debug-output-path": "./conversation_log/",
  "debug-file-prefix": "chatroom",
  "prompt": { ... }
}
```

| 설정 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| `debug-mode` | boolean | `false` | 디버그 모드 활성화 여부. `true`로 설정 시 AI 대화 내용이 JSON 파일로 저장됩니다 |
| `debug-output-path` | string | `"./conversation_log/"` | 디버그 로그 파일 저장 경로. 상대 경로 또는 절대 경로 지정 가능. JAR 실행 시 JAR 파일과 같은 디렉터리 기준 |
| `debug-file-prefix` | string | `"chatroom"` | 디버그 로그 파일명 접두사. 저장되는 파일명 형식: `{prefix}-{timestamp}-{sessionId}.json` |
| `prompt` | object | - | Action Queue 생성을 위한 프롬프트 템플릿 설정 |
| `prompt.cur_view_info` | array | - | 현재 화면 정보 프롬프트 템플릿. `${CurViewInfo}` 변수는 실제 화면 정보로 대체됩니다 |
| `prompt.action_queue` | array | - | Action Queue 생성 프롬프트 템플릿. `${UserMsg}` 변수는 사용자 메시지로 대체됩니다 |

#### apikey.json (선택)

API 키를 별도 파일로 관리할 수 있습니다. `def.apikey.json`을 복사하여 사용하세요.

```json
{
    "GEMINI_API_KEY": "your-api-key",
    "GEMINI_MODEL": "gemini-pro",
    "OPENAI_API_KEY": "your-api-key",
    "OPENAI_ASSIST_ID": "asst_xxxxxxxxxxxxx",
    "OPENAI_MODEL": "gpt-4"
}
```

| 설정 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `GEMINI_API_KEY` | string | Gemini 사용 시 필수 | Google Gemini API 키. [Google Cloud Console](https://console.cloud.google.com)에서 API Key를 생성하여 발급 가능 |
| `GEMINI_MODEL` | string | Gemini 사용 시 필수 | Gemini 모델명. 예: `gemini-1.5-flash`, `gemini-2.5-flash` |
| `OPENAI_API_KEY` | string | OpenAI 사용 시 필수 | OpenAI API 키. [OpenAI Platform](https://platform.openai.com)에서 발급 가능 |
| `OPENAI_ASSIST_ID` | string | Assistants API 사용 시 선택 | OpenAI Assistant ID. OpenAI Platform에서 Assistant를 생성하면 발급되는 ID |
| `OPENAI_MODEL` | string | OpenAI 사용 시 필수 | OpenAI 모델명. 예: `gpt-4o-mini`, `gpt-4`, `gpt-4.1-mini`, `gpt-4.1` |

> ⚠️ **보안 주의**: `apikey.json` 파일은 `.gitignore`에 추가하여 버전 관리에서 제외하세요.

#### 설정 파일 로딩 우선순위

1. **JAR 실행 디렉터리** (배포 환경) - JAR과 같은 폴더의 설정 파일 우선 적용
2. **classpath** (개발 환경) - `src/main/resources/` 내 파일

### 2️⃣ User Interaction Flow 문서 작성

AI가 **UI를 제어할 때 필요한 작업 흐름(Work Flow)** 을 정의한 **JSON 문서**를 작성합니다.
서비스 초기화 시 자동 로딩되어 AI에 전달됩니다.

#### 예시: UIF 문서 구조
```json
{
  "service": "키오스크 주문 시스템",
  "screens": [
    {
      "name": "메인 화면",
      "elements": [
        {
          "id": "menu_americano",
          "type": "button",
          "label": "아메리카노",
          "action": "click"
        },
        {
          "id": "order_count",
          "type": "input",
          "label": "수량",
          "action": "setValue"
        }
      ]
    }
  ],
  "workflows": [
    {
      "name": "메뉴 주문",
      "steps": [
        "1. 메뉴 버튼 클릭",
        "2. 수량 입력",
        "3. 장바구니 추가"
      ]
    }
  ]
}
```

샘플 파일은 `docs/sample.su-api_uif.json`을 참조하세요.

### 3️⃣ 프롬프트 메시지 전송을 위한 기본 인스턴스 생성

* OpenAI Responses / Google Gemini
  - `ResponsesChatRoom`, `ResponsesChatting`
  - `GeminiChatRoom`, `GeminiChatting`
* OpenAI Assistant
  - `Assistants`
  - `AssistantsThread`
  - `AssistantsMessage`

### 4️⃣ 서비스 초기화 및 사용자 요청 처리를 위한 Servlet 요구 사항
다음과 같은 프로세스를 통해 AI로부터 Action Queue를 응답받아 동작하게 됩니다.
* **User Interaction Flow 문서 전송** (사용자 세션 최초 생성 시 1회 실행)
* **현재 화면 정보(CurrentViewInfo) 전송** (UX Info Servlet)
* **사용자 프롬프트 메시지 전송 및 응답 처리** (Action Queue Servlet)

---

## 💻 코드 예제

### OpenAI Responses API 사용 예제

#### ChatRoom 및 Chatting 생성
```java
// API Key 및 모델 설정
String apiKey = "your-openai-api-key";
String model = "gpt-4";

// ChatRoom 생성 (대화 세션 관리)
ResponsesChatRoom chatRoom = new ResponsesChatRoom(apiKey, model);

// Action Queue Handler 설정
ActionQueueHandler aqHandler = new ActionQueueHandler();
chatRoom.setActionQueueHandler(aqHandler);

// 현재 화면 정보 설정 (JavaScript에서 전달받음)
String currentViewInfo = getCurrentViewInfoFromClient();
aqHandler.setCurrentViewInfo(currentViewInfo);

// Chatting 인스턴스 생성
Chatting chatting = chatRoom.getChatting();
```

#### 프롬프트 전송 및 응답 수신
```java
// 사용자 프롬프트
String userPrompt = "아이스 아메리카노 2잔 주문해줘";

// AI에게 프롬프트 전송
JSONObject response = chatting.sendPrompt(userPrompt);

// 응답 확인
String message = (String) response.get("message");
Object actionQueue = response.get("action_queue");
System.out.println("AI 응답: " + message);
System.out.println("Action Queue: " + actionQueue);
```

### Google Gemini API 사용 예제

```java
// API Key 및 모델 설정
String apiKey = "your-gemini-api-key";
String model = "gemini-pro";

// ChatRoom 생성
GeminiChatRoom chatRoom = new GeminiChatRoom(apiKey, model);

// Action Queue Handler 설정
ActionQueueHandler aqHandler = new ActionQueueHandler();
chatRoom.setActionQueueHandler(aqHandler);

// 현재 화면 정보 설정
aqHandler.setCurrentViewInfo(currentViewInfo);

// Chatting 생성 및 프롬프트 전송
Chatting chatting = chatRoom.getChatting();
JSONObject response = chatting.sendPrompt(userPrompt);
Object actionQueue = response.get("action_queue");
```

### OpenAI Assistants API 사용 예제

```java
// API Key 및 Assistant ID 설정
String apiKey = "your-openai-api-key";
String assistantId = "asst_xxxxxxxxxxxxx";

// Assistant 생성
Assistants assistant = new Assistants(assistantId);
assistant.setApiKey(apiKey);

// Thread 생성 (대화 세션)
AssistantsThread thread = new AssistantsThread(assistant);

// Action Queue Handler 설정
ActionQueueHandler aqHandler = new ActionQueueHandler();
thread.setActionQueueHandler(aqHandler);

// 현재 화면 정보 설정
aqHandler.setCurrentViewInfo(currentViewInfo);

// 메시지 전송
Chatting chatting = thread.getChatting();
JSONObject response = chatting.sendPrompt(userPrompt);
Object actionQueue = response.get("action_queue");
```

### Servlet 구현 예제

#### Action Queue 응답 Servlet
```java
@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
    private ResponsesChatRoom chatRoom;
    
    @Override
    public void init() throws ServletException {
        // API 설정 로드
        String apiKey = getServletContext().getInitParameter("openai.api.key");
        String model = getServletContext().getInitParameter("openai.model");
        
        // ChatRoom 초기화
        chatRoom = new ResponsesChatRoom(apiKey, model);
        
        // Action Queue Handler 초기화
        ActionQueueHandler aqHandler = new ActionQueueHandler();
        chatRoom.setActionQueueHandler(aqHandler);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        // 요청 파라미터 추출
        String userPrompt = req.getParameter("prompt");
        String currentViewInfo = req.getParameter("viewInfo");
        
        // 현재 화면 정보 설정
        ActionQueueHandler aqHandler = chatRoom.getActionQueueHandler();
        if (currentViewInfo != null) {
            try {
                aqHandler.setCurrentViewInfo(currentViewInfo);
            } catch (ParseException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Invalid view info format\"}");
                return;
            }
        }
        
        // Chatting 생성 및 메시지 전송
        Chatting chatting = chatRoom.getChatting();
        JSONObject response = chatting.sendPrompt(userPrompt);
        
        // JSON 응답
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(response.toJSONString());
    }
}
```

### JavaScript 클라이언트 사용 예제

```html
<!-- HTML에 스크립트 포함 -->
<!-- smart-ux-collector.js: 자동 실행되어 window.uiSnapshot에 정보 저장 -->
<script src="/js/smart-ux-collector.js"></script>

<!-- smart-ux-client.js: ES6 모듈로 로드 -->
<script type="module">
    import { doActions } from '/js/smart-ux-client.js';
    window.doActions = doActions;  // 전역에서 사용할 수 있도록 저장
</script>

<script>
// smart-ux-collector.js가 자동으로 수집한 정보 사용
// window.uiSnapshot에 이미 저장되어 있음

// 프롬프트 전송
fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        prompt: userInput,
        viewInfo: JSON.stringify(window.uiSnapshot || [])
    })
})
.then(response => response.json())
.then(data => {
    // Action Queue 실행
    const actions = Array.isArray(data.action_queue) 
        ? data.action_queue 
        : JSON.parse(data.action_queue);
    window.doActions(actions);
});
</script>
```

> 💡 **참고**: 
> - `smart-ux-collector.js`는 자동 실행되므로 별도로 호출할 필요가 없습니다
> - `smart-ux-client.js`는 ES6 모듈이므로 `import` 문 또는 `<script type="module">`로 로드해야 합니다

---

## 📚 API Reference

### 주요 클래스

#### ChatRoom 인터페이스
대화 세션을 관리하는 기본 인터페이스

**구현체:**
- `ResponsesChatRoom`: OpenAI Responses API용
- `GeminiChatRoom`: Google Gemini API용
- `AssistantsThread`: OpenAI Assistants API용

**주요 메서드:**
- `getId()`: ChatRoom의 고유 ID 반환
- `getChatting()`: Chatting 인스턴스 반환
- `close()`: ChatRoom 종료 및 리소스 정리
- `setActionQueueHandler(ActionQueueHandler)`: Action Queue 핸들러 설정
- `getActionQueueHandler()`: Action Queue 핸들러 반환

#### Chatting 인터페이스
개별 대화를 처리하는 인터페이스

**구현체:**
- `ResponsesChatting`: OpenAI Responses API용
- `GeminiChatting`: Google Gemini API용
- `AssistantsMessage`: OpenAI Assistants API용

**주요 메서드:**
- `sendPrompt(String userMsg)`: 프롬프트 전송 및 응답 수신 (JSONObject 반환)
- `getMessageIdSet()`: 메시지 ID Set 반환 (일부 구현체는 null 반환)
- `setActionQueueHandler(ActionQueueHandler)`: Action Queue 핸들러 설정

#### Assistants (OpenAI Assistants API 전용)
OpenAI Assistants API의 Assistant 정보를 관리하는 클래스

**주요 메서드:**
- `getAssistantId()`: Assistant ID 반환
- `getApiKey()`: API Key 반환
- `setApiKey(String apiKey)`: API Key 설정

#### AssistantsThread (OpenAI Assistants API 전용)
OpenAI Assistants API의 Thread를 관리하는 ChatRoom 구현체

**주요 메서드:**
- `getId()`: Thread ID 반환
- `getChatting()`: Chatting 인스턴스 반환
- `close()`: Thread 삭제 및 리소스 정리

#### ActionQueueHandler
Action Queue를 처리하는 클래스 (선택사항)

**주요 메서드:**
- `setCurrentViewInfo(String curViewInfo)`: 현재 화면 정보 저장
- `addCurrentViewInfo(JsonNode additionalViewInfo)`: 현재 화면 정보에 추가 정보 병합 (버전 0.6.0)
- `isCurrentViewInfo()`: 현재 화면 정보 저장 여부 확인
- `getCurViewPrompt()`: 현재 화면 정보 프롬프트 반환 (변경된 경우만)
- `getActionQueuePrompt(String userMsg)`: Action Queue 생성 요청 프롬프트 반환
- `getActionQueue(String resMsg)`: AI 응답에서 Action Queue 추출
- `clearCurrentViewInfo()`: 저장된 화면 정보 삭제
- `markViewInfoAsSent()`: 화면 정보 전송 완료 표시 (버전 0.6.0)
- `isViewInfoChanged()`: 화면 정보 변경 여부 확인 (버전 0.6.0)

자세한 API 문서는 [API.md](../docs/API.md) 또는 [JavaDoc](../docs/javadoc/)을 참조하세요.

---

## 🧑‍💻 기여 가이드

Pull Request 또는 Issue를 통해 다음과 같은 기여가 가능합니다:

* 🐞 버그 수정
* ✨ 기능 제안 및 개선
* 📝 문서 보강

👉 문의: **[kiunsea@gmail.com](mailto:kiunsea@gmail.com)**

---

## 📄 라이선스

이 프로젝트는 **Apache License, 버전 2.0**에 따라 배포됩니다.

라이선스의 전체 내용은 [LICENSE](../LICENSE) 파일을 참조해 주십시오.

---

**Copyright © 2025 [jiniebox.com](https://jiniebox.com)**

---

## 🔗 외부 링크

- Apache License, Version 2.0 (원문): http://www.apache.org/licenses/LICENSE-2.0
- 오픈소스SW 라이선스 종합정보시스템 (Apache-2.0): https://www.olis.or.kr/license/Detailselect.do?lId=1002
- 개발자 홈페이지: https://www.omnibuscode.com
- 문의: kiunsea@gmail.com

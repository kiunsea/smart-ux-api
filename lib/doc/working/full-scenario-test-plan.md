# Full Scenario Test Case 작성 계획서

## 1. 프로젝트 개요

### 목적
smuxapi-demo 실행 중 AI와의 대화 데이터를 수집하고, 이를 기반으로 Full Scenario Test Case를 작성하여 AI Assist 성능 측정과 오류 동작 점검을 수행합니다.

### 범위
- smuxapi-demo 실행 중 프롬프트/응답 데이터 수집
- 수집된 데이터를 활용한 테스트 케이스 자동 생성
- 테스트 케이스 실행을 통한 AI 응답 재현 및 검증

### 작업 절차 요약
1. **smuxapi-demo**: prompt/response 메시지 저장 로직 추가 (설정: `smuxapi-demo.yml`)
2. **smart-ux-api**: 저장된 메시지(JSON)를 테스트로 수행할 수 있는 테스트 클래스 준비
3. **테스트 클래스 설계**: 본 문서 내용을 참고하여 클래스 설계 및 필요 시 추가 클래스 작성

---

## 2. 데이터 수집 (smuxapi-demo)

### 2.1 수집 대상 데이터

#### ui_info
- **정의**: UI 정보 프롬프트 (ActionQueueHandler.getCurViewPrompt() 반환값)
- **생성 시점**: 화면 정보가 변경되어 사용자 발화 전에 먼저 전달되는 프롬프트
- **저장 위치**: 각 대화 턴별로 별도 저장

#### user_prompt
- **정의**: 사용자가 직접 입력한 발화 메시지
- **출처**: ActionQueueController에서 받은 user_msg 파라미터
- **저장 위치**: 각 대화 턴별로 별도 저장

#### api_prompt
- **정의**: API가 내부적으로 사용자 발화에 추가하는 프롬프트 부분
- **분리 방법**: ActionQueueHandler.getActionQueuePrompt(userMsg)에서 userMsg를 제외한 나머지 부분
- **예시**: "다음 UI 정보를 기반으로 Action Queue를 생성해주세요: [UI 정보] ... 사용자 요청: {UserMsg}"
- **저장 위치**: 각 대화 턴별로 별도 저장

#### res_msg
- **정의**: AI로부터 반환되는 응답 메시지
- **출처**: Chatting.sendPrompt() 반환값의 "message" 필드
- **저장 위치**: 각 대화 턴별로 별도 저장

### 2.2 설정 파일 (smuxapi-demo.yml)

데이터 수집 기능은 `smuxapi-demo.yml` 설정 파일에서 제어합니다.

```yaml
# smuxapi-demo.yml
smuxapi:
  scenario:
    # 시나리오 데이터 수집 활성화
    collect-enabled: true

    # 수집된 데이터 저장 경로
    output-path: "./scenarios/"

    # 파일명 접두사
    file-prefix: "test-scenario"
```

### 2.3 데이터 수집 구현 (smuxapi-demo)

#### 2.3.1 Decorator 패턴 기반 수집 클래스

데이터 수집을 위해 기존 클래스를 감싸는 Decorator 클래스를 생성합니다.

##### ChattingCollector (Chatting Decorator)
- **클래스명**: `ChattingCollector`
- **위치**: `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/`
- **구현**: `Chatting` 인터페이스 구현
- **기능**:
  - 내부에 실제 Chatting 구현체를 보유 (GeminiChatting 또는 ResponsesChatting)
  - sendPrompt() 호출 시 프롬프트와 응답을 캡처
  - 수집된 데이터를 PromptResponseCollector에 전달

```java
public class ChattingCollector implements Chatting {
    private final Chatting delegate;
    private final PromptResponseCollector collector;

    public ChattingCollector(Chatting delegate, PromptResponseCollector collector) {
        this.delegate = delegate;
        this.collector = collector;
    }

    @Override
    public JSONObject sendPrompt(String userMsg) throws Exception {
        // 1. user_prompt 수집
        collector.captureUserPrompt(userMsg);

        // 2. 실제 sendPrompt 호출
        JSONObject response = delegate.sendPrompt(userMsg);

        // 3. res_msg 및 action_queue 수집
        collector.captureResponse(response);

        return response;
    }

    // 기타 메서드는 delegate에 위임
}
```

##### ActionQueueHandlerCollector (ActionQueueHandler Decorator)
- **클래스명**: `ActionQueueHandlerCollector`
- **위치**: `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/`
- **기능**:
  - 내부에 실제 ActionQueueHandler를 보유
  - getCurViewPrompt(), getActionQueuePrompt() 호출 시 데이터 캡처
  - 수집된 데이터를 PromptResponseCollector에 전달

```java
public class ActionQueueHandlerCollector {
    private final ActionQueueHandler delegate;
    private final PromptResponseCollector collector;

    public ActionQueueHandlerCollector(ActionQueueHandler delegate, PromptResponseCollector collector) {
        this.delegate = delegate;
        this.collector = collector;
    }

    public String getCurViewPrompt() {
        String uiInfo = delegate.getCurViewPrompt();
        collector.captureUiInfo(uiInfo);
        return uiInfo;
    }

    public String getActionQueuePrompt(String userMsg) {
        String fullPrompt = delegate.getActionQueuePrompt(userMsg);
        // api_prompt = fullPrompt에서 userMsg 부분을 플레이스홀더로 대체
        String apiPrompt = extractApiPromptTemplate(fullPrompt, userMsg);
        collector.captureApiPrompt(apiPrompt);
        return fullPrompt;
    }

    // 기타 메서드는 delegate에 위임
}
```

##### PromptResponseCollector (데이터 수집기)
- **클래스명**: `PromptResponseCollector`
- **위치**: `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/`
- **기능**:
  - 각 턴별 수집 데이터 관리 (ui_info, user_prompt, api_prompt, res_msg, action_queue)
  - 시나리오 데이터를 JSON 파일로 저장
  - 세션 정보 및 AI 모델 정보 관리

```java
@Component
public class PromptResponseCollector {
    @Value("${smuxapi.scenario.collect-enabled:false}")
    private boolean collectEnabled;

    @Value("${smuxapi.scenario.output-path:./scenarios/}")
    private String outputPath;

    @Value("${smuxapi.scenario.file-prefix:test-scenario}")
    private String filePrefix;

    private String sessionId;
    private String aiModel;
    private List<ScenarioTurn> turns = new ArrayList<>();
    private ScenarioTurn currentTurn;

    public boolean isEnabled() {
        return collectEnabled;
    }

    public void startNewTurn() {
        if (!collectEnabled) return;
        currentTurn = new ScenarioTurn(turns.size() + 1);
    }

    public void captureUiInfo(String uiInfo) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setUiInfo(uiInfo);
    }

    public void captureUserPrompt(String userPrompt) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setUserPrompt(userPrompt);
    }

    public void captureApiPrompt(String apiPrompt) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setApiPrompt(apiPrompt);
    }

    public void captureResponse(JSONObject response) {
        if (!collectEnabled || currentTurn == null) return;
        currentTurn.setResMsg(response.get("message").toString());
        currentTurn.setActionQueue(response.get("action_queue"));
        turns.add(currentTurn);
    }

    public void saveToFile() throws IOException {
        if (!collectEnabled || turns.isEmpty()) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String fileName = String.format("%s-%s-%s.json", filePrefix, timestamp, sessionId);
        String filePath = outputPath + fileName;

        // JSON 직렬화 및 파일 저장
        ObjectMapper mapper = new ObjectMapper();
        ScenarioData data = new ScenarioData(sessionId, aiModel, timestamp, turns);
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), data);
    }
}
```

#### 2.3.2 ChatRoomService 통합

ChatRoomService에서 수집 모드가 활성화된 경우 Decorator를 적용합니다.

```java
@Service
public class ChatRoomService {
    @Autowired
    private PromptResponseCollector collector;

    public ChatRoom getChatRoom(String aiModel, HttpSession sess) {
        ChatRoom chatRoom = createChatRoom(aiModel);

        // 수집 모드가 활성화된 경우 Decorator 적용
        if (collector.isEnabled()) {
            collector.setSessionId(sess.getId());
            collector.setAiModel(aiModel);
            chatRoom = wrapWithCollector(chatRoom);
        }

        return chatRoom;
    }

    private ChatRoom wrapWithCollector(ChatRoom chatRoom) {
        // ActionQueueHandler Decorator 적용
        ActionQueueHandler realHandler = chatRoom.getActionQueueHandler();
        ActionQueueHandlerCollector handlerCollector =
            new ActionQueueHandlerCollector(realHandler, collector);

        // Chatting Decorator 적용
        Chatting realChatting = chatRoom.getChatting();
        ChattingCollector chattingCollector =
            new ChattingCollector(realChatting, collector);

        // Decorator가 적용된 ChatRoom 반환
        return new CollectorChatRoom(chatRoom, handlerCollector, chattingCollector);
    }
}
```

#### 2.3.3 데이터 수집 포인트

1. **ActionQueueHandlerCollector.getCurViewPrompt()**
   - UI 정보 프롬프트 생성 시점에 ui_info 저장

2. **ActionQueueHandlerCollector.getActionQueuePrompt()**
   - Action Queue 프롬프트 생성 시점에 api_prompt 템플릿 저장
   - userMsg는 플레이스홀더 `{USER_MSG}`로 대체하여 저장

3. **ChattingCollector.sendPrompt()**
   - user_prompt 저장 (sendPrompt 호출 전)
   - res_msg 및 action_queue 저장 (sendPrompt 호출 후)

### 2.4 데이터 저장 형식

- **파일 형식**: JSON
- **파일명**: `{file-prefix}-{timestamp}-{sessionId}.json`
- **저장 위치**: `smuxapi.scenario.output-path` 설정값 (기본: `./scenarios/`)
- **데이터 구조**:
```json
{
  "sessionId": "session_12345",
  "aiModel": "chatgpt",
  "timestamp": "2026-01-22T12:00:00",
  "scenarios": [
    {
      "turn": 1,
      "ui_info": "Current View Info: {...}",
      "user_prompt": "아이스 아메리카노 주문해줘",
      "api_prompt": "다음 UI 정보를 기반으로 Action Queue를 생성해주세요: ... 사용자 요청: {USER_MSG}",
      "res_msg": "AI 응답 메시지...",
      "action_queue": [
        {
          "id": "ice_아메리카노",
          "type": "click",
          "action": "click"
        }
      ]
    }
  ]
}
```

### 2.5 구현 계획 및 절차

#### 2.5.1 구현 전 준비 사항

**필요한 정보 확인:**
1. `ChatRoom` 인터페이스 구조 확인 (getChatting(), getActionQueueHandler() 메서드)
2. `Chatting` 인터페이스 구조 확인 (sendPrompt() 메서드)
3. `ActionQueueHandler` 클래스 구조 확인 (getCurViewPrompt(), getActionQueuePrompt() 메서드)
4. `ActionQueueController`의 요청 처리 흐름 확인
5. 세션 관리 방식 확인 (HttpSession 사용)

**기존 코드 분석:**
- `ChatRoomService.createChatRoom()`: ChatRoom 생성 로직
- `ActionQueueController.handleGet()`: 사용자 요청 처리 및 Chatting.sendPrompt() 호출
- `UXInfoController`: UI 정보 설정 (setCurrentViewInfo() 호출)

#### 2.5.2 구현 단계별 절차

##### Step 1: 설정 파일 업데이트
**작업 내용:**
1. `smuxapi-demo.yml` 파일에 시나리오 수집 설정 섹션 추가
2. `collect-enabled`, `output-path`, `file-prefix` 설정 항목 추가

**파일 위치:**
- `smuxapi-demo/src/main/resources/smuxapi-demo.yml`
- `packaging/distribution/smuxapi-demo/smuxapi-demo.yml` (배포용)

**구현 방법:**
```yaml
smuxapi:
  scenario:
    collect-enabled: true
    output-path: "./scenarios/"
    file-prefix: "test-scenario"
```

**검증 방법:**
- YAML 파일 문법 검증
- Spring Boot 설정 로드 테스트

---

##### Step 2: 데이터 모델 클래스 생성
**작업 내용:**
1. `ScenarioTurn` 클래스 생성 (턴별 데이터 모델)
2. `ScenarioData` 클래스 생성 (전체 시나리오 데이터 모델)

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/model/ScenarioTurn.java`
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/model/ScenarioData.java`

**구현 방법:**
- Jackson 어노테이션 사용 (`@JsonProperty`, `@JsonInclude`)
- getter/setter 메서드 구현
- JSON 직렬화/역직렬화 지원

**검증 방법:**
- 단위 테스트로 JSON 직렬화/역직렬화 검증

---

##### Step 3: PromptResponseCollector 클래스 구현
**작업 내용:**
1. Spring `@Component`로 등록
2. `@Value` 어노테이션으로 설정 값 주입
3. 턴별 데이터 수집 메서드 구현:
   - `startNewTurn()`: 새 턴 시작
   - `captureUiInfo()`: ui_info 수집
   - `captureUserPrompt()`: user_prompt 수집
   - `captureApiPrompt()`: api_prompt 수집
   - `captureResponse()`: res_msg 및 action_queue 수집
4. `saveToFile()`: JSON 파일 저장 로직
5. 세션 정보 관리 (sessionId, aiModel 설정)

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/PromptResponseCollector.java`

**구현 방법:**
- `collectEnabled` 플래그로 수집 모드 제어
- `currentTurn` 변수로 현재 턴 추적
- `turns` 리스트로 모든 턴 데이터 관리
- Jackson `ObjectMapper`로 JSON 파일 저장
- 파일 경로 자동 생성 및 디렉터리 생성

**검증 방법:**
- 수집 모드 비활성화 시 동작 안 함 확인
- 각 수집 메서드 호출 시 데이터 저장 확인
- JSON 파일 생성 및 형식 검증

---

##### Step 4: ActionQueueHandlerCollector 클래스 구현
**작업 내용:**
1. Decorator 패턴으로 `ActionQueueHandler` 래핑
2. `getCurViewPrompt()` 메서드 오버라이드: ui_info 수집
3. `getActionQueuePrompt()` 메서드 오버라이드: api_prompt 템플릿 추출
4. 나머지 메서드는 delegate에 위임

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/ActionQueueHandlerCollector.java`

**구현 방법:**
- `ActionQueueHandler`를 멤버 변수로 보유 (delegate)
- `PromptResponseCollector`를 멤버 변수로 보유
- `getCurViewPrompt()`: delegate 호출 → 결과를 collector에 전달 → 결과 반환
- `getActionQueuePrompt(userMsg)`: delegate 호출 → fullPrompt에서 userMsg를 `{USER_MSG}`로 대체 → collector에 전달 → fullPrompt 반환
- `extractApiPromptTemplate()`: fullPrompt에서 userMsg 부분을 찾아 `{USER_MSG}`로 대체

**api_prompt 추출 로직:**
```java
private String extractApiPromptTemplate(String fullPrompt, String userMsg) {
    // fullPrompt에서 userMsg를 찾아 {USER_MSG}로 대체
    // 예: "사용자 요청: 아이스 아메리카노" → "사용자 요청: {USER_MSG}"
    if (userMsg != null && fullPrompt.contains(userMsg)) {
        return fullPrompt.replace(userMsg, "{USER_MSG}");
    }
    return fullPrompt;
}
```

**검증 방법:**
- delegate 메서드 호출 시 수집 로직 동작 확인
- api_prompt 템플릿 추출 정확성 검증

---

##### Step 5: ChattingCollector 클래스 구현
**작업 내용:**
1. `Chatting` 인터페이스 구현
2. Decorator 패턴으로 실제 Chatting 구현체 래핑
3. `sendPrompt()` 메서드 오버라이드:
   - user_prompt 수집 (호출 전)
   - delegate.sendPrompt() 호출
   - res_msg 및 action_queue 수집 (호출 후)
4. 나머지 메서드는 delegate에 위임

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/ChattingCollector.java`

**구현 방법:**
- `Chatting`을 멤버 변수로 보유 (delegate)
- `PromptResponseCollector`를 멤버 변수로 보유
- `sendPrompt(userMsg)`: 
  1. `collector.captureUserPrompt(userMsg)` 호출
  2. `delegate.sendPrompt(userMsg)` 호출
  3. 응답에서 "message"와 "action_queue" 추출
  4. `collector.captureResponse(response)` 호출
  5. 응답 반환

**검증 방법:**
- sendPrompt 호출 시 user_prompt 수집 확인
- 응답 수신 시 res_msg 및 action_queue 수집 확인
- delegate 메서드 정상 동작 확인

---

##### Step 6: CollectorChatRoom 클래스 구현
**작업 내용:**
1. `ChatRoom` 인터페이스 구현
2. 원본 ChatRoom과 Decorator들을 보유
3. `getChatting()`: ChattingCollector 반환
4. `getActionQueueHandler()`: ActionQueueHandlerCollector 반환
5. 나머지 메서드는 원본 ChatRoom에 위임

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/collector/CollectorChatRoom.java`

**구현 방법:**
- 원본 `ChatRoom`을 멤버 변수로 보유
- `ActionQueueHandlerCollector`를 멤버 변수로 보유
- `ChattingCollector`를 멤버 변수로 보유
- `getChatting()`: ChattingCollector 반환
- `getActionQueueHandler()`: ActionQueueHandlerCollector 반환
- 기타 메서드 (getId() 등): 원본 ChatRoom에 위임

**검증 방법:**
- ChatRoom 인터페이스 메서드 정상 동작 확인
- Decorator들이 정상적으로 적용되는지 확인

---

##### Step 7: ChatRoomService 통합
**작업 내용:**
1. `PromptResponseCollector`를 `@Autowired`로 주입
2. `getChatRoom()` 메서드 수정:
   - 수집 모드 활성화 확인
   - 활성화된 경우 Decorator 적용
   - 세션 정보 설정 (sessionId, aiModel)
3. `wrapWithCollector()` 메서드 추가: Decorator 적용 로직

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/service/ChatRoomService.java`

**구현 방법:**
```java
@Service
public class ChatRoomService {
    @Autowired
    private PromptResponseCollector collector;

    public ChatRoom getChatRoom(HttpServletRequest req) {
        ChatRoom chatRoom = createChatRoom(aiModel);
        
        // 수집 모드가 활성화된 경우 Decorator 적용
        if (collector.isEnabled()) {
            HttpSession sess = req.getSession(true);
            collector.setSessionId(sess.getId());
            collector.setAiModel(aiModel);
            chatRoom = wrapWithCollector(chatRoom);
        }
        
        return chatRoom;
    }
    
    private ChatRoom wrapWithCollector(ChatRoom chatRoom) {
        ActionQueueHandler realHandler = chatRoom.getActionQueueHandler();
        ActionQueueHandlerCollector handlerCollector = 
            new ActionQueueHandlerCollector(realHandler, collector);
        
        Chatting realChatting = chatRoom.getChatting();
        ChattingCollector chattingCollector = 
            new ChattingCollector(realChatting, collector);
        
        return new CollectorChatRoom(chatRoom, handlerCollector, chattingCollector);
    }
}
```

**검증 방법:**
- 수집 모드 비활성화 시 원본 ChatRoom 반환 확인
- 수집 모드 활성화 시 Decorator 적용 확인
- 세션 정보 설정 확인

---

##### Step 8: 턴 시작/종료 로직 추가
**작업 내용:**
1. `ActionQueueController`에서 턴 시작 로직 추가
2. 세션 종료 시 파일 저장 로직 추가 (선택사항)

**파일 위치:**
- `smuxapi-demo/src/main/java/com/smartuxapi/demo/controller/ActionQueueController.java`

**구현 방법:**
```java
@GetMapping
public JSONObject handleGet(HttpServletRequest req) {
    ChatRoom chatRoom = chatRoomService.getChatRoom(req);
    
    // 수집 모드: 새 턴 시작
    if (collector.isEnabled()) {
        collector.startNewTurn();
    }
    
    String userMsg = req.getParameter("user_msg");
    // ... 기존 로직 ...
    
    return resObj;
}
```

**검증 방법:**
- 각 요청마다 새 턴이 시작되는지 확인
- 턴 번호가 순차적으로 증가하는지 확인

---

##### Step 9: 파일 저장 시점 결정
**작업 내용:**
1. 세션 종료 시 자동 저장 (HttpSessionListener 구현)
2. 또는 수동 저장 API 엔드포인트 추가 (선택사항)

**구현 방법 (선택 1: 세션 종료 시 자동 저장):**
```java
@Component
public class ScenarioSessionListener implements HttpSessionListener {
    @Autowired
    private PromptResponseCollector collector;
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        if (collector.isEnabled()) {
            try {
                collector.saveToFile();
            } catch (IOException e) {
                log.error("Failed to save scenario data", e);
            }
        }
    }
}
```

**구현 방법 (선택 2: 수동 저장 API):**
```java
@RestController
@RequestMapping("/scenario")
public class ScenarioController {
    @Autowired
    private PromptResponseCollector collector;
    
    @PostMapping("/save")
    public ResponseEntity<String> saveScenario() {
        if (collector.isEnabled()) {
            try {
                collector.saveToFile();
                return ResponseEntity.ok("Scenario saved");
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Failed to save");
            }
        }
        return ResponseEntity.ok("Collection disabled");
    }
}
```

**검증 방법:**
- 세션 종료 시 파일 저장 확인
- 또는 수동 저장 API 호출 시 파일 저장 확인

---

#### 2.5.3 구현 순서 요약

1. **설정 파일 업데이트** (Step 1)
2. **데이터 모델 클래스 생성** (Step 2)
3. **PromptResponseCollector 구현** (Step 3)
4. **ActionQueueHandlerCollector 구현** (Step 4)
5. **ChattingCollector 구현** (Step 5)
6. **CollectorChatRoom 구현** (Step 6)
7. **ChatRoomService 통합** (Step 7)
8. **턴 시작/종료 로직 추가** (Step 8)
9. **파일 저장 시점 결정 및 구현** (Step 9)

#### 2.5.4 구현 시 주의사항

1. **기존 코드 영향 최소화**: Decorator 패턴을 사용하여 기존 코드 수정 최소화
2. **수집 모드 제어**: `collect-enabled: false`일 때는 수집 로직이 실행되지 않도록 처리
3. **에러 처리**: 파일 저장 실패 시에도 애플리케이션이 정상 동작하도록 예외 처리
4. **동시성 고려**: 세션별로 독립적인 PromptResponseCollector 인스턴스 사용 (또는 ThreadLocal 사용)
5. **메모리 관리**: 대량의 데이터 수집 시 메모리 사용량 모니터링
6. **파일 경로**: 상대 경로 사용 시 JAR 실행 위치 기준으로 동작하도록 구현

#### 2.5.5 테스트 계획

**단위 테스트:**
- 각 Collector 클래스의 수집 로직 테스트
- JSON 직렬화/역직렬화 테스트
- api_prompt 템플릿 추출 로직 테스트

**통합 테스트:**
- 전체 수집 플로우 테스트 (ActionQueueController → ChatRoomService → Collectors)
- 실제 AI API 호출 없이 Mock 객체로 테스트
- JSON 파일 생성 및 형식 검증

**수동 테스트:**
- smuxapi-demo 실행 후 실제 시나리오 수행
- 생성된 JSON 파일 내용 검증
- 여러 턴의 시나리오 수집 검증

---

## 3. 테스트 케이스 실행 (smart-ux-api)

### 3.1 테스트 케이스 클래스 구조

#### 3.1.1 메인 테스트 클래스
- **클래스명**: `FullScenarioTestCase`
- **위치**: `smart-ux-api/lib/src/test/java/com/smartuxapi/scenario/`
- **기능**:
  - 수집된 JSON 파일 읽기
  - 각 시나리오 턴별로 AI와 대화 재현
  - Action Queue 검증 및 결과 리포트 생성

#### 3.1.2 테스트 설정 (멤버 변수)
```java
public class FullScenarioTestCase {
    // 테스트 대상 시나리오 파일 경로
    private String scenarioFilePath = "src/test/resources/scenarios/test-scenario-xxx.json";

    // 테스트 결과 저장 경로
    private String resultOutputPath = "src/test/resources/test-results/";

    // Action Queue 검증 활성화
    private boolean validateActionQueue = true;

    // 테스트 실행 모드 (단일 파일 / 배치)
    private boolean batchMode = false;

    // 배치 모드 시 시나리오 디렉터리
    private String scenarioDirectory = "src/test/resources/scenarios/";
}
```

#### 3.1.3 테스트 실행 프로세스
1. JSON 파일에서 시나리오 데이터 로드
2. ChatRoom 및 Chatting 인스턴스 생성 (수집 시점과 동일한 AI 모델)
3. 각 턴별로 다음 순서로 실행:
   - UI 정보 설정 (ui_info가 있는 경우)
   - 사용자 프롬프트 전송 (user_prompt 사용)
   - AI 응답 수신
   - Action Queue 추출 및 검증
4. 테스트 결과 산출물 생성:
   - AI 응답(res_msg)을 별도 JSON 파일로 저장
   - Action Queue를 client에 전송하기 직전 형식으로 JSON 파일 저장
5. 결과 리포트 생성 (Action Queue 검증 결과 포함)

### 3.2 테스트 케이스 구현 세부사항

#### 3.2.1 시나리오 데이터 로더
- **클래스명**: `ScenarioDataLoader`
- **위치**: `smart-ux-api/lib/src/test/java/com/smartuxapi/scenario/`
- **기능**: JSON 파일 파싱 및 시나리오 객체 변환
- **데이터 모델**: ScenarioData, ScenarioTurn 클래스

```java
public class ScenarioDataLoader {
    public ScenarioData load(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), ScenarioData.class);
    }
}

public class ScenarioData {
    private String sessionId;
    private String aiModel;
    private String timestamp;
    private List<ScenarioTurn> scenarios;
    // getters, setters
}

public class ScenarioTurn {
    private int turn;
    private String uiInfo;
    private String userPrompt;
    private String apiPrompt;
    private String resMsg;
    private JsonNode actionQueue;
    // getters, setters
}
```

#### 3.2.2 AI 대화 재현 로직
- **ChatRoom 생성**: 수집 시점과 동일한 AI 모델로 ChatRoom 생성
- **ActionQueueHandler 설정**: 동일한 설정으로 ActionQueueHandler 생성
- **UI 정보 설정**: ui_info가 있는 경우 ActionQueueHandler.setCurrentViewInfo() 호출
- **프롬프트 전송**: user_prompt를 Chatting.sendPrompt()에 전달
- **Action Queue 추출**: res_msg에서 ActionQueueHandler.getActionQueue()를 통해 추출

#### 3.2.3 검증 로직 (Action Queue 비교)

Action Queue만을 대상으로 검증을 수행합니다.

- **클래스명**: `ActionQueueValidator`
- **위치**: `smart-ux-api/lib/src/test/java/com/smartuxapi/scenario/validator/`

```java
public class ActionQueueValidator {

    /**
     * 수집된 Action Queue와 실제 응답에서 추출한 Action Queue를 비교
     * @param expected 수집 시점의 Action Queue (JsonNode)
     * @param actual 테스트 실행 시 추출한 Action Queue (JsonNode)
     * @return 검증 결과
     */
    public ValidationResult validate(JsonNode expected, JsonNode actual) {
        ValidationResult result = new ValidationResult();

        // 1. null 체크
        if (expected == null && actual == null) {
            result.setMatch(true);
            result.setMessage("Both Action Queues are null");
            return result;
        }

        if (expected == null || actual == null) {
            result.setMatch(false);
            result.setMessage("One Action Queue is null");
            result.setExpected(expected);
            result.setActual(actual);
            return result;
        }

        // 2. 배열 크기 비교
        if (expected.size() != actual.size()) {
            result.setMatch(false);
            result.setMessage("Action Queue size mismatch");
            result.setExpected(expected);
            result.setActual(actual);
            return result;
        }

        // 3. 각 Action 항목 비교
        List<ActionDiff> diffs = new ArrayList<>();
        for (int i = 0; i < expected.size(); i++) {
            JsonNode expectedAction = expected.get(i);
            JsonNode actualAction = actual.get(i);

            ActionDiff diff = compareAction(i, expectedAction, actualAction);
            if (diff != null) {
                diffs.add(diff);
            }
        }

        result.setMatch(diffs.isEmpty());
        result.setDiffs(diffs);
        return result;
    }

    /**
     * 개별 Action 항목 비교
     * 필수 필드: id, type, action
     */
    private ActionDiff compareAction(int index, JsonNode expected, JsonNode actual) {
        List<String> requiredFields = Arrays.asList("id", "type", "action");
        List<String> mismatches = new ArrayList<>();

        for (String field : requiredFields) {
            JsonNode expectedValue = expected.get(field);
            JsonNode actualValue = actual.get(field);

            if (!Objects.equals(expectedValue, actualValue)) {
                mismatches.add(field + ": expected=" + expectedValue + ", actual=" + actualValue);
            }
        }

        if (mismatches.isEmpty()) {
            return null;
        }

        return new ActionDiff(index, mismatches);
    }
}

public class ValidationResult {
    private boolean match;
    private String message;
    private JsonNode expected;
    private JsonNode actual;
    private List<ActionDiff> diffs;
    // getters, setters
}

public class ActionDiff {
    private int index;
    private List<String> fieldMismatches;
    // constructor, getters
}

public class TestResult {
    private String sessionId;
    private String aiModel;
    private List<Integer> successTurns = new ArrayList<>();
    private Map<Integer, ValidationResult> failedTurns = new HashMap<>();
    private Map<Integer, String> errorTurns = new HashMap<>();

    public TestResult(String sessionId, String aiModel) {
        this.sessionId = sessionId;
        this.aiModel = aiModel;
    }

    public void addSuccess(int turn) { successTurns.add(turn); }
    public void addFailure(int turn, ValidationResult result) { failedTurns.put(turn, result); }
    public void addError(int turn, String message) { errorTurns.put(turn, message); }

    public int getTotalTurns() { return successTurns.size() + failedTurns.size() + errorTurns.size(); }
    public int getSuccessCount() { return successTurns.size(); }
    public int getFailedCount() { return failedTurns.size(); }
    public int getErrorCount() { return errorTurns.size(); }
    public double getSuccessRate() { return getTotalTurns() > 0 ? (double) successTurns.size() / getTotalTurns() : 0; }

    // getters
}
```

#### 3.2.4 오류 감지
- **Action Queue 추출 오류**: Action Queue가 없는 경우 또는 형식 오류
- **Action Queue 불일치**: 수집된 것과 실제 추출된 것의 차이
- **API 호출 오류**: 네트워크 오류, 타임아웃 등

#### 3.2.5 테스트 결과 파일 저장

- **클래스명**: `ResultWriter`
- **위치**: `smart-ux-api/lib/src/test/java/com/smartuxapi/scenario/writer/`

```java
public class ResultWriter {
    private String basePath;

    public ResultWriter(String basePath) {
        this.basePath = basePath;
    }

    /**
     * AI 응답 저장
     */
    public void writeResponse(String sessionId, int turn, String message) throws IOException {
        String dirPath = basePath + "/" + sessionId;
        Files.createDirectories(Paths.get(dirPath));

        JSONObject json = new JSONObject();
        json.put("turn", turn);
        json.put("sessionId", sessionId);
        json.put("timestamp", LocalDateTime.now().toString());
        json.put("message", message);

        String filePath = dirPath + "/turn" + turn + "-response.json";
        Files.writeString(Paths.get(filePath), json.toJSONString());
    }

    /**
     * Action Queue 저장 (client 전송 형식)
     */
    public void writeActionQueue(String sessionId, int turn, JsonNode actionQueue) throws IOException {
        String dirPath = basePath + "/" + sessionId;
        Files.createDirectories(Paths.get(dirPath));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode json = mapper.createObjectNode();
        json.set("action_queue", actionQueue != null ? actionQueue : mapper.nullNode());

        String filePath = dirPath + "/turn" + turn + "-action-queue.json";
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), json);
    }
}
```

- **AI 응답 파일**: `turn{turn}-response.json`
  - 저장 위치: `test-results/{sessionId}/`
  - 내용 형식:
    ```json
    {
      "turn": 1,
      "sessionId": "session_12345",
      "timestamp": "2026-01-22T12:00:00",
      "message": "AI 응답 메시지 전체 내용..."
    }
    ```
- **Action Queue 파일**: `turn{turn}-action-queue.json`
  - 저장 위치: `test-results/{sessionId}/`
  - 내용 형식: ActionQueueController가 client에 전송하는 형식과 동일
    ```json
    {
      "action_queue": [
        {
          "id": "ice_아메리카노",
          "type": "click",
          "action": "click"
        }
      ]
    }
    ```
  - Action Queue가 없는 경우: `{"action_queue": null}`

### 3.3 테스트 실행 방법

#### 3.3.1 Main 함수로 실행
```java
public class FullScenarioTestCase {
    // 테스트 대상 시나리오 파일 경로
    private String scenarioFilePath = "src/test/resources/scenarios/test-scenario-xxx.json";

    // 테스트 결과 저장 경로
    private String resultOutputPath = "src/test/resources/test-results/";

    // Action Queue 검증 활성화
    private boolean validateActionQueue = true;

    // 테스트 실행 모드 (단일 파일 / 배치)
    private boolean batchMode = false;

    // 배치 모드 시 시나리오 디렉터리
    private String scenarioDirectory = "src/test/resources/scenarios/";

    public static void main(String[] args) {
        FullScenarioTestCase testCase = new FullScenarioTestCase();

        // 커맨드라인 인자 처리
        if (args.length > 0) {
            testCase.scenarioFilePath = args[0];
        }
        if (args.length > 1) {
            testCase.resultOutputPath = args[1];
        }
        if (args.length > 2 && args[2].equals("--batch")) {
            testCase.batchMode = true;
            testCase.scenarioDirectory = args[0];  // 배치 모드에서는 첫 번째 인자가 디렉터리
        }

        try {
            if (testCase.batchMode) {
                testCase.runBatch();
            } else {
                testCase.runSingle();
            }
        } catch (Exception e) {
            System.err.println("Test execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 단일 시나리오 파일 실행
     */
    public void runSingle() throws Exception {
        System.out.println("=== Full Scenario Test Case ===");
        System.out.println("Scenario file: " + scenarioFilePath);
        System.out.println("Result output: " + resultOutputPath);
        System.out.println();

        // 시나리오 로드
        ScenarioDataLoader loader = new ScenarioDataLoader();
        ScenarioData scenario = loader.load(scenarioFilePath);

        // 테스트 실행
        TestResult result = executeScenario(scenario);

        // 결과 출력
        printResult(result);
    }

    /**
     * 배치 실행 (디렉터리 내 모든 시나리오)
     */
    public void runBatch() throws Exception {
        System.out.println("=== Full Scenario Test Case (Batch Mode) ===");
        System.out.println("Scenario directory: " + scenarioDirectory);
        System.out.println("Result output: " + resultOutputPath);
        System.out.println();

        // 디렉터리에서 모든 JSON 파일 찾기
        File dir = new File(scenarioDirectory);
        File[] scenarioFiles = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (scenarioFiles == null || scenarioFiles.length == 0) {
            System.out.println("No scenario files found in: " + scenarioDirectory);
            return;
        }

        List<TestResult> allResults = new ArrayList<>();
        ScenarioDataLoader loader = new ScenarioDataLoader();

        for (File file : scenarioFiles) {
            System.out.println("Processing: " + file.getName());
            ScenarioData scenario = loader.load(file.getAbsolutePath());
            TestResult result = executeScenario(scenario);
            allResults.add(result);
        }

        // 종합 리포트 생성
        generateReport(allResults);
    }

    /**
     * 시나리오 실행
     */
    private TestResult executeScenario(ScenarioData scenario) throws Exception {
        TestResult testResult = new TestResult(scenario.getSessionId(), scenario.getAiModel());

        // ChatRoom 생성
        ChatRoom chatRoom = createChatRoom(scenario.getAiModel());
        ActionQueueHandler aqHandler = chatRoom.getActionQueueHandler();
        Chatting chatting = chatRoom.getChatting();

        // ResultWriter 초기화
        ResultWriter writer = new ResultWriter(resultOutputPath);
        ActionQueueValidator validator = new ActionQueueValidator();

        // 각 턴 실행
        for (ScenarioTurn turn : scenario.getScenarios()) {
            System.out.println("  Turn " + turn.getTurn() + ": " + turn.getUserPrompt());

            try {
                // UI 정보 설정
                if (turn.getUiInfo() != null) {
                    aqHandler.setCurrentViewInfo(turn.getUiInfo());
                }

                // 프롬프트 전송
                JSONObject response = chatting.sendPrompt(turn.getUserPrompt());

                // 결과 저장
                String resMsg = response.get("message").toString();
                JsonNode actualActionQueue = aqHandler.getActionQueue(resMsg);

                writer.writeResponse(scenario.getSessionId(), turn.getTurn(), resMsg);
                writer.writeActionQueue(scenario.getSessionId(), turn.getTurn(), actualActionQueue);

                // Action Queue 검증
                if (validateActionQueue) {
                    ValidationResult result = validator.validate(turn.getActionQueue(), actualActionQueue);

                    if (result.isMatch()) {
                        testResult.addSuccess(turn.getTurn());
                        System.out.println("    [PASS] Action Queue matched");
                    } else {
                        testResult.addFailure(turn.getTurn(), result);
                        System.out.println("    [FAIL] Action Queue mismatch");
                        System.out.println("      Expected: " + turn.getActionQueue());
                        System.out.println("      Actual: " + actualActionQueue);
                    }
                }
            } catch (Exception e) {
                testResult.addError(turn.getTurn(), e.getMessage());
                System.out.println("    [ERROR] " + e.getMessage());
            }
        }

        return testResult;
    }

    private void printResult(TestResult result) {
        System.out.println();
        System.out.println("=== Test Result ===");
        System.out.println("Session: " + result.getSessionId());
        System.out.println("AI Model: " + result.getAiModel());
        System.out.println("Total Turns: " + result.getTotalTurns());
        System.out.println("Success: " + result.getSuccessCount());
        System.out.println("Failed: " + result.getFailedCount());
        System.out.println("Errors: " + result.getErrorCount());
        System.out.println("Success Rate: " + String.format("%.2f%%", result.getSuccessRate() * 100));
    }

    private void generateReport(List<TestResult> results) throws IOException {
        // ReportGenerator를 사용하여 종합 리포트 생성
        ReportGenerator generator = new ReportGenerator(resultOutputPath);
        generator.generate(results);
        System.out.println();
        System.out.println("Report generated: " + resultOutputPath + "test-report.json");
    }

    private ChatRoom createChatRoom(String aiModel) {
        // AI 모델에 따른 ChatRoom 생성 로직
        // ...
    }
}
```

**실행 예시**:
```bash
# 단일 파일 실행
java -cp ... com.smartuxapi.scenario.FullScenarioTestCase scenarios/test-scenario-20260122-12345.json

# 결과 출력 경로 지정
java -cp ... com.smartuxapi.scenario.FullScenarioTestCase scenarios/test-scenario-20260122-12345.json test-results/

# 배치 모드 (디렉터리 내 모든 시나리오 실행)
java -cp ... com.smartuxapi.scenario.FullScenarioTestCase scenarios/ test-results/ --batch
```

#### 3.3.2 배치 실행
- **여러 시나리오 파일**: 디렉터리 지정 시 모든 JSON 파일 실행
- **결과 집계**: 모든 테스트 결과를 종합 리포트로 생성
- **출력 파일 구조**:
  ```
  test-results/
  ├── {sessionId}/
  │   ├── turn1-response.json          # AI 응답
  │   ├── turn1-action-queue.json      # Action Queue (client 전송 형식)
  │   ├── turn2-response.json
  │   ├── turn2-action-queue.json
  │   └── ...
  └── test-report.json                  # 종합 리포트
  ```

#### 3.3.3 종합 리포트 형식
```json
{
  "executionTime": "2026-01-22T12:00:00",
  "totalScenarios": 5,
  "totalTurns": 25,
  "results": [
    {
      "sessionId": "session_12345",
      "aiModel": "chatgpt",
      "turnsTotal": 5,
      "turnsSuccess": 4,
      "turnsFailed": 1,
      "failures": [
        {
          "turn": 3,
          "reason": "Action Queue mismatch",
          "expected": [...],
          "actual": [...]
        }
      ]
    }
  ],
  "summary": {
    "successRate": 0.96,
    "totalSuccess": 24,
    "totalFailed": 1
  }
}
```

---

## 4. 구현 단계

### Phase 1: smuxapi-demo 데이터 수집 기능 구현
1. `smuxapi-demo.yml`에 시나리오 수집 설정 옵션 추가
2. PromptResponseCollector 클래스 생성 (`smuxapi-demo/src/main/java/.../collector/`)
3. ActionQueueHandlerCollector (Decorator) 클래스 생성
4. ChattingCollector (Decorator) 클래스 생성
5. CollectorChatRoom 클래스 생성
6. ChatRoomService에 수집 로직 통합

### Phase 2: smuxapi-demo 데이터 수집 테스트
1. 수집 모드 설정 활성화 후 smuxapi-demo 실행
2. 실제 사용 시나리오 수행
3. JSON 파일 저장 검증

### Phase 3: smart-ux-api 테스트 케이스 클래스 구현
1. ScenarioData, ScenarioTurn 데이터 모델 구현 (`smart-ux-api/lib/src/test/java/.../scenario/`)
2. ScenarioDataLoader 클래스 구현
3. ActionQueueValidator 클래스 구현
4. ValidationResult, ActionDiff, TestResult 클래스 구현
5. ResultWriter 클래스 구현
6. ReportGenerator 클래스 구현
7. FullScenarioTestCase 메인 클래스 구현

### Phase 4: 통합 테스트 및 검증
1. smuxapi-demo에서 수집한 시나리오 데이터를 smart-ux-api 테스트로 복사
2. 테스트 케이스 실행 및 검증
3. 결과 파일 생성 검증
4. 리포트 생성 기능 검증

---

## 5. 기술 스택

### 데이터 수집 (smuxapi-demo)
- **프레임워크**: Spring Boot
- **설정**: YAML (smuxapi-demo.yml)
- **JSON 처리**: Jackson
- **파일 I/O**: Java NIO
- **디자인 패턴**: Decorator 패턴

### 테스트 케이스 (smart-ux-api)
- **실행 방식**: Main 함수 실행 (커맨드라인)
- **JSON 파싱**: Jackson
- **AI API 호출**: 기존 ChatRoom/Chatting 구현체 재사용
- **검증**: Action Queue JSON 비교

---

## 6. 파일 구조

### smuxapi-demo (데이터 수집)
```
smuxapi-demo/
├── src/main/java/com/smartuxapi/demo/
│   ├── collector/
│   │   ├── PromptResponseCollector.java   # 데이터 수집기
│   │   ├── ChattingCollector.java         # Chatting Decorator
│   │   ├── ActionQueueHandlerCollector.java  # Handler Decorator
│   │   └── CollectorChatRoom.java         # ChatRoom Decorator
│   └── service/
│       └── ChatRoomService.java           # 수집 로직 통합
├── src/main/resources/
│   └── smuxapi-demo.yml                   # 수집 설정 추가
└── scenarios/                              # 수집된 시나리오 데이터 저장
    └── test-scenario-*.json
```

### smart-ux-api (테스트 케이스)
```
smart-ux-api/
└── lib/
    └── src/
        └── test/
            ├── java/com/smartuxapi/scenario/
            │   ├── FullScenarioTestCase.java          # 메인 테스트 클래스
            │   ├── ScenarioData.java                  # 시나리오 데이터 모델
            │   ├── ScenarioTurn.java                  # 턴 데이터 모델
            │   ├── ScenarioDataLoader.java            # JSON 로더
            │   ├── validator/
            │   │   ├── ActionQueueValidator.java      # Action Queue 검증
            │   │   ├── ValidationResult.java          # 검증 결과
            │   │   ├── ActionDiff.java                # 차이점 정보
            │   │   └── TestResult.java                # 테스트 실행 결과
            │   └── writer/
            │       ├── ResultWriter.java              # 결과 파일 저장
            │       └── ReportGenerator.java           # 종합 리포트 생성
            └── resources/
                ├── scenarios/                         # 수집된 시나리오 데이터 (smuxapi-demo에서 복사)
                │   └── test-scenario-*.json
                └── test-results/                      # 테스트 실행 결과
                    └── {sessionId}/
                        ├── turn*-response.json
                        └── turn*-action-queue.json
```

---

## 7. 예상 결과물

### 데이터 수집 결과 (smuxapi-demo)
- 실제 사용 시나리오를 반영한 JSON 파일들
- 각 대화 턴별 상세 프롬프트/응답 데이터

### 테스트 케이스 실행 결과 (smart-ux-api)
- **AI 응답 파일**: 각 턴별 AI 응답(res_msg)을 JSON 파일로 저장
  - 파일명: `turn{turn}-response.json`
  - 내용: AI 응답 메시지 전체
- **Action Queue 파일**: 각 턴별 Action Queue를 client 전송 형식으로 저장
  - 파일명: `turn{turn}-action-queue.json`
  - 내용: ActionQueueController가 client에 전송하는 형식과 동일
  - 형식: `{"action_queue": [...]}` (JSONObject 형태)
  - 예시:
    ```json
    {
      "action_queue": [
        {
          "id": "ice_아메리카노",
          "type": "click",
          "action": "click"
        }
      ]
    }
    ```
- **검증 리포트**: Action Queue 검증 결과 (일치/불일치)
- **오류 발생 시나리오 목록**: 검증 실패한 턴 및 오류 내용

---

## 8. 향후 개선 사항

- **자동화**: CI/CD 파이프라인에 테스트 케이스 통합
- **결과 분석**: 수집된 결과 파일들을 분석하여 AI 응답 패턴 분석
- **시각화**: 테스트 결과 대시보드 (Action Queue 추출 성공률 등)
- **확장성**: 다른 AI 모델(Gemini, Assistants) 지원
- **성능 측정**: 응답 시간, Action Queue 추출 시간 등 측정

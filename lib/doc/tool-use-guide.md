# Tool Use Guide (v0.8.0+)

smart-ux-api v0.8.0 은 **LLM Function Calling / Tool Use** 를 Provider 중립 API 로 제공합니다.
OpenAI Responses API 의 `tools`, Gemini 의 `functionDeclarations` 를 단일 인터페이스로 감싸며,
**자동 실행 루프**와 **수동 dispatch** 두 모드를 모두 지원합니다.

---

## 1. 핵심 API

| API | 설명 |
|-----|------|
| `ToolDefinition(name, description, parametersSchema, handler)` | Tool 정의 |
| `ToolRegistry.register(def)` | 등록 |
| `ToolCall` | LLM 이 생성한 호출 요청 (id, toolName, arguments) |
| `ToolResult.ok(callId, output)` / `.error(callId, msg)` | 실행 결과 |
| `Chatting.sendPromptWithTools(msg, registry)` | **자동 루프 모드** (최대 5 라운드) |
| `Chatting.sendPromptExpectingToolCalls(msg, registry)` | **수동 모드** 진입 |
| `Chatting.continueWithToolResults(results, registry)` | 수동 모드 continuation |
| `VisionTools.scanImageTool(vision, aqHandler)` | Vision 을 Tool 로 감싸는 헬퍼 |

---

## 2. 자동 루프 (권장)

```java
import com.smartuxapi.ai.tools.*;
import com.smartuxapi.ai.schema.SchemaBuilder;
import com.smartuxapi.ai.vision.VisionTools;
import com.smartuxapi.ai.vision.VisionServiceFactory;

ChatRoom chat = new ResponsesChatRoom(apiKey, "gpt-4o-mini");
chat.setActionQueueHandler(aqHandler);

ToolRegistry tools = new ToolRegistry();

// Vision 을 Tool 로 등록 — ActionQueueHandler 에 결과 자동 주입
tools.register(VisionTools.scanImageTool(
    VisionServiceFactory.createOpenAI(apiKey), aqHandler));

// 커스텀 Tool 직접 등록
tools.register(new ToolDefinition(
    "lookupProduct",
    "SKU 로 제품 정보를 조회한다.",
    SchemaBuilder.object()
        .stringProperty("sku", "제품 SKU")
        .required("sku")
        .build(),
    call -> {
        String sku = call.getArguments().get("sku").asText();
        // 실제 조회 로직
        JsonNode product = productService.findBySku(sku);
        return ToolResult.ok(call.getId(), product);
    }
));

JSONObject res = chat.getChatting().sendPromptWithTools(
    "오늘 화면 오른쪽 위 빨간 버튼 내용을 읽어줘", tools);

String message = (String) res.get("message");           // 최종 텍스트
JSONArray executedCalls = (JSONArray) res.get("tool_calls");
JsonNode actionQueue = (JsonNode) res.get("action_queue");
```

라이브러리가 `sendPromptWithTools` 안에서:
1. LLM 호출 → 응답에 `function_call` 아이템이 있으면
2. 등록된 handler 를 실행하고
3. 결과를 대화에 추가 후 LLM 재호출
4. 최종 텍스트 응답이 나올 때까지 반복 (최대 `Chatting.DEFAULT_MAX_TOOL_ROUNDS = 5`)

---

## 3. 수동 dispatch

비동기 실행·조건부 실행·사용자 승인이 필요한 경우:

```java
JSONObject res = chat.getChatting().sendPromptExpectingToolCalls(userMsg, tools);

if (Boolean.TRUE.equals(res.get("pending"))) {
    JSONArray pendingCalls = (JSONArray) res.get("tool_calls");

    // 호출자가 직접 실행 (예: 사용자 승인 후, 비동기로, 병렬로 ...)
    List<ToolResult> results = new ArrayList<>();
    for (Object callObj : pendingCalls) {
        JSONObject call = (JSONObject) callObj;
        ToolResult r = executeMyself(
            (String) call.get("id"),
            (String) call.get("toolName"),
            (String) call.get("arguments"));
        results.add(r);
    }

    JSONObject done = chat.getChatting().continueWithToolResults(results, tools);
    // done.message 에 최종 텍스트. 필요하면 반복.
} else {
    // LLM 이 tool 없이 바로 답변한 경우
    String text = (String) res.get("message");
}
```

`continueWithToolResults` 를 다시 호출할 수도 있습니다 (여러 라운드 수동 dispatch).

---

## 4. Tool 정의 상세

### 4.1 parametersSchema

`SchemaBuilder` 로 구성 (권장):
```java
JsonNode params = SchemaBuilder.object()
    .stringProperty("query", "검색어")
    .integerProperty("limit", "최대 반환 수")
    .required("query")
    .build();
```

또는 raw `JsonNode` 직접 (enum/oneOf 등):
```java
JsonNode params = mapper.readTree("""
{ "type":"object",
  "properties": { "level": { "type":"string", "enum":["low","med","high"] } },
  "required": ["level"], "additionalProperties": false }
""");
```

### 4.2 ToolHandler

```java
ToolHandler handler = call -> {
    JsonNode args = call.getArguments();
    // 비즈니스 로직
    return ToolResult.ok(call.getId(), resultNode);
};
```

- 예외를 던지면 라이브러리가 캐치하여 자동으로 `ToolResult.error` 로 변환
- 결과 크기 상한 **256KB** — 초과 시 자동 축약 + 경고 로그

### 4.3 Registry

```java
ToolRegistry reg = new ToolRegistry();
reg.register(def1).register(def2);  // 체이닝
reg.unregister("oldTool");
reg.clear();
```

등록 순서는 보존되며 `all()` 로 iterate 가능.

---

## 5. 반환 JSON 스키마

**자동 루프 — `sendPromptWithTools`**
```json
{
  "message": "<최종 텍스트>",
  "action_queue": {...},
  "tool_calls": [
    { "id": "call_abc", "toolName": "scanImage", "arguments": "{...}", "result": "{...}", "isError": false }
  ]
}
```

**수동 모드 — `sendPromptExpectingToolCalls` / `continueWithToolResults` (pending 상태)**
```json
{
  "message": null,
  "tool_calls": [
    { "id": "call_abc", "toolName": "scanImage", "arguments": "{...}" }
  ],
  "pending": true
}
```

**수동 모드 — 최종 상태**
```json
{
  "message": "<최종 텍스트>",
  "action_queue": {...},
  "tool_calls": []
}
```

---

## 6. 안전장치

| 항목 | 동작 |
|-----|------|
| `max_tool_rounds` | 기본 5. 초과 시 마지막 텍스트 응답 + 경고 로그 (예외 없음) |
| 등록되지 않은 tool 호출 | `ToolResult.error` 로 LLM 에 피드백 → 복구 가능 |
| Handler 예외 | 자동 캐치 → `ToolResult.error(ex.getClass() + msg)` |
| Output 크기 초과 (>256KB) | 자동 축약 + 경고 |

---

## 7. Provider 매핑

### 7.1 OpenAI Responses API
- 요청: `"tools": [{ "type": "function", "name", "description", "parameters" }]`
- 응답: `output[]` 중 `{ "type": "function_call", "call_id", "name", "arguments" }`
- 결과 전송: `{ "type": "function_call_output", "call_id", "output" }`

### 7.2 Gemini
- 요청: `"tools": [{ "functionDeclarations": [{ "name", "description", "parameters" }] }]`
- 응답: `candidates[0].content.parts[].functionCall: { "name", "args" }`
- 결과 전송: `{ "role": "user", "parts": [{ "functionResponse": { "name", "response" } }] }`
- **주의**: Gemini 는 호출 ID 를 응답에 포함하지 않으므로 클라이언트에서 UUID 부여

---

## 8. Vision 통합 (T1-b × T2-b)

`VisionTools.scanImageTool` 은 v0.7.0 Vision 모듈을 Tool 로 감싸는 헬퍼:
- `imageUrl` 인자 하나만 받음
- 내부에서 `VisionService.scanImage(url)` 호출
- `ActionQueueHandler` 가 제공되면 `addImageScanInfo(info)` 자동 호출 (imageUrl 기준 dedupe 포함)
- 결과 JSON 은 `ImageScanInfo.toJSON()` 의 5개 키와 동일 (`imageUrl / extractedText / confidence / timestamp / modelUsed`)

이것으로 두 경로가 공존:
- **경로 A (수동)**: `vision.scanImage(url)` → `aqHandler.addImageScanInfo(info)` 직접 호출
- **경로 B (Tool)**: LLM 이 `scanImage` 를 판단하여 자동 호출 → 같은 결과가 `aqHandler` 에 주입됨

---

## 9. 하위 호환

- `sendPrompt(msg)` 기존 경로는 그대로. `tool_calls` 키 추가되지 않음.
- 모든 신규 메서드는 `Chatting` 인터페이스의 `default` 메서드 — 기존 구현체 무수정 동작.
- `tools=null` 또는 빈 Registry 전달 시 `sendPrompt` 로 위임됨.

---

## 10. 관련 문서
- `caching-guide.md` — Prompt Caching (v0.7.0)
- `vision-guide.md` — Vision API (v0.7.0)
- `structured-output-guide.md` — Structured Output (v0.8.0 T2-a)
- `doc/tasks/20260421_tool_use_design_sketch.md` — 설계 스케치 (로컬)

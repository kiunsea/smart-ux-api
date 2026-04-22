# Structured Output Guide (v0.8.0+)

smart-ux-api v0.8.0 은 **JSON Schema 를 강제하는 구조화 응답 API** 를 제공합니다.
LLM 응답을 항상 정해진 스키마의 JSON 으로 받아 파싱 오류·후처리 복잡도를 줄입니다.

---

## 1. 핵심 API

| API | 설명 |
|-----|------|
| `ResponseSchema.of(name, schemaNode)` | 이미 만들어진 JsonNode 로부터 스키마 구성 |
| `ResponseSchema.object()` / `SchemaBuilder.object()` | 편의 빌더 진입점 (object 루트) |
| `SchemaBuilder.stringProperty / integerProperty / booleanProperty / arrayProperty / objectProperty` | 기본 5 타입 |
| `SchemaBuilder.required(...)` | 필수 필드 지정 |
| `SchemaBuilder.asResponse(name)` | `ResponseSchema` 로 래핑 |
| `Chatting.sendPromptWithSchema(userMsg, schema)` | **신규** — 구조화 응답 전송 |

---

## 2. 사용 예 — OpenAI

```java
import com.smartuxapi.ai.schema.ResponseSchema;
import com.smartuxapi.ai.schema.SchemaBuilder;

ChatRoom chat = new ResponsesChatRoom(apiKey, "gpt-4o-mini");

ResponseSchema userSchema = SchemaBuilder.object()
    .stringProperty("name", "사용자 이름")
    .integerProperty("age", "나이")
    .stringArrayProperty("hobbies", "취미 목록")
    .required("name", "age")
    .asResponse("UserProfile");

JSONObject res = chat.getChatting().sendPromptWithSchema(
    "Alice 는 30세, 취미는 등산과 독서입니다. 프로필을 JSON 으로 추출하세요.",
    userSchema);

// 반환 포맷
String rawJson = (String) res.get("message");       // provider 원문 (JSON 문자열)
JsonNode structured = (JsonNode) res.get("structured");  // 파싱된 JsonNode (실패 시 null)

if (structured != null) {
    System.out.println(structured.get("name").asText());     // "Alice"
    System.out.println(structured.get("age").asInt());        // 30
    System.out.println(structured.get("hobbies").get(0));     // "등산"
}
```

---

## 3. 사용 예 — Gemini

동일 API, 동일 스키마를 그대로 재사용:

```java
ChatRoom chat = new GeminiChatRoom(apiKey, "gemini-1.5-pro");

// 위에서 만든 userSchema 그대로 사용
JSONObject res = chat.getChatting().sendPromptWithSchema(
    "Alice 는 30세, 취미는 등산과 독서입니다.",
    userSchema);

JsonNode structured = (JsonNode) res.get("structured");
```

---

## 4. raw JsonNode 경로 (고급)

`SchemaBuilder` 가 지원하지 않는 스키마 (e.g. `enum`, `oneOf`, `pattern`) 가 필요하면
직접 `JsonNode` 를 구성:

```java
ObjectMapper mapper = new ObjectMapper();
JsonNode schemaNode = mapper.readTree("""
{
  "type": "object",
  "properties": {
    "status": { "type": "string", "enum": ["active", "pending", "done"] }
  },
  "required": ["status"],
  "additionalProperties": false
}
""");

ResponseSchema schema = ResponseSchema.of("StatusResult", schemaNode);
```

---

## 5. 반환 포맷 상세

| 키 | 타입 | 설명 |
|----|------|------|
| `message` | `String` | Provider 응답 원문 (schema 지정 시 JSON 문자열) |
| `action_queue` | `JsonNode` | 기존 ActionQueueHandler 경로 — schema 와 무관 |
| `structured` | `JsonNode` | 파싱된 JSON. **파싱 실패 시 null** (예외 없음) |

`structured` 키는 schema 를 지정한 경우에만 존재. `sendPrompt(msg)` 나
`sendPromptWithSchema(msg, null)` 는 키를 추가하지 않습니다.

---

## 6. Provider 매핑

### 6.1 OpenAI Responses API
- 요청: `text.format = { type: "json_schema", name, strict, schema }`
- strict 플래그 기본 `true`. OpenAI 는 미지원 필드(`$ref`, `allOf` 등) 가 포함되면 요청 단계에서 거부.

### 6.2 Gemini
- 요청: `generationConfig.responseMimeType = "application/json"` + `generationConfig.responseSchema`
- Gemini 는 OpenAI JSON Schema 의 **서브셋**만 지원 — `$ref`, `anyOf`, `not` 등 미지원. 초과 시 API 가 거부.

---

## 7. 주의사항

1. **Schema 이름 필수** — OpenAI strict 모드에서 식별자로 사용됨. `SchemaBuilder.asResponse("...")` 또는 `ResponseSchema.of("...", schema)` 에서 지정.
2. **`additionalProperties: false` 자동 주입** — `SchemaBuilder` 경로. raw JsonNode 경로는 **호출자가 직접 포함** 해야 strict 모드에서 동작.
3. **파싱 실패는 예외가 아님** — `structured = null` + WARN 로그. 호출자가 raw `message` 를 원하면 그대로 사용 가능.
4. **캐시/Vision 과 독립** — `markAsCacheable`, `addImageScanInfo` 와 조합 가능. schema 는 매 호출마다 지정.
5. **토큰 사용량** — structured output 은 JSON 구조 유지를 위해 종종 원문보다 길어질 수 있음. 프롬프트에서 값 길이 제약을 명시 권장.

---

## 8. 하위 호환

`sendPrompt(String)` 기존 호출 경로는 **그대로 유지** — `structured` 키 추가 없음.
`Chatting.sendPromptWithSchema` 는 default 메서드로 추가되어 기존 smuxapi-demo, doribox 코드는 무수정 동작.

---

## 9. 관련 문서
- `caching-guide.md` — Prompt Caching (v0.7.0)
- `vision-guide.md` — Vision API (v0.7.0)
- `doc/tasks/20260422_structured_output_design_sketch.md` — 설계 스케치 (로컬)

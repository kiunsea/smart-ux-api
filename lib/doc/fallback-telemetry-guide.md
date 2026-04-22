# Cross-provider Fallback + Cost Telemetry Guide (v0.9.1+)

smart-ux-api v0.9.1 은 **공급자 간 자동 fallback** 과 **비용 telemetry** 를 제공합니다.
OpenAI 가 일시적으로 실패해도 Gemini 로 자동 재시도하고, 모든 호출의 토큰/USD 비용이 누적됩니다.

---

## 1. 핵심 API

### Fallback

| API | 설명 |
|-----|------|
| `FallbackPolicies.openaiPrimaryGeminiFallback(openai, gemini)` | 가장 흔한 프리셋 |
| `FallbackPolicies.geminiPrimaryOpenaiFallback(gemini, openai)` | 역방향 |
| `FallbackPolicy.builder()` | 커스텀 chain + trigger reason 설정 |
| `new FallbackChatRoom(policy)` | `ChatRoom` 데코레이터 — 기존 코드에 투명 적용 |
| `FallbackExhaustedException` | 모든 slot 실패 시 던져지는 예외 |

### Cost Telemetry

| API | 설명 |
|-----|------|
| `CostTracker.INSTANCE` | 전역 싱글톤 — 모든 호출 자동 기록 |
| `CostTracker.INSTANCE.getSummary()` | 합계 집계 |
| `CostTracker.INSTANCE.getSummary(predicate)` | 필터 적용 집계 |
| `CostTracker.INSTANCE.getEntries()` | 개별 entry 스냅샷 |
| `CostTable.register(model, inputPer1M, outputPer1M)` | 런타임 단가 추가/override |
| `CostTracker.INSTANCE.setMaxEntries(N)` | FIFO ring buffer 전환 |
| `CostTracker.INSTANCE.setEnabled(false)` | 수집 중단 |

---

## 2. 기본 사용 — OpenAI → Gemini Fallback

```java
import com.smartuxapi.ai.ChatRoom;
import com.smartuxapi.ai.openai.ResponsesChatRoom;
import com.smartuxapi.ai.gemini.GeminiChatRoom;
import com.smartuxapi.ai.fallback.FallbackChatRoom;
import com.smartuxapi.ai.fallback.FallbackPolicies;

ChatRoom openai = new ResponsesChatRoom(openaiKey, "gpt-4o-mini");
ChatRoom gemini = new GeminiChatRoom(geminiKey, "gemini-1.5-flash");

ChatRoom chat = new FallbackChatRoom(
    FallbackPolicies.openaiPrimaryGeminiFallback(openai, gemini));

// 기존 ChatRoom 처럼 사용 — 투명하게 fallback 적용
JSONObject res = chat.getChatting().sendPrompt("안녕");
```

OpenAI 가 5xx / 429 / 타임아웃 / 네트워크 오류를 반환하면 자동으로 Gemini 로 재시도.
UNAUTHORIZED(401/403) 은 기본 trigger 에 포함되지 않음 (설정 오류 가능성 — 호출자 판단).

---

## 3. 커스텀 정책

```java
import com.smartuxapi.ai.fallback.*;

FallbackPolicy policy = FallbackPolicy.builder()
    .addSlot(new OpenAiSlot(openai))
    .addSlot(new GeminiSlot(gemini))
    .triggerReasons(
        FailureReason.TIMEOUT,
        FailureReason.RATE_LIMIT,
        FailureReason.UNAUTHORIZED)  // UNAUTHORIZED 추가
    .logOnFallback(true)              // WARN 로그
    .build();

ChatRoom chat = new FallbackChatRoom(policy);
```

`FailureReason` 분류:
- `TIMEOUT` — SocketTimeoutException, 메시지에 "timeout"
- `RATE_LIMIT` — HTTP 429, 메시지에 "rate"
- `SERVER_ERROR` — HTTP 5xx
- `NETWORK_ERROR` — UnknownHostException, ConnectException, IOException
- `UNAUTHORIZED` — HTTP 401/403 (기본 trigger 미포함)
- `UNKNOWN` — 분류 불가 (기본 trigger 미포함)

---

## 4. Tool Use / Structured / Cache 와 통합

```java
ChatRoom chat = new FallbackChatRoom(policy);
chat.setActionQueueHandler(aqHandler);             // 모든 slot 에 broadcast
chat.markAsCacheable(CacheHint.of(largePrefix));   // 모든 slot 에 prime

JSONObject res = chat.getChatting().sendPromptWithTools(msg, tools);
// OpenAI 로 tool_call → 실패 → Gemini 로 전체 호출 재시도
// (한 round 중간에 전환하지 않음 — 대화 일관성)
```

- **Tool Use**: `sendPromptWithTools`, `sendPromptExpectingToolCalls`, `continueWithToolResults` 모두 동일 chain 적용. 단, auto-loop 의 round 중간에는 전환하지 않음.
- **Structured Output**: `sendPromptWithSchema` 도 동일.
- **Cache**: `markAsCacheable` 은 모든 slot 에 broadcast.
- **ActionQueueHandler / DebugLogger**: 모든 slot 공유.

---

## 5. 대화 히스토리 독립성

중요: 각 slot 의 ChatRoom 은 **자신의 대화 히스토리** 만 관리합니다.
Fallback 이 발동한 턴은 성공한 slot 의 히스토리에만 기록됩니다.

예:
- Turn 1: OpenAI 성공 → OpenAI 히스토리에 기록
- Turn 2: OpenAI 5xx → Gemini 재시도 성공 → Gemini 히스토리에 기록 (Turn 1 없음)
- Turn 3: OpenAI 다시 성공 → OpenAI 히스토리에 Turn 3 추가 (Turn 2 빠짐)

이는 단순화를 위한 설계. 대화 일관성이 중요하면 primary 만 쓰거나, 양 slot 에 수동으로 sync.

---

## 6. Cost Telemetry — 자동 기록

`CostTracker.INSTANCE` 에 **모든 API 호출이 자동 기록**됩니다 — Fallback 여부와 무관.
기존 코드 수정 불필요 — smart-ux-api v0.9.1 부터 기본 동작.

```java
import com.smartuxapi.ai.cost.CostTracker;
import com.smartuxapi.ai.cost.CostSummary;

// 여러 호출 수행...
chat.getChatting().sendPrompt("hi");
chat.getChatting().sendPromptWithTools(msg, tools);
embeddingService.embed(text);

// 집계 조회
CostSummary s = CostTracker.INSTANCE.getSummary();
System.out.printf("토큰: %d in + %d out, 비용: $%.6f%n",
    s.getTotalInputTokens(), s.getTotalOutputTokens(), s.getTotalUsd());

// provider 별 비용
s.getByProvider().forEach((p, usd) -> System.out.println(p + ": $" + usd));

// callKind 별 비용
s.getByCallKind().forEach((k, usd) -> System.out.println(k + ": $" + usd));
```

### callKind 자동 분류
- `chat` — `sendPrompt`
- `structured` — `sendPromptWithSchema`
- `tool_use` — `sendPromptWithTools`, `sendPromptExpectingToolCalls`, `continueWithToolResults`
- `vision` — `VisionService.scanImage` / `extractText` / `extractTextFromBase64`
- `embedding` — `EmbeddingService.embed` / `embedBatch`

### Fallback 기록
`CostEntry.isFallbackTriggered()` 는 **현재 false 로 고정** 기록됩니다 — 향후 FallbackChatRoom 레벨에서 세팅 예정.
집계의 `fallbackCount` 는 정확히 fallback 이 발동한 호출 수를 반영 (현재는 수동 설정 필요).

---

## 7. 필터링된 집계

```java
// OpenAI 비용만
CostSummary openaiOnly = CostTracker.INSTANCE.getSummary(
    e -> "openai".equals(e.getProvider()));

// 최근 1시간 비용
long cutoff = System.currentTimeMillis() - 3600_000;
CostSummary lastHour = CostTracker.INSTANCE.getSummary(
    e -> e.getTimestampMs() >= cutoff);

// Vision 호출만
CostSummary visionOnly = CostTracker.INSTANCE.getSummary(
    e -> "vision".equals(e.getCallKind()));
```

---

## 8. 가격표 관리

### 기본 제공 모델 (2026-Q2 기준)

| Model | input USD/1M | output USD/1M |
|-------|--------------|---------------|
| gpt-4o-mini | 0.15 | 0.60 |
| gpt-4o | 2.50 | 10.00 |
| gpt-4.1 | 3.00 | 12.00 |
| gpt-4.1-mini | 0.40 | 1.60 |
| gemini-1.5-flash | 0.075 | 0.30 |
| gemini-2.5-flash | 0.075 | 0.30 |
| gemini-1.5-pro | 1.25 | 5.00 |
| text-embedding-3-small | 0.02 | 0 |
| text-embedding-3-large | 0.13 | 0 |
| text-embedding-004 | 0 | 0 |

### 미등록 모델
미등록 모델 사용 시 비용 0 으로 기록 + WARN 로그. 필요하면 `CostTable.register` 로 override:

```java
CostTable.register("custom-model-v2", 1.0, 4.0);
```

---

## 9. 메모리 관리

```java
// 무한 누적 (기본)
CostTracker.INSTANCE.setMaxEntries(0);

// 최근 10000건만 유지 (FIFO)
CostTracker.INSTANCE.setMaxEntries(10000);

// 리셋
CostTracker.INSTANCE.reset();

// telemetry 끄기
CostTracker.INSTANCE.setEnabled(false);
```

---

## 10. 독립 인스턴스 (테스트/세션 격리)

```java
import com.smartuxapi.ai.cost.CostTracker;

CostTracker sessionTracker = new CostTracker();
// 단, APIConnection 은 INSTANCE 에만 기록 — 독립 인스턴스는 수동 record 용
sessionTracker.record(new CostEntry(...));
```

---

## 11. 주의사항

- **APIConnection 의 CostTracker 기록은 고정적으로 `CostTracker.INSTANCE` 사용** — 독립 인스턴스로 redirect 옵션은 v0.9.x 이후.
- **Fallback 정보** — 자동 기록 시 `isFallbackTriggered=false`. Fallback 여부를 정확히 알려면 `CostEntry.getProvider()` 를 호출 전 primary 와 비교.
- **Cache 메트릭 기록 실패** — 기존 cache 메트릭 기록은 유지. CostTracker 와 독립.
- **성능**: 기록은 synchronized — 대용량 스트리밍엔 부적합 (batch 호출 위주 권장).

---

## 12. 관련 문서
- `caching-guide.md` — Prompt Caching (v0.7.0)
- `vision-guide.md` — Vision API (v0.7.0 + v0.8.1 Gemini)
- `structured-output-guide.md` — Structured Output (v0.8.0)
- `tool-use-guide.md` — Tool Use (v0.8.0)
- `embeddings-guide.md` — Embeddings (v0.9.0)
- `doc/tasks/20260422_fallback_telemetry_design_sketch.md` — 설계 스케치 v1.0 (로컬)

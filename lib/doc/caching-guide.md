# Prompt Caching Guide (v0.7.0+)

smart-ux-api v0.7.0 은 Provider 중립 프롬프트 캐시 API 를 제공합니다.
대형 UI Object Map, 시스템 지침 등 **안정적으로 재사용되는 콘텐츠**를 힌트로 등록하면
OpenAI 는 자동 프리픽스 캐싱, Gemini 는 명시적 context caching 을 통해 토큰 비용이 최대 90%
절감됩니다.

---

## 1. 핵심 API

| API | 설명 |
|-----|------|
| `CacheHint.of(content)` | 기본 힌트 생성 (TTL 3600초) |
| `CacheHint.of(content, label)` | 로깅용 라벨 포함 |
| `CacheHint.withTtl(content, ttlSec)` | Gemini 용 TTL 지정 |
| `ChatRoom.markAsCacheable(hint)` | 이후 모든 sendPrompt 가 힌트를 프리픽스로 사용 |
| `ChatRoom.setCacheStrategy(strategy)` | 커스텀 전략 주입 (기본값 제공) |
| `ChatRoom.getLastCacheMetrics()` | 마지막 호출의 캐시 히트율 조회 |

---

## 2. OpenAI (자동 프리픽스 캐싱)

```java
ChatRoom chat = new ResponsesChatRoom(apiKey, "gpt-4o-mini");

// 안정적인 프리픽스 등록 (로컬 작업 — 네트워크 호출 없음)
String uiObjectMap = loadUiObjectMap(); // 예: 50KB JSON
chat.markAsCacheable(CacheHint.of(uiObjectMap, "ui-object-map"));

// 반복 호출 시 프리픽스가 자동 캐시됨
Chatting chatting = chat.getChatting();
for (String userMsg : userMessages) {
    JSONObject resp = chatting.sendPrompt(userMsg);
    CacheMetrics m = chat.getLastCacheMetrics();
    log.info("cache hit: {} / {} tokens ({:.1f}%)",
             m.getCachedInputTokens(),
             m.getTotalInputTokens(),
             m.getHitRate() * 100);
}
chat.close();
```

### OpenAI 측 동작

- 응답 본문에서 `usage.prompt_tokens_details.cached_tokens` 를 파싱하여 메트릭 제공
- 프리픽스는 `role="system"` 메시지로 대화 시작부에 배치됨 (매 요청)
- 내부 `ConversationHistory` 는 프리픽스를 **누적 저장하지 않음** — 매 요청 시 조립만 수행

### OpenAI 캐시 히트 조건

- 프리픽스 길이가 1024 토큰 이상이어야 자동 캐싱 대상
- 동일 프리픽스를 **수 분 이내** 재사용해야 히트 (TTL 은 OpenAI 가 관리)
- 프리픽스 중간 글자가 바뀌면 캐시 무효

---

## 3. Gemini (명시적 context caching)

```java
ChatRoom chat = new GeminiChatRoom(apiKey, "gemini-1.5-flash");

// 힌트 등록 시 네트워크 호출 발생 — cachedContents 리소스 생성
String largeContext = loadLargeReferenceDoc(); // 32,768 토큰 이상 권장
chat.markAsCacheable(CacheHint.withTtl(largeContext, 3600));

// 이후 모든 sendPrompt 는 생성된 캐시 리소스를 참조
Chatting chatting = chat.getChatting();
JSONObject resp = chatting.sendPrompt("이 문서를 3줄로 요약해줘");

CacheMetrics m = chat.getLastCacheMetrics();
log.info(m.toString());

// close() 시 캐시 리소스 자동 삭제 (TTL 만료 대신 명시적 해제)
chat.close();
```

### ⚠️ Gemini 캐시 사용 조건

- **최소 토큰 수**: 모델별로 다르며 일반적으로 32,768 토큰 이상. 그 미만이면 `prime()` 실패
- **TTL 필수**: 기본 3600초, `CacheHint.withTtl(content, seconds)` 로 조절
- 캐시 생성 실패 시 `Exception` 이 던져지므로, 호출자가 `try/catch` 로 no-cache fallback 을 선택할 수 있음

---

## 4. 캐시 전략 교체

기본 전략 대신 커스텀 구현으로 교체 가능 (예: 모킹, 로깅 wrapping):

```java
chat.setCacheStrategy(NoOpCacheStrategy.INSTANCE); // 캐시 완전 비활성화
// 또는
chat.setCacheStrategy(myInstrumentedStrategy);
```

---

## 5. 권장 패턴

### 5.1 ActionQueueHandler 와 결합

`ActionQueueHandler` 의 `curViewInfo` 가 자주 변하는 반면 UI Object Map 은 거의 변하지 않습니다.
UI Object Map 만 `markAsCacheable` 로 등록하면, 화면 변경이 캐시 히트율을 깎지 않습니다.

```java
chat.setActionQueueHandler(aqHandler);
chat.markAsCacheable(CacheHint.of(uiObjectMapJson, "ui-map"));
// 이후 aqHandler.setCurrentViewInfo(...) 로 자주 바뀌는 부분 갱신
```

### 5.2 히트율 모니터링

`ChatRoom.getLastCacheMetrics()` 를 모든 호출 후 기록하여 히트율 회귀를 감지하세요.
평균 히트율이 70% 이하로 떨어지면 프리픽스 콘텐츠가 불안정하다는 신호입니다.

### 5.3 Gemini TTL 과 close() 타이밍

Gemini 캐시 리소스는 **요청 건당 과금이 아니라 저장 시간 과금**입니다.
오래 유지할수록 비용이 누적되므로, 사용이 끝나면 `chat.close()` 로 즉시 해제하는 것을 권장합니다.

---

## 6. 관련 문서

- 설계 배경: [`doc/tasks/20260421_tool_use_design_sketch.md`](./tasks/20260421_tool_use_design_sketch.md)
- doribox 측 활용 방향: `doribox/doc/20260421-doribox-smartuxapi-upgrade-directions.md` 항목 B3

---

## 7. 변경 이력

| 버전 | 변경 |
|------|------|
| 0.7.0 | 초기 도입 — OpenAI 자동 프리픽스 + Gemini context caching |

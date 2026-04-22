# Session Handoff — v0.8.0 Release Work

**작성일**: 2026-04-22 (v0.7.0 핸드오프 당일)
**작성자**: Claude (Opus 4.7, 1M context) + 사용자
**용도**: v0.8.0 (Structured Output + Tool Use) 릴리스 상태와 다음 단계 안내

> 이전 문서: `session-handoff-20260422.md` (v0.7.0 릴리스 기준)

---

## 1. 상태 스냅샷

### 브랜치
| 브랜치 | HEAD | 원격 | 설명 |
|---|---|---|---|
| `main` | `2950061` | ✅ | v0.8.0 머지 완료 (Structured Output + Tool Use) |
| `feature/v0.8.0-structured-output` | `3e07aaf` | ✅ | T2-a 단독 |
| `feature/v0.8.0-tool-use` | `b9c34dc` | ✅ | T2-b (T2-a 위에 분기) |
| `release/v0.8.0` | `b09a792` | ✅ | 통합 릴리스 브랜치 + 버전 bump |
| 태그 | `v0.8.0` | ✅ | main HEAD (2950061) 에 생성/push |

### 테스트 결과 (main 기준)
- **358 tests** / 338 pass / 20 skip (integration — API 키 요구) / **0 fail**
- Gradle 8.10.2 / Java 17 / JUnit 5.10.1 / Mockito 5.11.0

### 빌드 산출물
- JAR: `smart-ux-api-0.8.0.jar` (약 0.11 MB)
- 배포 완료: `doribox/libs/smart-ux-api-0.8.0.jar` (v0.7.0 자동 삭제 후 복사)

---

## 2. 이번 세션에서 완료한 작업

### v0.7.0 릴리스 (세션 초반)
- 로컬 배포 → 원격 push → PR [#7](https://github.com/kiunsea/smart-ux-api/pull/7) 머지 → 태그 `v0.7.0` 생성/push

### T2-a: Provider 중립 Structured Output (v0.8.0)
- 신규 패키지 `com.smartuxapi.ai.schema`: `ResponseSchema`, `SchemaBuilder`
- OpenAI `text.format = { type: "json_schema", ... }` 매핑
- Gemini `generationConfig.responseMimeType + responseSchema` 매핑
- `Chatting.sendPromptWithSchema(userMsg, schema)` default 메서드
- 응답 원문 자동 파싱 → `structured` 키 (JsonNode). 파싱 실패는 예외 없이 WARN + null
- 가이드: `doc/structured-output-guide.md`
- 테스트 +22건

### T2-b: Provider 중립 Tool Use / Function Calling (v0.8.0)
- 신규 패키지 `com.smartuxapi.ai.tools`:
  - `ToolDefinition`, `ToolHandler` (FI), `ToolCall`, `ToolResult`, `ToolRegistry`, `ToolTurnResult`
- Auto loop: `Chatting.sendPromptWithTools(msg, registry)` — 최대 5 라운드
- Manual dispatch: `sendPromptExpectingToolCalls` + `continueWithToolResults`
- OpenAI Responses API `tools` + Gemini `functionDeclarations` 양방향 구현
- 안전장치:
  - 미등록 tool 호출 → `ToolResult.error` (LLM 복구 가능)
  - Handler 예외 자동 캐치 → error
  - Output 크기 상한 256KB — 초과 시 자동 축약
  - max_tool_rounds 초과 → 마지막 텍스트 응답 + 경고 (예외 없음)
- `ConversationHistory` (양쪽 provider) 에 tool_call/tool_result 아이템 지원
- `ChatRoom.setToolRegistry/getToolRegistry` default 메서드
- 가이드: `doc/tool-use-guide.md`
- 테스트 +25건

### VisionTools.scanImageTool (T1-b × T2-b 통합)
- 신규 `com.smartuxapi.ai.vision.VisionTools` 헬퍼
- `scanImageTool(vision, aqHandler)` — imageUrl 인자만 받는 Tool 정의
- `ActionQueueHandler` 주입 시 결과 자동 주입 (imageUrl 기준 dedupe 유지)
- 출력 JSON = `ImageScanInfo.toJSON()` 5키 그대로 (T2-b 스케치 §6.3 준수)
- 테스트 5건

### 릴리스 준비
- `lib/build.gradle.kts` version `0.7.0` → `0.8.0`
- `CHANGELOG.md` 에 `[0.8.0] - 2026-04-22` 엔트리 추가

---

## 3. 이번 세션에서 합의된 결정

### 로드맵 업데이트
```
[✅] v0.7.0 — T1-a Prompt Caching + T1-b Vision
[✅] v0.8.0 — T2-a Structured Output + T2-b Tool Use
[ ] v0.9.x — T3-a Embeddings, T3-b Cross-provider Fallback/Cost Telemetry
```

### T2-a 설계 스케치 (v1.0 확정)
문서: `doc/tasks/20260422_structured_output_design_sketch.md` (로컬)

| Q | 결정 |
|---|------|
| Q1: 신규 메서드 vs 오버로드 | 신규 메서드 `sendPromptWithSchema` |
| Q2: 반환 포맷 | B — 기존 키 유지 + `structured` 필드 병기 |
| Q3: 파싱 실패 | null + 원문 유지 + WARN 로그 (예외 없음) |
| Q4: SchemaBuilder enum 포함? | 미포함 — raw JsonNode 로 커버 |
| Q5: OpenAI strict 기본값 | true |
| Q6: `ResponseSchema.name` 필수? | 필수 |

### T2-b 설계 스케치 (v1.0 확정)
기존 문서: `doc/tasks/20260421_tool_use_design_sketch.md` (로컬) — 그대로 준수

### 브랜치 전략
v0.7.0 패턴 그대로:
- feature 브랜치 2개 (T2-a / T2-b)
- T2-b 는 T2-a 브랜치 위에 분기 (SchemaBuilder 의존)
- release/v0.8.0 에 모두 머지 + 버전 bump + CHANGELOG

---

## 4. 다음 액션 후보

### A. v0.8.0 검증
1. **doribox 소비처 연동 테스트** — 새 API 가 기존 호출 경로에 영향 없는지 재확인
2. **Vision Tool 실측 검증** — `VisionTools.scanImageTool` 로 자동 호출 경로가 실제 UX 에서 어떻게 동작하는지 확인
3. **Gemini 수동 dispatch 한계** — `continueWithToolResults` 가 Gemini 에서 tool 이름을 registry 첫 번째로 추정하는 부분 확인 (호출자가 한 번에 한 tool 씩만 쓰면 안전)

### B. v0.9.x 착수 (Tier 3)
1. **T3-a Embeddings** — OpenAI `text-embedding-3-*` + Gemini `text-embedding-004`
   - 용도: UI Object Map 의미 검색, 사용자 의도 → 액션 매칭
2. **T3-b Cross-provider Fallback** — 같은 요청을 OpenAI 실패 시 Gemini 로 자동 재시도
   - Cost Telemetry: 토큰 사용량/비용 누적 리포트
3. **브랜치**: `feature/v0.9.0-embeddings`, `feature/v0.9.0-cross-provider`

### C. 보조 작업
1. **GeminiChattingToolUseTest** — T2-b 의 Gemini 쪽 auto-loop 를 Mockito 로 검증 (현재 OpenAI 만 통합 테스트 있음)
2. **Gemini Vision 구현** — `GeminiVisionService` (T1-b 보강)
3. **doribox A1 (Self-healing selector), A3 (`extract_by_intent`) 스케치**

---

## 5. 주의사항

### Tool Use 제약
- **max_tool_rounds 기본 5** — 필요 시 상수 override 고려 (현재 상수)
- **Gemini 호출 ID** — 응답이 ID 를 포함하지 않아 클라이언트가 UUID 부여. 호출자가 ID 에 의존한 외부 추적을 하면 매 호출마다 달라짐
- **Manual mode Gemini** — `continueWithToolResults` 가 tool 이름을 registry 첫 번째로 추정 (Result 에는 name 이 없음). 실용상 한 번에 한 tool 씩 처리 권장

### Structured Output 제약
- **Gemini schema subset 제한** — `$ref`, `anyOf`, `not` 미지원. 초과 시 provider 오류 그대로 전파
- **strict 모드** — `SchemaBuilder` 는 `additionalProperties: false` 자동 주입. raw JsonNode 경로는 호출자 책임

### 배포 / 하위 호환
- 모든 신규 메서드는 `default` — smuxapi-demo, doribox 기존 코드 무수정 동작
- `sendPrompt(msg)` 기존 반환 포맷 (`message`, `action_queue`) 유지. 신규 키 (`structured`, `tool_calls`) 는 신규 메서드에서만 추가됨

---

## 6. 핵심 문서 인덱스

1. **이 문서** — v0.8.0 릴리스 맥락
2. `CHANGELOG.md` — `[0.8.0] - 2026-04-22` 공식 기록
3. `doc/structured-output-guide.md` — T2-a 사용법
4. `doc/tool-use-guide.md` — T2-b 사용법
5. `doc/caching-guide.md` — T1-a (v0.7.0)
6. `doc/vision-guide.md` — T1-b (v0.7.0)
7. `doc/tasks/20260422_structured_output_design_sketch.md` — T2-a 스케치 v1.0 (로컬)
8. `doc/tasks/20260421_tool_use_design_sketch.md` — T2-b 스케치 v1.0 (로컬)
9. `doc/working/session-handoff-20260422.md` — v0.7.0 핸드오프 (이전)

---

## 7. 종료 시점 체크리스트

- [x] 358 테스트 통과 (0 fail)
- [x] 빌드 성공 (compileJava)
- [x] 버전 bump (0.8.0)
- [x] CHANGELOG 엔트리 작성
- [x] 로컬 배포 (`doribox/libs/smart-ux-api-0.8.0.jar`)
- [x] 원격 push (release + 2 feature branches)
- [x] GitHub PR [#8](https://github.com/kiunsea/smart-ux-api/pull/8) 머지
- [x] 태그 `v0.8.0` 생성/push
- [x] 핸드오프 문서 작성 (이 문서)

---

**세션 마감 기준**: 모든 체크박스 ✅ — v0.8.0 완전 배포 완료.

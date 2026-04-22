# 📋 Changelog

모든 변경 사항은 이 문서에 기록됩니다.

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따르며,  
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.
  - Major: 하위 버전과 호환되지 않는 변화가 생겼을 때 증가
  - Minor: 하위 버전과 호환되면서 새로운 기능이 추가될 때 증가
  - Patch: 기존 버전과 호환되면서 버그를 수정한 것일 때 증가
  
---
## [0.9.3] - 2026-04-22

### Docs
- **루트 README.md**: JAR 경로 `smart-ux-api/lib/build/libs/smart-ux-api-0.6.2.jar` → `lib/build/libs/smart-ux-api-0.9.3.jar` (4곳). refactor 후 2단 구조 반영 + 최신 버전
- **lib/README.md**: JAR 참조 `smart-ux-api-0.6.1.jar` → `smart-ux-api-0.9.3.jar`, 경로 `smart-ux-api/lib/build/libs/` → `lib/build/libs/`
- **docs/INSTALL.md**: 빌드 산출물 확인/복사/Gradle 의존성 예제 3곳의 경로·버전 갱신
- **lib/doc/API.md**: JAR 통합 예제의 버전 갱신

### Notes
- 기능/API 변경 없음. 문서 정합성만 확보

---
## [0.9.2] - 2026-04-23

### Changed
- **프로젝트 구조 재편** (repo-level refactor)
  - 기존 `smart-ux-api/smart-ux-api/lib/` 3단 중첩(gradle init 스캐폴드) 구조에서 `smart-ux-api/lib/` 2단 구조로 평탄화
  - 루트에 `settings.gradle.kts` + `build.gradle.kts` 신설 — doribox 동일 패턴의 multi-project (`include lib`, `include smuxapi-demo`)
  - wrapper, gradle.properties, CHANGELOG, README, doc, bat/deploy_to_doribox 등 모두 루트로 승격 (git mv 로 히스토리 보존)
  - Eclipse 추적 파일(.classpath, .project, .settings/) 전부 제거
  - `smuxapi-demo/settings.gradle.kts` 삭제 → 루트로 통합 (`project(":lib")` 의존은 multi-project 자동 해결)
  - `smuxapi-war`(구버전 이름) 잔재 참조 정리
- **CI/Release 경로 갱신**: ci.yml/release.yml/deploy-docs.yml 의 `working-directory: ./smart-ux-api` 제거, `smart-ux-api/lib/...` → `lib/...`
- **create-release.sh/.bat** JAR/CHANGELOG 경로 갱신
- **bat/deploy_to_doribox.bat** 위치 이동 + 경로 로직 정비

### Fixed
- 깨진 include 참조 (`include("smuxapi-war")` / `../smuxapi-war` — 실제 폴더 없음) 제거

### Notes
- 기능/API 변경 없음. 코드 동작은 v0.9.1 과 완전히 동일
- Eclipse에서 repo 루트 1회 import 로 lib + smuxapi-demo 자동 포함

---
## [0.9.1] - 2026-04-23

### Added
- **Cross-provider Fallback** (T3-b)
  - `com.smartuxapi.ai.fallback`: `FallbackChatRoom` (데코레이터), `FallbackPolicy`, `FallbackPolicies` (프리셋), `ProviderSlot` + `OpenAiSlot` / `GeminiSlot`, `FailureReason` (6종 분류), `FallbackExhaustedException`
  - OpenAI primary → Gemini fallback / 역방향 프리셋
  - 기본 trigger: `TIMEOUT` / `RATE_LIMIT` / `SERVER_ERROR` / `NETWORK_ERROR` (UNAUTHORIZED/UNKNOWN 제외)
  - 호출 단위 전환 — Tool Use auto-loop 의 round 중간에는 전환하지 않음 (대화 일관성)
  - 모든 slot 에 `ActionQueueHandler` / `DebugLogger` / `CacheStrategy` / `ToolRegistry` broadcast
- **Cost Telemetry**
  - `com.smartuxapi.ai.cost`: `CostTracker` (INSTANCE 싱글톤 + 독립 instance), `CostEntry`, `CostSummary`, `CostTable` (정적 단가 + 런타임 override), `TokenUsageExtractor`
  - 2026-Q2 공개 단가 11종 하드코딩 (OpenAI/Gemini chat+embedding)
  - callKind 자동 분류: `chat` / `structured` / `tool_use` / `vision` / `embedding`
  - `setMaxEntries(N)` FIFO ring buffer, `setEnabled(false)` off
- 기존 `ResponsesAPIConnection` / `GeminiAPIConnection` / `OpenAiVisionService` / `GeminiVisionService` / `OpenAiEmbeddingService` / `GeminiEmbeddingService` 에 `CostTracker.INSTANCE.record()` 주입 — 호출자 코드 수정 불필요
- 가이드: `doc/fallback-telemetry-guide.md`
- 신규 테스트 48건: `CostTableTest`, `CostTrackerTest`, `TokenUsageExtractorTest`, `FailureReasonTest`, `FallbackPolicyTest`, `FallbackChatRoomTest` (Mockito 통합 시나리오 8건)
- 총 테스트 수: 444 → 540 (+96 이중 집계 포함, 실제 +48건)

### Notes
- Fallback `CostEntry.isFallbackTriggered` 는 현재 항상 `false` 로 기록됨 — FallbackChatRoom 레벨의 표시 로직은 v0.9.2+ 에서 보강 예정
- 모든 기존 API 시그니처 유지 — 하위 호환

---
## [0.9.0] - 2026-04-22

### Added
- **Embeddings API** (Provider 중립 텍스트 임베딩 — T3-a)
  - `com.smartuxapi.ai.embedding`: `EmbeddingService`, `EmbeddingResult`, `EmbeddingException`, `EmbeddingServiceFactory`, `Embeddings` 유틸
  - OpenAI 구현: `text-embedding-3-small` (1536, 기본) / `text-embedding-3-large` (3072) / `ada-002` (legacy)
  - Gemini 구현: `text-embedding-004` (768, 기본) — `batchEmbedContents` REST 엔드포인트
  - `embed(text)` 단일 + `embedBatch(List<String>)` 배치 — 한 번의 API 호출로 여러 텍스트 처리
  - `Embeddings` 유틸: `cosineSimilarity`, `argmax`, `topK`, `normalize` — 0-벡터 NaN 방지
  - `EmbeddingServiceFactory.createOpenAI[FromEnv]` / `createGemini[FromEnv]`
  - 가이드: `doc/embeddings-guide.md`
- 총 테스트 수: 384 → 444 (+60 이중 집계 포함, 실제 +30건)

### Notes
- T3-b (Cross-provider Fallback + Cost Telemetry) 는 v0.9.1 로 이관 — 별도 설계 스케치 `20260422_fallback_telemetry_design_sketch.md` 준비됨

---
## [0.8.1] - 2026-04-22

### Added
- **Gemini Vision 구현** (T1-b 공급자 확장 — `GeminiVisionService`)
  - `gemini-1.5-flash` 기본 모델, `VisionService` 인터페이스 100% 호환
  - HTTPS URL 은 자동 다운로드 후 base64 inline 전달 (Gemini 는 image URL 직접 지원 X)
  - data URI (`data:image/...;base64,...`) 는 별도 다운로드 없이 페이로드 직접 사용
  - 이미지 다운로드 크기 상한 20MB
  - `VisionServiceFactory.createGemini(apiKey[, model])` + `createGeminiFromEnv()` 팩토리 추가
- **GeminiChattingToolUseTest** — T2-b Gemini 쪽 Mockito 통합 테스트 (6건)
  - v0.8.0 의 `ResponsesChattingToolUseTest` 와 대칭: null tools 위임, 1/2-round auto loop, 미등록 tool→error, manual first round, manual continuation
- **GeminiVisionServiceTest** — 단위 테스트 7건 (활성 상태, 입력 검증, MIME 추정)

### Coverage
- 테스트 수: 358 → 384 (+26건)

---
## [0.8.0] - 2026-04-22

### Added
- **Structured Output** (Provider 중립 JSON Schema 강제 응답 — T2-a)
  - `com.smartuxapi.ai.schema`: `ResponseSchema` / `SchemaBuilder`
  - OpenAI Responses: `text.format = { type: "json_schema", ... }` 매핑
  - Gemini: `generationConfig.responseMimeType + responseSchema` 매핑
  - `Chatting.sendPromptWithSchema(userMsg, schema)` default 메서드 추가
  - `ResponsesChatting` / `GeminiChatting` override — 응답 원문을 자동 파싱하여 `structured` 키 (JsonNode) 로 병기
  - 파싱 실패는 예외가 아닌 WARN 로그 + `structured=null` (호출자가 fallback 결정)
  - `SchemaBuilder`: object 루트 + 5 타입 (string/integer/boolean/array/nested object) — strict 호환 `additionalProperties: false` 자동 주입
  - T2-b Tool Use 와 `SchemaBuilder.build()` JsonNode 포맷 공유
  - 가이드: `doc/structured-output-guide.md`
  - 신규 테스트: `SchemaBuilderTest`, `ResponseSchemaTest`, `ResponsesChattingStructuredOutputTest`, `GeminiChattingStructuredOutputTest` (+22건)

- **Tool Use / Function Calling** (Provider 중립 T2-b)
  - `com.smartuxapi.ai.tools`: `ToolDefinition` / `ToolHandler` / `ToolCall` / `ToolResult` / `ToolRegistry` / `ToolTurnResult`
  - OpenAI Responses API `tools` 연동 (request/response 양방향)
  - Gemini `functionDeclarations` 연동 (클라이언트 UUID 부여)
  - `Chatting.sendPromptWithTools(msg, registry)` — **자동 루프** (최대 5 라운드 / `DEFAULT_MAX_TOOL_ROUNDS`)
  - `Chatting.sendPromptExpectingToolCalls(msg, registry)` + `continueWithToolResults(results, registry)` — **수동 dispatch**
  - 등록되지 않은 tool 호출은 `ToolResult.error` 로 LLM 에 피드백 (자동 복구 가능)
  - Handler 예외 자동 캐치 → `ToolResult.error`
  - Output 크기 상한 256KB — 초과 시 자동 축약
  - `ConversationHistory` (OpenAI/Gemini 양쪽) 에 tool_call / tool_result 아이템 지원 추가
  - `ChatRoom.setToolRegistry/getToolRegistry` default 메서드
- **VisionTools.scanImageTool** (T1-b × T2-b 통합)
  - Vision 모듈을 Tool 로 감싸 LLM 이 자동 호출 가능
  - `ActionQueueHandler` 주입 시 `addImageScanInfo` 자동 호출 (imageUrl 기준 dedupe)
- 가이드: `doc/tool-use-guide.md`
- 신규 테스트: `ToolRegistryTest`, `ToolCallResultTest`, `ToolTurnResultTest`, `ResponsesChattingToolUseTest`, `VisionToolsTest` (+25건)
- 총 단위 테스트 수: 266 → 358 (+92건, T2-a +22 + T2-b +25, 각 suite 이중 집계 포함)

### Changed
- `ResponsesAPIConnection.generateContent(JSONArray, CacheStrategy, ResponseSchema)` 오버로드 추가 — 기존 2-인자 시그니처 유지
- `GeminiAPIConnection.generateContent(JSONArray, CacheStrategy, ResponseSchema)` 오버로드 추가
- `ResponsesAPIConnection.generateContentWithTools(...)` 추가 — Tool Use 전용 경로 (ToolTurnResult 반환)
- `GeminiAPIConnection.generateContentWithTools(...)` 추가
- `openai.ConversationHistory`: `addAssistantOutputItems`, `addToolResult` 추가
- `gemini.ConversationHistory`: `addModelContent`, `addToolResults` 추가

---
## [0.7.0] - 2026-04-22

### Added
- **Prompt Caching** (Provider 중립 캐시 API — T1-a)
  - `com.smartuxapi.ai.cache`: `CacheHint` / `CacheMetrics` / `CacheStrategy` / `NoOpCacheStrategy`
  - OpenAI 자동 프리픽스 캐싱 (`OpenAiPromptCacheStrategy`) — `usage.prompt_tokens_details.cached_tokens` 파싱
  - Gemini 명시적 context caching (`GeminiContextCacheStrategy`) — `cachedContents` REST 리소스 생성/참조/삭제
  - `ChatRoom` / `Chatting` 에 `markAsCacheable` / `setCacheStrategy` / `getLastCacheMetrics` 기본 메서드 추가
  - OpenAI `ConversationHistory`: 캐시 프리픽스를 `system` 메시지로 매 요청 앞에 배치 (내부 히스토리 불오염)
  - 가이드: `doc/caching-guide.md`
- **Vision API** (OpenAI GPT-4 Vision 기반 이미지 텍스트 추출 — T1-b)
  - `com.smartuxapi.ai.vision`: `VisionService` / `VisionException` / `ImageScanInfo` / `VisionServiceFactory`
  - `impl.OpenAiVisionService` — checked 예외 기반 에러 전파 (Tool Use 호환)
  - `ActionQueueHandler` 에 `addImageScanInfo` / `addImageScanInfoList` / `getImageScanInfoList` / `clearImageScanInfo` 추가
    - `imageUrl` 기준 dedupe 기본 적용
    - `curViewInfo` JSON 의 `imageScanInfo` 키로 자동 병합 (원본 오염 없는 shallow copy)
  - 가이드: `doc/vision-guide.md`
- **테스트 인프라 확장**
  - JaCoCo 코드 커버리지 플러그인 도입
  - Mockito (mockito-core, mockito-junit-jupiter) 테스트 의존성 추가
  - 신규 유닛 테스트: `ActionQueueHandlerErrorTest`, `ConfigLoaderTest`, `ActionQueueUtilTest`, `DebugConfigTest`, `DebugLoggerTest`
  - 테스트 리소스 `invalid.json` 추가
  - 시나리오 기반 테스트 계획 문서: `doc/working/full-scenario-test-plan.md`
- 총 단위 테스트 수: 196 → 266 (+70건 추가)

### Changed
- `ChatRoom` / `Chatting` 인터페이스 — 기본(`default`) 메서드 추가로 기존 구현체 하위 호환 유지
- `ResponsesAPIConnection.generateContent(JSONArray, CacheStrategy)` 오버로드 추가 — 기존 시그니처 유지
- `GeminiAPIConnection.generateContent(JSONArray, CacheStrategy)` 오버로드 추가 — 요청에 `cachedContent` 자동 참조
- `README.md`: `config.json (필수)` → `(선택)` 으로 정정
- 버전 표기 업데이트: 0.6.2 → 0.7.0

### Removed
- Eclipse 프로젝트 설정 파일 `.settings/org.eclipse.buildship.core.prefs` 제거 (IDE 종속성 정리)

### 설계 문서
- `doc/tasks/20260421_tool_use_design_sketch.md` (로컬 전용, gitignore 대상) — v0.8.0 T2-b Tool Use 와의 API 정합을 위한 사전 설계

---
## [0.6.2] - 2026-01-24

### Added
- 디버그 모드 기능: AI 대화 내용을 JSON 파일로 저장하는 디버깅 기능 추가
- `DebugConfig.java`: 싱글턴 패턴으로 구현된 디버그 설정 관리 클래스
- `DebugLogger.java`: 대화 내용을 JSON 파일로 저장하는 로거 클래스
- `ConversationData.java`, `ConversationTurn.java`: 대화 데이터 모델 클래스
- 설정 파일 로딩 우선순위: JAR 실행 디렉터리에서 먼저 설정 파일을 찾고, 없으면 classpath에서 로드

### Changed
- `ConfigLoader.java`: JAR 실행 디렉터리 우선 로딩 기능 추가 (`getJarDirectory()`, `loadFromJarDirectory()` 메서드)
- `config.json`: `setting.json` 내용 통합 (debug-mode, debug-output-path, debug-file-prefix 설정)
- 문서 업데이트: API.md, README.md, TEST_GUIDE.md에 설정 파일(config.json, apikey.json) 가이드 추가
- 버전 표기 업데이트: 0.6.0 → 0.6.1 → 0.6.2

### Removed
- `setting.json` 파일 제거: `config.json`으로 통합

---
## [0.6.1] - 2026-01-20

### Added
- YAML 설정 파일 지원: `PropertiesUtil`에 YAML 파일 파싱 기능 추가 (SnakeYAML 라이브러리 사용)
- YAML 형식의 설정 파일 읽기 기능: `.yml`, `.yaml` 확장자 파일 지원

### Changed
- 문서 업데이트: 모든 문서에서 `smuxapi-war` 프로젝트 참조를 `smuxapi-demo`로 변경
- 프로젝트 구조 문서 정리: 제거된 `smuxapi-war` 프로젝트 관련 내용 정리

### Removed
- `smuxapi-war` 프로젝트 제거: Spring Boot 기반 `smuxapi-demo` 프로젝트로 대체

---
## [0.6.0] - 2025-01-XX

### Added
- `ActionQueueHandler.addCurrentViewInfo(JsonNode)` 메서드 추가: 현재 화면 정보에 추가 정보를 병합하는 기능

### Changed
- 화면 정보 변경 감지 로직 구현: 화면 정보가 실제로 변경된 경우에만 프롬프트에 포함되도록 최적화
- 테스트 프레임워크를 JUnit 5로 마이그레이션: 모든 테스트를 JUnit 5로 변환하고 `com.smartuxapi.AllTests` 통합 테스트 스위트 추가

### Removed
- Nexacro Platform 지원 제거: 관련 문서 및 코드 정리

---
## [0.5.1]

### Added

### Changed
- ConversationHistory 에 view 정보 제거(성능 개선)

### Fixed

### Removed

---
## [0.5.0]

### Added

### Changed
- ActionQueueHandler 종속성 제거 (사용자가 생성 관리)

### Fixed

### Removed

---
## [0.4.0] - 2025-07-29

### Added
- OpenAI Responses API 지원

---
## [0.3.1] - 2025-07-27

### Changed
- API Interface 이름 변경 (SmuThread -> ChatRoom, SmuMessage -> Chatting)

---
## [0.3.0] - 2025-07-25

### Added
- Gemini API 지원

---
## [0.2.1] - 2025-07-23

### Added
- 내부 API 클래스 생성 (ActionQueueHandler)

### Changed
- API Interface 이름 변경 (ChatRoom -> SmuThread, Chatting -> SmuMessage) 

---
## [0.1.1] - 2025-07-07

### Added
- AGPL-3.0 LICENSE, README.md, 디렉터리 구조 작성

---
## [0.1.0] - 2025-01-30

### Added
- 프로젝트 초기 생성
- Java 서버 API 및 JS 클라이언트 코드 기본 뼈대 추가
- User Interaction Flow 포맷 예시 추가 (`docs/sample.su-api_uif.json`)
- `CHANGELOG.md` 문서 초안 추가
- `.gitignore` 설정에 테스트 로그 무시 경로 추가

---

## 📌 참고

- 다음 릴리스에서는 [Google Gemini API 연동]이 포함될 예정입니다.
- 릴리스 이름은 버전 번호(`x.y.z`) 형식을 따릅니다.

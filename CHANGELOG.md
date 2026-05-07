# 📋 Smart UX API — 프로젝트 변경 이력

이 문서는 **smart-ux-api 리포지토리 전체** 의 릴리스 히스토리를 한 페이지에서 조망하기 위한 인덱스입니다.
모듈별 상세 내용은 각자의 CHANGELOG 를 참조하세요:

- 📦 **lib (smart-ux-api)** — [`lib/CHANGELOG.md`](lib/CHANGELOG.md)
- 🎯 **smuxapi-demo** — [`smuxapi-demo/CHANGELOG.md`](smuxapi-demo/CHANGELOG.md)

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) 를 따르고, 버전은 [Semantic Versioning](https://semver.org/lang/ko/) 을 따릅니다.

| 변경 종류 | 의미 |
|----------|------|
| Major | 하위 버전과 호환되지 않는 변화 |
| Minor | 하위 버전과 호환되면서 새 기능 추가 |
| Patch | 기존 버전과 호환되는 버그/문서 수정 |

> **태그 정책**: 리포지토리 태그 (`v0.x.y`) 는 **lib 모듈 릴리스**를 가리킵니다.
> smuxapi-demo 는 자체 버전 (`0.10.x`) 을 가지며 별도 태그 없이 demo 내부 CHANGELOG 에서만 추적합니다.

---

## 활성 릴리스 (태그 보유)

| Tag | 날짜 | 헤드라인 | 모듈 변동 | 상세 |
|-----|------|---------|---------|------|
| **v0.9.6** | 2026-04-25 | Real LLM IT — Phase 3 검증 통합 테스트 | lib | [lib §0.9.6](lib/CHANGELOG.md#096---2026-04-25) |
| **v0.9.5** | 2026-04-25 | Full Scenario Test Case Phase 3 (테스트 러너) | lib | [lib §0.9.5](lib/CHANGELOG.md#095---2026-04-25) |
| **v0.9.4** | 2026-04-24 | `isFallbackTriggered` 정확성 + `:lib:deploy` 경로 보정 | lib | [lib §0.9.4](lib/CHANGELOG.md#094---2026-04-24) |
| **v0.9.3** | 2026-04-22 | 문서 경로/버전 정합성 patch (refactor 후속) | lib | [lib §0.9.3](lib/CHANGELOG.md#093---2026-04-22) |
| **v0.9.2** | 2026-04-23 | 프로젝트 구조 평탄화 (3단 중첩 → 2단 multi-project) | lib · demo | [lib §0.9.2](lib/CHANGELOG.md#092---2026-04-23) · [demo §0.9.1](smuxapi-demo/CHANGELOG.md#091---2026-04-23) |
| **v0.9.1** | 2026-04-23 | T3-b — Cross-provider Fallback + Cost Telemetry | lib | [lib §0.9.1](lib/CHANGELOG.md#091---2026-04-23) |
| **v0.9.0** | 2026-04-22 | T3-a — Embeddings API (provider 중립) | lib · demo | [lib §0.9.0](lib/CHANGELOG.md#090---2026-04-22) · [demo §0.9.0](smuxapi-demo/CHANGELOG.md#090---2026-04-22) |
| **v0.8.1** | 2026-04-22 | Gemini Vision + Tool Use 테스트 (coverage gap) | lib | [lib §0.8.1](lib/CHANGELOG.md#081---2026-04-22) |
| **v0.8.0** | 2026-04-22 | T2-a/T2-b — Structured Output + Tool Use | lib · demo | [lib §0.8.0](lib/CHANGELOG.md#080---2026-04-22) · [demo §0.8.0](smuxapi-demo/CHANGELOG.md#080---2026-04-22) |
| **v0.7.0** | 2026-04-22 | T1-a/T1-b — Prompt Caching + Vision API + 테스트 인프라 | lib | [lib §0.7.0](lib/CHANGELOG.md#070---2026-04-22) |
| **v0.6.2** | 2026-01-24 | 모델명 갱신 + SCENARIO COLLECTION 제거 | lib · demo | [lib §0.6.2](lib/CHANGELOG.md#062---2026-01-24) · [demo §0.6.2](smuxapi-demo/CHANGELOG.md#062---2026-01-24) |
| **v0.5.1** | — | (이전 baseline — lib only) | lib | [lib §0.5.1](lib/CHANGELOG.md#051) |

---

## smuxapi-demo 단독 릴리스 (lib 태그 없음)

데모 모듈은 v0.10.x 부터 lib 와 독립적으로 패치 가능. 다음은 lib 태그가 동반되지 않은 데모 단독 릴리스:

| Demo Ver | 날짜 | 헤드라인 | 상세 |
|---------|------|---------|------|
| **0.10.2** | 2026-05-07 | `easy_kiosc_uif.json` 학습 자원 추가 + BrowserLauncher headless / `--smuxapi.no-browser` 옵션 | [demo §0.10.2](smuxapi-demo/CHANGELOG.md#0102---2026-05-07) |
| **0.10.1** | 2026-04-25 | `/demo/scenario/*` 운영 엔드포인트 (status / save / reset / preview) | [demo §0.10.1](smuxapi-demo/CHANGELOG.md#0101---2026-04-25) |
| **0.10.0** | 2026-04-24 | Scenario Collector — Full Scenario Test Case Phase 1 | [demo §0.10.0](smuxapi-demo/CHANGELOG.md#0100---2026-04-24) |
| **0.9.2** | 2026-04-22 | (lib v0.9.3 자동 픽업, 문서 정합성) | [demo §0.9.2](smuxapi-demo/CHANGELOG.md#092---2026-04-22) |
| **0.6.1** | 2026-01-22 | demo WAR 배포 + `SERVER_PORT` 동적 설정 | [demo §0.6.1](smuxapi-demo/CHANGELOG.md#061---2026-01-22) |
| **0.6.0** | 2026-01-20 | 독립 Spring Boot JAR + Custom JRE 번들 + 배포 ZIP | [demo §0.6.0](smuxapi-demo/CHANGELOG.md#060---2026-01-20) |
| **0.1.0** | 2026-01-20 | 프로젝트 시작 (lib v0.5.1 동기화) | [demo §0.1.0](smuxapi-demo/CHANGELOG.md#010---2026-01-20) |

---

## 레거시 (태그 미부여, lib CHANGELOG 만 보유)

`v0.5.1` 이전 — 초기 개발 단계. 태그가 부여되지 않았으며 lib CHANGELOG 에만 기록됨.

| 버전 | 날짜 | 헤드라인 | 상세 |
|------|------|---------|------|
| **0.6.0** | 2025-01-XX | `addCurrentViewInfo(JsonNode)` + 화면 변경 감지 최적화 | [lib §0.6.0](lib/CHANGELOG.md#060---2025-01-xx) |
| **0.5.0** | — | ActionQueueHandler 종속성 제거 | [lib §0.5.0](lib/CHANGELOG.md#050) |
| **0.4.0** | 2025-07-29 | OpenAI Responses API 지원 | [lib §0.4.0](lib/CHANGELOG.md#040---2025-07-29) |
| **0.3.1** | 2025-07-27 | API 인터페이스 이름 변경 (`SmuThread` → `ChatRoom`, `SmuMessage` → `Chatting`) | [lib §0.3.1](lib/CHANGELOG.md#031---2025-07-27) |
| **0.3.0** | 2025-07-25 | Gemini API 지원 | [lib §0.3.0](lib/CHANGELOG.md#030---2025-07-25) |
| **0.2.1** | 2025-07-23 | 내부 API 클래스 (`ActionQueueHandler`) | [lib §0.2.1](lib/CHANGELOG.md#021---2025-07-23) |
| **0.1.1** | 2025-07-07 | AGPL-3.0 라이선스, README, 디렉터리 구조 | [lib §0.1.1](lib/CHANGELOG.md#011---2025-07-07) |
| **0.1.0** | 2025-01-30 | 프로젝트 초기 생성 — Java 서버 API + JS 클라이언트 뼈대 | [lib §0.1.0](lib/CHANGELOG.md#010---2025-01-30) |

---

## 마일스톤별 요약

### Tier 1 — 프롬프트 효율 / 비전 (v0.7.0 ~ v0.8.1)
- **T1-a** Prompt Caching (provider 중립 — OpenAI 자동 프리픽스 / Gemini cachedContents)
- **T1-b** OpenAI Vision API + ActionQueueHandler 이미지 스캔 통합
- **v0.8.1** Gemini Vision 확장 (HTTPS URL → base64 inline 자동 다운로드)

### Tier 2 — 응답 형식 / Tool Use (v0.8.0 ~ v0.8.1)
- **T2-a** Structured Output (`sendPromptWithSchema` — `ResponseSchema` + `SchemaBuilder`)
- **T2-b** Tool Use auto-loop + manual dispatch (`sendPromptWithTools` / `sendPromptExpectingToolCalls` / `continueWithToolResults`)
- **VisionTools.scanImageTool** — Vision 모듈을 Tool 로 노출

### Tier 3 — Embeddings / Fallback / Telemetry (v0.9.0 ~ v0.9.4)
- **T3-a** Embeddings API (`EmbeddingService` — OpenAI text-embedding-3-* / Gemini text-embedding-004)
- **T3-b** `FallbackChatRoom` 데코레이터 + `FailureReason` 분류 + `CostTracker` / `CostTable` / `TokenUsageExtractor`
- **v0.9.4** `FallbackContext` ThreadLocal — `isFallbackTriggered` 정확성 패치

### Quality Pass — 구조 / 문서 / 테스트 (v0.9.2 ~ v0.9.6)
- **v0.9.2** 3단 중첩 → 2단 multi-project 평탄화 (루트가 Gradle root)
- **v0.9.3** 문서 경로/버전 정합성 일괄 갱신
- **v0.9.5/0.9.6** Full Scenario Test Case Phase 3 (시나리오 재현/검증 러너 + Real LLM IT)

---

## 컨벤션

- **버전 기록 위치**: 코드 변경 시 해당 모듈의 CHANGELOG (`lib/` or `smuxapi-demo/`) 에 항목 추가. 태그 시점에 본 루트 인덱스 갱신.
- **헤드라인 작성**: 한 줄로 "무엇이 추가/변경되었는지" — 상세는 모듈 CHANGELOG 가 담당.
- **태그 명명**: lib 기준 `v{major}.{minor}.{patch}`. demo 단독 릴리스는 태그 없이 demo CHANGELOG 만 기록.

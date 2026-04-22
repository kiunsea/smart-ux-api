# 📋 Changelog

모든 변경 사항은 이 문서에 기록됩니다.

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따르며,  
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.
  - Major: 하위 버전과 호환되지 않는 변화가 생겼을 때 증가
  - Minor: 하위 버전과 호환되면서 새로운 기능이 추가될 때 증가
  - Patch: 기존 버전과 호환되면서 버그를 수정한 것일 때 증가
  
---
## [0.9.1] - 2026-04-23

### Changed
- **프로젝트 구조 재편**: repo 루트가 Gradle multi-project 루트가 되어 `settings.gradle.kts` 불필요 → 삭제. `project(":lib")` 의존은 루트 `include` 로 자동 해결
- smart-ux-api 의존성 `project(":lib")` 가 자동으로 v0.9.2 픽업

### Notes
- 기능/API 변경 없음. `./gradlew :smuxapi-demo:bootWar` 동작 v0.9.0 과 동일

---
## [0.9.0] - 2026-04-22

### Added
- **Embedding 의미 검색 데모 엔드포인트** (`POST /demo/embedding/search`)
  - smart-ux-api v0.9.0 T3-a 의 `EmbeddingService.embedBatch` + `Embeddings.topK` 사용
  - 요청: `{provider, query, candidates[], top_k}` (provider: "openai"|"gemini", top_k 기본 3)
  - 응답: `{query, provider, model, dimension, prompt_tokens, matches: [{index, label, score}]}`
  - query + candidates 를 **단일 배치 호출** (API 호출 1회) — cosine 은 로컬 계산
  - 안전장치: candidates 상한 100, 빈 문자열 거부

### Changed
- smart-ux-api 의존성 `project(":lib")` 가 자동으로 v0.9.0 픽업
- 버전 `0.8.0` → `0.9.0` (smart-ux-api 와 정렬)

---
## [0.8.0] - 2026-04-22

### Added
- **Structured Output 데모 엔드포인트** (`POST /demo/structured`)
  - smart-ux-api v0.8.0 T2-a 의 `sendPromptWithSchema` 사용
  - 사전 정의 스키마 2종: `userProfile`, `uiIntent`
  - 요청: `{ai_model, user_msg, schema}` → 응답: `{message, action_queue, structured}`
- **Tool Use 데모 엔드포인트** (`POST /demo/tools`)
  - smart-ux-api v0.8.0 T2-b 의 `sendPromptWithTools` 사용 (auto-loop, max 5 rounds)
  - 기본 tool: `getTime` (네트워크 호출 없음, 데모 안전)
  - 옵션 tool: `scanImage` (`VisionTools.scanImageTool` — `enable_vision=true` 기본값)
  - 요청: `{ai_model, user_msg, enable_vision}` → 응답: `{message, action_queue, tool_calls}`

### Changed
- smart-ux-api 의존성 `project(":lib")` 가 자동으로 v0.8.0 픽업 (Gradle 서브모듈 참조)
- 버전 `0.6.0` → `0.8.0` (smart-ux-api 와 정렬)
- `tasks.bootWar { enabled = false }` 추가 — 외부 Tomcat 배포용 `tasks.war` 만 사용 (bootWar 의 기본 duplicate 검사로 jackson-annotations 등 중복 충돌 방지)

### Compatibility
- 기존 `/action` `/collect` 엔드포인트 동작 변화 없음 (smoke test 회귀 통과)
- smart-ux-api 신규 API 는 모두 `default` 메서드 — 기존 ChatRoom/Chatting 경로 무수정 동작

---
## [0.6.2] - 2026-01-24

### Changed
- `smuxapi-demo.yml` 설정 파일 정리: 모델명 업데이트 (gpt-4 → gpt-4.1, gemini-2.0-flash-exp → gemini-2.5-flash)
- README.md 문서 업데이트: SERVER_PORT 설정 및 최신 모델명 반영

### Removed
- SCENARIO COLLECTION 기능 제거: 시나리오 수집 관련 코드 및 설정 완전 제거
  - `ChatRoomService.java`에서 `wrapWithCollector` 메서드 제거
  - `ActionQueueController.java`에서 collector 관련 코드 제거
  - `ScenarioController.java` 파일 삭제
  - `smuxapi-demo.yml`에서 SCENARIO COLLECTION SETTINGS 섹션 제거

---
## [0.6.1] - 2026-01-22

### Added
- WAR 배포 기능: Tomcat 서버에 WAR 파일로 배포 가능
- 서버 포트 동적 설정: `smuxapi-demo.yml`의 `SERVER_PORT` 설정으로 서버 포트 변경 가능
- BrowserLauncher 동적 포트 지원: 설정된 포트에 맞춰 브라우저 URL 자동 생성
- mega_speech.html 점 애니메이션: 마이크 버튼 클릭 시 "Interpreting" 점 애니메이션 추가
- war.bat 빌드 스크립트: WAR 파일 빌드를 위한 배치 스크립트 추가

### Changed
- 설정 키 이름 변경: `DEMO_PORT` → `SERVER_PORT`로 통일
- WAR 파일 출력 경로: `build/libs/` → `packaging/distribution/`으로 변경
- 로그 저장 위치: 배포 타입에 따라 동적으로 결정 (JAR: 배포 위치/log, WAR: C:/LOGS)
- packaging/distribution/README.md: ZIP 및 WAR 배포 가이드 추가

### Fixed
- Eclipse IDE Validation Error: ERROR_DUPLICATE_WEB_INF_LIB 오류 해결

---
## [0.6.0] - 2026-01-20

### Added
- Spring Boot 기반 독립 실행형 데모 애플리케이션 생성
- Custom JRE 번들링 기능: `jlink`를 사용한 독립 실행 가능한 JRE 생성
- 배포 패키지 자동 생성: `deploy` Gradle 태스크로 ZIP 배포 파일 생성
- 브라우저 자동 실행 기능: 서버 시작 시 자동으로 웹 브라우저 열기
- YAML 설정 파일 지원: `smuxapi-demo.yml` 파일로 API 키 관리
- 배치 스크립트: Windows용 `smuxapi-demo.bat` 실행 파일 제공
- Context Path 설정: `/smuxapi` 경로로 애플리케이션 접근

### Changed
- 설정 파일 형식 변경: `def.smuxapi.properties` → `smuxapi-demo.yml` (YAML 형식)
- HTML 리소스 경로: 모든 절대 경로를 상대 경로로 변경하여 context-path 독립성 확보
- 정적 리소스 처리: `src/main/webapp` → `src/main/resources/static` 자동 복사
- 프로젝트 독립성: `smart-ux-api` 프로젝트의 하위 프로젝트에서 독립 프로젝트로 변경

### Fixed
- Jakarta Servlet API 버전 충돌 해결: `compileOnly`로 변경하여 Spring Boot 제공 버전 사용
- 정적 리소스 404 오류 해결: Spring Boot가 인식할 수 있도록 리소스 경로 수정

### Removed
- `def.smuxapi.properties` 파일 제거: YAML 형식으로 완전 전환
- `smuxapi-war` 프로젝트 의존성 제거: 독립 실행형 프로젝트로 전환

---
## [0.1.0] - 2026-01-20
### Changed
- project 시작
- smart-ux-api 프로젝트의 [0.5.1] 버전과 동기화 작업

---

## 📌 참고

- 릴리스 이름은 버전 번호(`x.y.z`) 형식을 따릅니다.

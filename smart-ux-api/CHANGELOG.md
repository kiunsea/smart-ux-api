# 📋 Changelog

모든 변경 사항은 이 문서에 기록됩니다.

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따르며,  
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.
  - Major: 하위 버전과 호환되지 않는 변화가 생겼을 때 증가
  - Minor: 하위 버전과 호환되면서 새로운 기능이 추가될 때 증가
  - Patch: 기존 버전과 호환되면서 버그를 수정한 것일 때 증가
  
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

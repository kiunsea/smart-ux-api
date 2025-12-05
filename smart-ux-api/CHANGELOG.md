# 📋 Changelog

모든 변경 사항은 이 문서에 기록됩니다.

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따르며,  
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.
  - Major: 하위 버전과 호환되지 않는 변화가 생겼을 때 증가
  - Minor: 하위 버전과 호환되면서 새로운 기능이 추가될 때 증가
  - Patch: 기존 버전과 호환되면서 버그를 수정한 것일 때 증가
  
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

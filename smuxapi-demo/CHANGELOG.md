# 📋 Changelog

모든 변경 사항은 이 문서에 기록됩니다.

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따르며,  
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.
  - Major: 하위 버전과 호환되지 않는 변화가 생겼을 때 증가
  - Minor: 하위 버전과 호환되면서 새로운 기능이 추가될 때 증가
  - Patch: 기존 버전과 호환되면서 버그를 수정한 것일 때 증가
  
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

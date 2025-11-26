# 📋 Changelog

모든 변경 사항은 이 문서에 기록됩니다.

형식은 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)를 따르며,  
버전 관리는 [Semantic Versioning](https://semver.org/lang/ko/)을 따릅니다.

---

## [Unreleased]

### Added

### Changed

### Fixed

### Removed

---
## [0.5.2] - 2025-11-26

### Added
- **Embedded Tomcat 지원**: 톰캣 서버 없이 실행 가능한 Embedded Tomcat 서버 추가
- **Gradle 실행 태스크**: `./gradlew :smuxapi-war:run` 명령으로 바로 실행 가능
- **EmbeddedTomcatServer 클래스**: 독립 실행형 서버 메인 클래스 추가

### Changed
- **Gradle 웹 애플리케이션으로 전환**: Maven 기반에서 Gradle 기반 빌드 시스템으로 변경
- **의존성 관리 개선**: WEB-INF/lib의 JAR 파일을 Gradle 의존성으로 대체
- **리소스 파일 구조 개선**: 리소스 파일을 표준 Gradle 구조(`src/main/resources`)로 이동
- **빌드 설정 최적화**: WAR 플러그인을 사용하여 표준 웹 애플리케이션 구조로 정리
- **Jakarta Servlet API**: compileOnly에서 implementation으로 변경 (Embedded Tomcat 실행을 위해)

### Fixed
- 컴파일 경고 수정 (null 체크, 제네릭 타입 안전성)
- 리소스 파일 경로 수정 (`resources/easy_kiosc_uif.json` → `easy_kiosc_uif.json`)
- SessionListener web.xml 등록 추가

### Technical Details
- `build.gradle.kts` 추가: war 플러그인 및 의존성 설정
- `settings.gradle.kts`에 모듈 추가
- Embedded Tomcat 의존성 추가:
  - `tomcat-embed-core:10.1.20`
  - `tomcat-embed-jasper:10.1.20`
  - `tomcat-embed-websocket:10.1.20`
- 리소스 파일 위치 정리:
  - `log4j2.xml` → `src/main/resources/`
  - `def.smuxapi.properties` → `src/main/resources/`
  - `easy_kiosc_uif.json` → `src/main/resources/`

---
## [0.4.0] - 2025-0?-??
### Changed
- smart-ux-api 프로젝트의 [0.5.0] 버전과 동기화 작업

---
## [0.3.0] - 2025-07-29
### Changed
- smart-ux-api 프로젝트의 [0.4.0] 버전과 동기화 작업

---
## [0.2.0] - 2025-07-25
### Changed
- smart-ux-api 프로젝트의 [0.3.0] 버전과 동기화 작업

---
## [0.1.0] - 2025-07-23
### Changed
- smart-ux-api 프로젝트의 [0.2.1] 버전과 동기화 작업

---

## 📌 참고

- 릴리스 이름은 버전 번호(`x.y.z`) 형식을 따릅니다.

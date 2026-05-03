# Smart UX API 프로젝트
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen) 

## 🧠 소개

**Smart UX API**는 Pure Java와 HTML5 기반의 기존 웹 애플리케이션에 인공지능 기반 화면 제어 기능을 쉽게 통합할 수 있도록 지원하는 오픈소스 도구입니다.

사용자가 전달하는 텍스트 기반 요청을 분석하여, 해당 요청을 수행하기 위해 어떤 UI 요소에 어떤 액션을 취해야 하는지를 AI가 판단하고 실행 가능한 **액션 프로세스 정의서**로 응답받아 화면을 자동 제어합니다.

서비스에 익숙하지 않은 사용자에게 직관적인 사용 가이드를 제공하거나, 반복 작업을 AI를 통해 자동화하고자 할 때 특히 유용합니다.

<img src="https://github.com/user-attachments/assets/9d597451-94a4-401a-967c-effccd1b60f2" alt="kiosk 시연 영상" height="650">

## 🔍 주요 기능

- ✅ 기존 Java 웹 애플리케이션 서비스에 손쉽게 통합 가능
- 🔍 웹 화면 내 UI 구성 요소를 자동 수집
- 🤖 사용자의 자연어 요청을 AI에게 전달
  * Google Gemini API 지원
  * OpenAI Responses API 지원
  * OpenAI Assistants API 지원
- 📋 AI로부터 화면 제어를 위한 액션 프로세스 정의서 수신
- ⚡ 액션 프로세스로 화면 상에서 필요한 액션을 자동 실행

## 🏗️ 아키텍처

Smart UX API는 다음과 같은 플로우로 동작합니다:

```
┌─────────────┐  Natural Lang  ┌──────────────────┐  UI Info       
│    User     │  Command       │  Smart UX        │  Collection    
│   Web UI    │                │                  │
│             │ ──────────────>│  Collector (JS)  │ ──────────────>
│  Elements   │                │                  │
└─────────────┘                └──────────────────┘                
                                       │
                                       │ UI Structure JSON
                                       ▼
┌─────────────┐  Action Queue  ┌──────────────────┐  Prompt +      
│   Web UI    │ <──────────────│  Smart UX        │  UI Info       
│   AI API    │                │                  │
│   Control   │                │  Client (Java)   │ ──────────────>
│ (GPT/Gemini)│                │                  │
└─────────────┘                └──────────────────┘                
```

1. **UI 수집**: `smart-ux-collector.js`가 웹 페이지의 DOM 요소를 스캔하여 JSON으로 변환
2. **프롬프트 전송**: 사용자 요청과 UI 정보를 AI API에 전달
3. **액션 생성**: AI가 실행 가능한 Action Queue를 생성
4. **자동 실행**: `smart-ux-client.js`가 Action Queue를 파싱하여 UI 제어

## 📁 프로젝트 구조 이해하기

이 저장소는 **Gradle multi-project 루트**이며, 다음과 같은 구조로 구성되어 있습니다:

```
smart-ux-api/                    ← GitHub 저장소 루트 (Gradle multi-project root)
│
├── 📖 README.md                 ← 이 파일 (프로젝트 개요 및 시작 가이드)
├── 📚 docs/                     ← 프로젝트 문서 (API, 설치, 예제 등)
├── ⚙️ settings.gradle.kts       ← :lib, :smuxapi-demo 모듈 include
├── 🔧 gradlew, gradlew.bat      ← Gradle 래퍼 (루트에서 모든 모듈 빌드)
│
├── 📦 lib/                      ← 메인 라이브러리 모듈
│   ├── src/main/java/           ← Java 소스 코드
│   ├── src/main/js/             ← JavaScript 클라이언트 라이브러리
│   ├── build/libs/              ← 빌드된 JAR 파일 (빌드 후 생성)
│   ├── doc/                     ← 라이브러리 상세 문서
│   ├── CHANGELOG.md             ← 라이브러리 변경 이력
│   └── README.md                ← 라이브러리 모듈 문서
│
├── 🎯 smuxapi-demo/             ← 샘플 Spring Boot 애플리케이션
│   ├── src/main/java/           ← 샘플 Spring Boot 코드
│   ├── src/main/webapp/         ← 샘플 웹 리소스
│   ├── packaging/               ← 배포 산출물 디렉터리
│   ├── bat/                     ← 데모 실행 스크립트 (start.bat 등)
│   └── README.md                ← 샘플 프로젝트 설명
│
└── 🔧 bat/                      ← 리포지토리 공용 스크립트 (doribox 배포 등)
```

**처음 사용하시는 경우**: 저장소를 클론한 뒤 **루트에서 바로 빌드** 하세요:
```bash
git clone https://github.com/kiunsea/smart-ux-api.git
cd smart-ux-api
gradlew.bat :lib:build          # Windows
./gradlew :lib:build            # Linux / macOS
```

> 💡 v0.9.2 부터 루트가 Gradle multi-project 루트로 평탄화 되어 `cd smart-ux-api/lib` 같은 하위 디렉터리 이동 없이 바로 빌드 가능.

## 🚀 Quick Start

### 사전 요구 사항
- Java 17 이상
- Gradle 8.x (프로젝트에 Gradle Wrapper 포함)
- OpenAI API Key 또는 Google Gemini API Key
- 웹 애플리케이션 서버 (Tomcat, Jetty 등)

### 5분만에 시작하기

#### 1단계: 저장소 클론
```bash
git clone https://github.com/kiunsea/smart-ux-api.git
cd smart-ux-api
```

#### 2단계: 메인 라이브러리 빌드
리포지토리 루트에서 Gradle multi-project 경로로 빌드합니다.
```bash
# Windows
gradlew.bat :lib:build

# Linux/Mac
./gradlew :lib:build
```

> 💡 **팁**: 빌드가 완료되면 `lib/build/libs/smart-ux-api-0.9.3.jar` 파일이 생성됩니다.
> 데모 앱은 `gradlew :smuxapi-demo:bootRun` 또는 `smuxapi-demo\bat\start.bat` 더블클릭 으로 바로 실행 가능.

#### 3단계: JAR 파일을 웹 애플리케이션에 추가
생성된 JAR 파일을 웹 애플리케이션의 `/WEB-INF/lib/` 디렉터리에 복사합니다.

```bash
# 예시: Tomcat 웹 애플리케이션에 복사
cp lib/build/libs/smart-ux-api-0.9.3.jar \
   /path/to/your-webapp/WEB-INF/lib/
```

#### 4단계: JavaScript 라이브러리 포함
JavaScript 클라이언트 라이브러리를 웹 루트 디렉터리에 복사합니다.

```bash
# JavaScript 파일 복사
cp lib/src/main/js/*.js \
   /path/to/your-webapp/js/
```

#### 5단계: API Key 설정
웹 애플리케이션의 `resources/` 디렉터리에 `apikey.json` 파일을 생성합니다.

```json
{
    "OPENAI_API_KEY": "your-openai-api-key",
    "OPENAI_MODEL": "gpt-4.1",
    "GEMINI_API_KEY": "your-gemini-api-key",
    "GEMINI_MODEL": "gemini-2.5-flash"
}
```

> ⚠️ **보안 주의**: `apikey.json` 파일은 절대 Git에 커밋하지 마세요! `.gitignore`에 추가되어 있습니다.

#### 6단계: HTML에 스크립트 추가
웹 페이지에 JavaScript 라이브러리를 포함합니다.

```html
<!DOCTYPE html>
<html>
<head>
    <title>My Web App</title>
</head>
<body>
    <!-- 웹 애플리케이션 콘텐츠 -->
    
    <!-- Smart UX API 스크립트 (body 끝에 추가) -->
    <!-- smart-ux-collector.js: 자동 실행되어 UI 정보를 수집하고 window.uiSnapshot에 저장 -->
    <script src="/js/smart-ux-collector.js"></script>
    
    <!-- smart-ux-client.js: ES6 모듈로 로드 (필요시) -->
    <script type="module">
        import { doActions } from '/js/smart-ux-client.js';
        window.doActions = doActions;  // 전역에서 사용할 수 있도록 저장
    </script>
</body>
</html>
```

> 💡 **참고**: 
> - `smart-ux-collector.js`는 일반 스크립트로 로드하면 자동 실행됩니다
> - `smart-ux-client.js`는 ES6 모듈이므로 `type="module"`로 로드하거나 `import` 문을 사용해야 합니다
> - 수집된 UI 정보는 `window.uiSnapshot`에 자동으로 저장됩니다

### 📦 프로젝트 구성 요약

| 디렉터리 | 설명 | 용도 |
|---------|------|------|
| `lib/` | **메인 라이브러리 모듈** | 실제 사용할 라이브러리 소스 코드 및 빌드 결과물 (`smart-ux-api-{version}.jar`) |
| `smuxapi-demo/` | **샘플 Spring Boot 앱** | `project(":lib")` 에 의존하는 WAR/JAR 데모 — `:smuxapi-demo:bootRun` 으로 실행 |
| `docs/` | **프로젝트 문서** | API 레퍼런스, 설치 가이드, 예제, 트러블슈팅 |
| `bat/` | **공용 스크립트** | 리포지토리 레벨 유틸 (예: doribox 로 JAR 배포) |

### 🎯 다음 단계

- **샘플 프로젝트 실행**: `smuxapi-demo/` 디렉터리의 [README.md](smuxapi-demo/README.md) 참조
- **상세 설치 가이드**: [INSTALL.md](docs/INSTALL.md) 참조
- **API 문서**: [API.md](docs/API.md) 참조
- **코드 예제**: [EXAMPLES.md](docs/EXAMPLES.md) 참조
- **릴리스 이력**: [CHANGELOG.md](CHANGELOG.md) — 프로젝트 전체 인덱스 / 모듈별 상세는 [`lib/CHANGELOG.md`](lib/CHANGELOG.md), [`smuxapi-demo/CHANGELOG.md`](smuxapi-demo/CHANGELOG.md)

## 🌟 사용 사례

### 키오스크 자동화
음성 또는 텍스트 명령으로 키오스크 주문을 자동화하여 고령자 및 디지털 취약계층을 지원합니다.

```
사용자: "아이스 아메리카노 2잔이랑 따뜻한 레몬차 주문해줘"
→ AI가 메뉴 선택 → 수량 조절 → 장바구니 추가
```

### 웹 애플리케이션 가이드
복잡한 웹 서비스의 사용법을 AI가 자동으로 안내합니다.

```
사용자: "회원가입 도와줘"
→ AI가 단계별로 폼 작성 가이드 제공
```

### 업무 자동화
반복적인 웹 작업을 자연어 명령으로 자동화합니다.

```
사용자: "오늘 날짜로 보고서 검색하고 엑셀로 다운로드해줘"
→ AI가 검색 → 필터 설정 → 다운로드 실행
```

## 📂 상세 디렉터리 구조

```
smart-ux-api/                          ← GitHub 저장소 루트 = Gradle multi-project 루트
│
├── 📄 README.md                      ← 프로젝트 개요 (현재 파일)
├── 📄 CHANGELOG.md                   ← 프로젝트 전체 릴리스 인덱스 (모듈 CHANGELOG deep link)
├── 📄 LICENSE                        ← Apache 2.0 라이선스
├── 📄 SECURITY.md                    ← 보안 정책
├── 📄 CONTRIBUTING.md                ← 기여 가이드
├── 📄 CODE_OF_CONDUCT.md             ← 행동 강령
├── ⚙️ settings.gradle.kts            ← include(":lib"), include(":smuxapi-demo")
├── ⚙️ build.gradle.kts               ← 루트 공통 설정 (allprojects repositories)
├── 🔧 gradlew, gradlew.bat           ← Gradle 래퍼 (루트에서 모든 모듈 빌드)
├── 📄 create-release.sh / .bat       ← 릴리스 번들 생성
│
├── 📚 docs/                          ← 프로젝트 문서
│   ├── API.md                        ← API 레퍼런스
│   ├── INSTALL.md                    ← 설치 가이드
│   ├── EXAMPLES.md                   ← 코드 예제
│   └── TROUBLESHOOTING.md            ← 문제 해결 가이드
│
├── 🔧 bat/                           ← 리포지토리 공용 스크립트
│   └── deploy_to_doribox.bat         ← doribox 프로젝트로 JAR 배포
│
├── 📦 lib/                           ← 메인 라이브러리 모듈 (:lib)
│   ├── 📄 README.md                  ← 라이브러리 모듈 문서
│   ├── 📄 CHANGELOG.md               ← 라이브러리 변경 이력
│   ├── 📄 TEST_GUIDE.md              ← 테스트 가이드
│   ├── 📄 build.gradle.kts           ← java-library, jacoco 빌드 설정
│   ├── 📁 src/
│   │   ├── main/
│   │   │   ├── java/                 ← Java 소스 (AI / Cache / Vision / Tools / Embedding / Fallback / Cost)
│   │   │   ├── js/                   ← JavaScript 클라이언트 (smart-ux-collector.js, smart-ux-client.js)
│   │   │   └── resources/            ← 설정 파일
│   │   └── test/                     ← 단위/통합 테스트 (JUnit 5 + Mockito)
│   ├── 📁 doc/                       ← 라이브러리 상세 문서 (API, 가이드)
│   ├── 📁 build/                     ← 빌드 결과물 (생성됨)
│   │   └── libs/
│   │       └── smart-ux-api-0.9.3.jar  ← 빌드된 JAR
│   └── 🔧 run-tests.bat / .sh        ← 테스트 러너
│
└── 🎯 smuxapi-demo/                  ← 샘플 Spring Boot 애플리케이션 (:smuxapi-demo)
    ├── 📄 README.md                  ← 샘플 프로젝트 설명
    ├── 📄 CHANGELOG.md               ← 샘플 변경 이력
    ├── 📄 build.gradle.kts           ← Spring Boot + WAR 빌드 설정
    ├── 🔧 bat/
    │   └── start.bat                 ← 원클릭 실행 (내부적으로 :smuxapi-demo:bootRun)
    ├── 📁 packaging/distribution/    ← 배포 산출물 (ZIP, WAR)
    └── 📁 src/
        └── main/
            ├── java/                 ← Spring Boot 컨트롤러 / 서비스
            ├── resources/            ← application yml, 설정
            └── webapp/               ← 웹 리소스 (HTML, CSS, JS)
```

### 🔍 주요 경로 안내

| 목적 | 경로 / 명령 |
|------|------|
| **라이브러리 빌드** (루트에서) | `gradlew :lib:build` |
| **빌드된 JAR 파일** | `lib/build/libs/smart-ux-api-0.9.3.jar` |
| **JavaScript 파일** | `lib/src/main/js/` |
| **데모 실행** (루트에서) | `gradlew :smuxapi-demo:bootRun` 또는 `smuxapi-demo\bat\start.bat` |
| **샘플 프로젝트** | `smuxapi-demo/` |
| **프로젝트 문서** | `docs/` |
| **API 레퍼런스** | `docs/API.md` |
| **라이브러리 모듈 문서** | `lib/README.md`, `lib/doc/` |
| **릴리스 이력 (전체)** | `CHANGELOG.md` (루트) — 모듈 상세는 `lib/CHANGELOG.md`, `smuxapi-demo/CHANGELOG.md` |

## ❓ FAQ

### Q: 어디서부터 시작해야 하나요?
A:
1. **빠른 시작**: 루트 `README.md`의 "Quick Start" 섹션을 따라하세요 (`gradlew :lib:build`)
2. **데모 체험**: `smuxapi-demo\bat\start.bat` 더블클릭 → `http://localhost:9090/smuxapi/`
3. **상세 설치**: `docs/INSTALL.md` 참조
4. **API 학습**: `docs/API.md`, `docs/EXAMPLES.md`, 그리고 `lib/doc/` 하위 기능별 가이드 (caching / vision / structured-output / tool-use / embeddings / fallback-telemetry) 참조

### Q: 루트에서 `gradlew` 명령이 안 먹히면?
A: v0.9.2 부터 루트가 Gradle multi-project 루트로 평탄화되었습니다.
- `gradlew :lib:build` — 라이브러리 빌드
- `gradlew :smuxapi-demo:bootRun` — 데모 실행
- `gradlew :smuxapi-demo:war` — Tomcat 배포용 WAR 생성
- `gradlew tasks --all` — 전체 사용 가능 태스크 조회

### Q: 어떤 AI 모델을 사용할 수 있나요?
A: OpenAI GPT (Responses API, Assistants API), Google Gemini를 지원합니다. 향후 Claude 등 다른 모델도 지원 예정입니다.

### Q: 기존 웹 애플리케이션에 쉽게 통합할 수 있나요?
A: 네! JAR 파일과 JavaScript 라이브러리만 추가하면 바로 사용 가능합니다. 기존 코드 수정 최소화.

### Q: 어떤 프레임워크를 지원하나요?
A: 순수 HTML5, React, Vue.js 등 모든 웹 기반 UI 프레임워크에서 사용 가능합니다.

### Q: 상업적 이용이 가능한가요?
A: Apache 2.0 라이선스로 상업적 이용이 자유롭습니다.

### Q: API 비용은 얼마나 드나요?
A: Smart UX API 자체는 무료입니다. OpenAI/Gemini API 사용료만 발생합니다.

### Q: 보안은 안전한가요?
A: API Key는 서버에서만 관리되며, 클라이언트에 노출되지 않습니다. 자세한 내용은 [SECURITY.md](SECURITY.md)를 참조하세요.

더 많은 질문은 [Discussions](https://github.com/kiunsea/smart-ux-api/discussions)에서 확인하세요!

## 📊 Roadmap

### v1.0.0 (계획 중)
- [ ] 안정화 및 프로덕션 레디
- [ ] 상세 문서 및 튜토리얼
- [ ] 커뮤니티 에코시스템

제안사항이 있으시면 [Feature Request](https://github.com/kiunsea/smart-ux-api/issues/new?template=feature_request.md)를 등록해 주세요!

## 🧑‍💻 기여 가이드

기여를 환영합니다! 다음과 같은 방법으로 참여하실 수 있습니다:

- 🐛 [버그 신고](https://github.com/kiunsea/smart-ux-api/issues/new?template=bug_report.md)
- ✨ [기능 제안](https://github.com/kiunsea/smart-ux-api/issues/new?template=feature_request.md)
- 📝 문서 개선
- 💻 Pull Request 제출

자세한 내용은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참조해 주세요.

💬 문의: **kiunsea@gmail.com**

## 🙌 Acknowledgments

- [OpenAI](https://openai.com) - GPT API 제공
- [Google](https://ai.google.dev) - Gemini API 제공
- [Apache Software Foundation](https://www.apache.org) - 오픈소스 라이선스
- 모든 기여자분들께 감사드립니다!

---

## 📄 라이선스

이 프로젝트는 **Apache License, 버전 2.0**에 따라 배포됩니다.

라이선스의 전체 내용은 [LICENSE](LICENSE) 파일을 참조해 주십시오.

---

**Copyright © 2025 [jiniebox.com](https://jiniebox.com)**

---

## 🔗 외부 링크

- Apache License, Version 2.0 (원문): http://www.apache.org/licenses/LICENSE-2.0
- 오픈소스SW 라이선스 종합정보시스템 (Apache-2.0): https://www.olis.or.kr/license/Detailselect.do?lId=1002
- 개발자 홈페이지: https://www.omnibuscode.com
- 문의: kiunsea@gmail.com

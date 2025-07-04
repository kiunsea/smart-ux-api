# Smart UX API

![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

## 🧠 소개

**Smart UX API**는 기존 웹 애플리케이션에 인공지능 기반 화면 제어 기능을 쉽게 통합할 수 있도록 지원하는 오픈소스 도구입니다.  
사용자가 전달하는 텍스트 기반 요청을 분석하여, 해당 요청을 수행하기 위해 어떤 UI 요소에 어떤 액션을 취해야 하는지를 AI가 판단하고 실행 가능한 **액션 프로세스 정의서**로 응답받아 화면을 자동 제어합니다.

서비스에 익숙하지 않은 사용자에게 직관적인 사용 가이드를 제공하거나, 반복 작업을 AI를 통해 자동화하고자 할 때 특히 유용합니다.

## 🔍 주요 기능

- 기존 Java 웹 애플리케이션 서비스에 손쉽게 통합 가능
- 웹 화면 내 UI 구성 요소를 자동 수집
- 사용자의 자연어 요청을 AI에게 전달
- AI로부터 화면 제어를 위한 액션 프로세스 정의서 수신
- 액션 프로세스로 화면 상에서 필요한 액션을 자동 실행

## 🖱️ 설치 방법

Smart UX API는 기존 또는 신규 Java 기반 웹 애플리케이션에 쉽게 통합할 수 있습니다.

### 1. 📦 JAR 파일 추가

- `lib/build/libs/smart-ux-api.jar`를 웹 애플리케이션의 `/WEB-INF/lib/` 디렉토리에 추가합니다.

### 2. 📦 JS 라이브러리 포함

- `lib/src/main/js/*.js`를 웹 애플리케이션의 `[DOC ROOT]/suapi` 디렉토리에 추가합니다.
- 웹 페이지에 다음 스크립트를 추가합니다:

```html
<script src="/suapi/smart-ux-client.js"></script>
<script src="/suapi/smart-ux-collector.js"></script>

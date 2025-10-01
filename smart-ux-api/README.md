# Smart UX API

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.html)
![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

---

## 🖱️ 설치 방법

**Smart UX API**는 Java 기반 웹 애플리케이션(기존 또는 신규)에 손쉽게 통합할 수 있습니다.

### 1️⃣ JAR 파일 추가

* `lib/build/libs/smart-ux-api.jar` 파일을 웹 애플리케이션의 `/WEB-INF/lib/` 디렉터리에 복사합니다.

### 2️⃣ JS 라이브러리 포함

* `lib/src/main/js/*.js` 파일을 웹 애플리케이션의 `[DOC ROOT]/smuxapi` 디렉터리에 추가합니다.
* 웹 페이지에 다음 스크립트를 포함시킵니다:

```html
<script src="/smuxapi/smart-ux-client.js"></script>
<script src="/smuxapi/smart-ux-collector.js"></script>
```

---

## 🧊 주요 API 개요

### 1️⃣ ChatRoom

AI 대화 시 기존 대화 내용을 유지하는 **대화 저장소** 역할을 합니다.

### 2️⃣ Chatting

ChatRoom 내에서 진행되는 **하나의 대화 세트**를 의미합니다.
사용자의 프롬프트를 전달하고, AI의 응답을 받을 수 있습니다.

### 3️⃣ Assistant

OpenAI **Assistants API**를 사용하는 경우 필요한 기능을 제공합니다.

---

## 📋 사용 방법

**사용 예제**는 GitHub Repository의 `smart-ux-api/smuxapi-war` 프로젝트를 참고하세요.

### 1️⃣ AI 모델 API 등록

사용하는 AI 모델에 따라 해당 AI서비스 API Key가 필요합니다.

* OpenAI Responses / Assistants API
  - OpenAI API Key 발급 필요
    👉 [API Key 발급 링크](https://platform.openai.com/settings/organization/api-keys)
  - Assistants API를 사용할 경우 **Assistant ID**도 필요합니다.
    👉 [Assistants 문서](https://platform.openai.com/docs/assistants)

* Google Gemini API
  - [Google Cloud Console](https://console.cloud.google.com)에서 API Key를 생성하세요.

### 2️⃣ User Interaction Flow 문서 작성

AI가 **UI를 제어할 때 필요한 작업 흐름(Work Flow)** 을 정의한 **JSON 문서**를 작성합니다.
서비스 초기화 시 자동 로딩되어 AI에 전달됩니다.

### 3️⃣ 프롬프트 메시지 전송을 위한 기본 인스턴스 생성

* OpenAI Responses / Google Gemini
  - `ResponsesChatRoom`, `ResponsesChatting`
  - `GeminiChatRoom`, `GeminiChatting`
* OpenAI Assistant
  - `Assistant`
  - `AssistantsThread`
  - `AssistantsMessage`

### 4️⃣ 서비스 초기화 및 사용자 요청 처리를 위한 Servlet 요구 사항
다음과 같은 프로세스를 통해 AI로부터 Action Queue를 응답받아 동작하게 됩니다.
* **User Interaction Flow 문서 전송** (사용자 세션 최초 생성 시 1회 실행)
* **현재 화면 정보(CurrentViewInfo) 전송** (UX Info Servlet)
* **사용자 프롬프트 메시지 전송 및 응답 처리** (Action Queue Servlet)

---

## 🧑‍💻 기여 가이드

Pull Request 또는 Issue를 통해 다음과 같은 기여가 가능합니다:

* 🐞 버그 수정
* ✨ 기능 제안 및 개선
* 📝 문서 보강

👉 문의: **[kiunsea@gmail.com](mailto:kiunsea@gmail.com)**

---

## 📄 라이선스

이 프로젝트는 **Apache License, 버전 2.0**에 따라 배포됩니다.

라이선스의 전체 내용은 [LICENSE](../LICENSE) 파일을 참조해 주십시오.

---

**Copyright [2025] [kiunsea@gmail.com]**

---

## 🔗 외부 링크

- Apache License, Version 2.0 (원문): http://www.apache.org/licenses/LICENSE-2.0
- 오픈소스SW 라이선스 종합정보시스템 (Apache-2.0): https://www.olis.or.kr/license/Detailselect.do?lId=1002

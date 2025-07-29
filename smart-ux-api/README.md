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

* `lib/src/main/js/*.js` 파일을 웹 애플리케이션의 `[DOC ROOT]/smuapi` 디렉터리에 추가합니다.
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

OpenAI **Assistants API**를 활용할 때 필요한 기능을 제공합니다.

---

## 📋 사용 방법

**사용 예제**는 GitHub Repository의 `smart-ux-api/smuxapi-war` 프로젝트를 참고하세요.

### 1️⃣ AI 모델 API 등록

AI 모델별로 API Key가 필요합니다.

**OpenAI Responses / Assistants API**

  * OpenAI API Key 발급 필요
    👉 [API Key 발급 링크](https://platform.openai.com/settings/organization/api-keys)
  * Assistants API를 사용할 경우 **Assistant ID**도 필요합니다.
    👉 [Assistants 문서](https://platform.openai.com/docs/assistants)

**Gemini API**

  * [Google Cloud Console](https://console.cloud.google.com)에서 API Key를 생성하세요.

### 2️⃣ User Interaction Flow 문서 작성

AI가 \*\*UI를 제어할 때 필요한 작업 흐름(Work Flow)\*\*을 정의한 **JSON 문서**를 작성합니다.
서비스 초기화 시 자동 로딩되어 AI에 전달됩니다.

### 3️⃣ 프롬프트 메시지 전송을 위한 기본 인스턴스 생성

**OpenAI Responses / Google Gemini**

  * `ResponsesChatRoom`, `ResponsesChatting`
  * `GeminiChatRoom`, `GeminiChatting`
**OpenAI Assistant**

  * `Assistant`
  * `AssistantsThread`
  * `AssistantsMessage`

### 4️⃣ 서비스 초기화 및 사용자 요청 처리를 위한 Servlet 요구 사항

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

이 프로젝트는 \*\*GNU Affero General Public License v3.0 (AGPL-3.0)\*\*을 따릅니다.
자세한 내용은 [LICENSE](./LICENSE) 파일을 참고하세요.

> ⚠️ **AGPL**은 네트워크 사용자를 포함해 소스코드 공개를 요구하는 **엄격한 카피레프트** 라이선스입니다.
> 사용 및 배포 전 반드시 내용을 이해하고 준수해 주세요.

---

## 🔗 외부 링크

* [AGPL 공식 문서](https://www.gnu.org/licenses/agpl-3.0.html)
* [Choose a License 설명](https://choosealicense.com/licenses/agpl-3.0/)


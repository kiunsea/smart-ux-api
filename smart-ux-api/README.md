# Smart UX API

[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.html)
![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

## 🖱️ 설치 방법

Smart UX API는 기존 또는 신규 Java 기반 웹 애플리케이션에 쉽게 통합할 수 있습니다.

### 1. 📦 JAR 파일 추가

- `lib/build/libs/smart-ux-api.jar`를 웹 애플리케이션의 `/WEB-INF/lib/` 디렉토리에 추가합니다.

### 2. 📦 JS 라이브러리 포함

- `lib/src/main/js/*.js`를 웹 애플리케이션의 `[DOC ROOT]/smuapi` 디렉토리에 추가합니다.
- 웹 페이지에 다음 스크립트를 추가합니다:

```html
<script src="/smuxapi/smart-ux-client.js"></script>
<script src="/smuxapi/smart-ux-collector.js"></script>
```

## 🧊 주요 API 소개

### 1. ChatRoom
SmuThread는 AI와 대화시 기존 대화들을 계속해서 유지하는 저장공간입니다.

### 2. Chatting
SmuMessage는 SmuThread내에서 AI와 대화하는 하나의 메세지셋입니다.
사용자 프롬프트를 전송하고 AI의 응답을 전달 받을 수 있습니다.

### 3. Assistant
OpenAI Assistants API를 이용할 경우.

## 📋 사용 방법
사용 예제는 GitHub Repository의 smart-ux-api/smuxapi-war 프로젝트를 참고해 주세요.

### 1. AI Model API 등록
사용할 AI Model에 따라 API Key가 필요합니다.
### 1) OpenAI Assitants API
openai package의 Assistant instance 생성시에 OpenAI API Key와 Assistant ID가 필요합니다.
https://platform.openai.com/docs/assistants 에서 Assistant API 사용을 위한 계정을 등록하고 
API Key 발급과 Assitant 생성을 진행하여 주세요.

### 2) Gemini API
Gemini API Key 생성은 다음의 링크에서 진행해 주세요.
https://console.cloud.google.com

### 2. User Interaction Flow 문서 작성
User Interaction Flow Document(JSON) 작성이 필요합니다.
ui를 control 할 수 있도록 AI가 action queue를 작성할때 필요한 work flow를 정의한 문서입니다.
서비스 초기화시 자동으로 로딩되어 AI에게 전달합니다.

### 3. 프롬프트 메세지 전송을 위한 기본 Instance 생성 (OpenAI Assistant)
- Assistant instance 생성
- AssistantsThread instance 생성
- AssistantsMessage instance 생성

### 4. 서비스 초기화 및 사용자 요청 처리를 위한 Servlet 요구기능
- User Interaction Flow 문서 전송 (사용자 세션 생성시 최초 한번 실행)
- 현재 화면 정보(CurrentViewInfo) 전송 (UX Info Servlet)
- 사용자 프롬프트 메세지 전송 및 응답 처리 (Action Queue Servlet)

## 🧑‍💻 기여 가이드

Pull Request 또는 Issue를 통해 다음에 기여하실 수 있습니다:

- 버그 수정
- 기능 제안 또는 개선
- 문서화 작업
- kiunsea@gmail.com

---

## 📄 라이선스

이 프로젝트는 **GNU Affero General Public License v3.0 (AGPL-3.0)**을 따릅니다.  
상세한 내용은 [LICENSE](./LICENSE) 파일을 참조하세요.

> ⚠️ AGPL은 네트워크 사용자에게도 소스코드 공개를 요구하는 **엄격한 카피레프트** 라이선스입니다.  
> 이를 충분히 이해한 후 사용하거나 배포하세요.

---

## 🔗 외부 링크

- AGPL 공식 문서: https://www.gnu.org/licenses/agpl-3.0.html
- Choose a License 설명: https://choosealicense.com/licenses/agpl-3.0/

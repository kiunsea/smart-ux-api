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
┌─────────────┐  자연어 명령   ┌──────────────────┐  UI 정보 수집   ┌─────────────┐
│   사용자    │ ────────────> │  Smart UX        │ ─────────────> │   Web UI    │
│   (User)    │               │  Collector (JS)  │                │  Elements   │
└─────────────┘               └──────────────────┘                └─────────────┘
                                      │
                                      │ UI 구조 JSON
                                      ▼
┌─────────────┐  Action Queue  ┌──────────────────┐  Prompt +      ┌─────────────┐
│   Web UI    │ <────────────  │  Smart UX        │  UI Info       │   AI API    │
│   Control   │                │  Client (Java)   │ ─────────────> │ (GPT/Gemini)│
└─────────────┘                └──────────────────┘                └─────────────┘
```

1. **UI 수집**: `smart-ux-collector.js`가 웹 페이지의 DOM 요소를 스캔하여 JSON으로 변환
2. **프롬프트 전송**: 사용자 요청과 UI 정보를 AI API에 전달
3. **액션 생성**: AI가 실행 가능한 Action Queue를 생성
4. **자동 실행**: `smart-ux-client.js`가 Action Queue를 파싱하여 UI 제어

## 🚀 Quick Start

### 사전 요구 사항
- Java 17 이상
- Gradle 8.x
- OpenAI API Key 또는 Google Gemini API Key
- 웹 애플리케이션 서버 (Tomcat, Jetty 등)

### 5분만에 시작하기

1. **저장소 클론**
```bash
git clone https://github.com/kiunsea/smux-api.git
cd smux-api
```

2. **라이브러리 빌드**
```bash
cd smart-ux-api/lib
./gradlew build
```

3. **JAR 파일을 웹 애플리케이션에 추가**
```
smart-ux-api/lib/build/libs/smart-ux-api-0.6.0.jar 를 /WEB-INF/lib/ 에 복사
```

4. **JavaScript 라이브러리 포함**
```
smart-ux-api/lib/src/main/js/*.js 를 웹 루트 디렉터리에 복사
```

5. **API Key 설정**
```json
// resources/apikey.json
{
  "openai": {
    "apiKey": "your-api-key",
    "model": "gpt-4"
  }
}
```

6. **HTML에 스크립트 추가**
```html
<script src="/lib/smart-ux-client.js"></script>
<script src="/lib/smart-ux-collector.js"></script>
```

자세한 설치 가이드는 [INSTALL.md](docs/INSTALL.md)를 참조하세요.

### 📦 프로젝트 구성
- **smart-ux-api**: 메인 라이브러리 (Java + JavaScript)
- **smuxapi-war**: 샘플 애플리케이션 (WAR 프로젝트)

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

## 디렉터리 구조
```
smux-api/
├── smart-ux-api/
│   ├── bin/
│   ├── docs/
│   ├── gradle/
│   ├── lib/
│   │   ├── build/
│   │   │   └── libs/
│   │   └── src/
│   │       └── main/
│   │           ├── java/
│   │           ├── js/
│   │           └── resources/
│   ├── LICENSE
│   └── README.md
├── smuxapi-war/
│   ├── src/
│   └── README.md
└── README.md
```

## ❓ FAQ

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

더 많은 질문은 [Discussions](https://github.com/kiunsea/smux-api/discussions)에서 확인하세요!

## 📊 Roadmap

### v1.0.0 (계획 중)
- [ ] 안정화 및 프로덕션 레디
- [ ] 상세 문서 및 튜토리얼
- [ ] 커뮤니티 에코시스템

제안사항이 있으시면 [Feature Request](https://github.com/kiunsea/smux-api/issues/new?template=feature_request.md)를 등록해 주세요!

## 🧑‍💻 기여 가이드

기여를 환영합니다! 다음과 같은 방법으로 참여하실 수 있습니다:

- 🐛 [버그 신고](https://github.com/kiunsea/smux-api/issues/new?template=bug_report.md)
- ✨ [기능 제안](https://github.com/kiunsea/smux-api/issues/new?template=feature_request.md)
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

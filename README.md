# Smart UX API 프로젝트
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL%20v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.html)
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

- 기존 Java 웹 애플리케이션 서비스에 손쉽게 통합 가능
- 웹 화면 내 UI 구성 요소를 자동 수집
- 사용자의 자연어 요청을 AI에게 전달
- AI로부터 화면 제어를 위한 액션 프로세스 정의서 수신
- 액션 프로세스로 화면 상에서 필요한 액션을 자동 실행

## project directories
- smart-ux-api : Main Project (Java+JS Library)
- smuapi-war : Example Project (War)

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
├── smuapi-war/
│   ├── src/
│   └── README.md
└── README.md
```

## 🧑‍💻 기여 가이드

Pull Request 또는 Issue를 통해 다음에 기여하실 수 있습니다:

- 버그 수정
- 기능 제안 또는 개선
- 문서화 작업
- kiunsea@gmail.com

---

## 📄 라이선스

이 프로젝트는 **GNU Affero General Public License v3.0 (AGPL-3.0)**을 따릅니다.  
상세한 내용은 [LICENSE](https://www.gnu.org/licenses/agpl-3.0.html) 파일을 참조하세요.

> ⚠️ AGPL은 네트워크 사용자에게도 소스코드 공개를 요구하는 **엄격한 카피레프트** 라이선스입니다.  
> 이를 충분히 이해한 후 사용하거나 배포하세요.

---

## 🔗 외부 링크

- AGPL 공식 문서: https://www.gnu.org/licenses/agpl-3.0.html
- Choose a License 설명: https://choosealicense.com/licenses/agpl-3.0/

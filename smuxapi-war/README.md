# smuxapi-war 🧪

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

📁 **smuxapi-war**는 **Smart UX API** 프로젝트의 **배포 라이브러리 테스트용 샘플 프로젝트**입니다.
새로운 기능을 실험하고, 설정을 검증하며, 협업용 코드 베이스로도 활용할 수 있습니다.

---

## 🔧 목적

* ✅ 기능 개발 전 **테스트 환경 구축**
* ✅ **빌드 및 배포 과정** 실습
* ✅ 프로젝트 구조 및 협업 절차 연습
* ✅ 다양한 **라이브러리 및 프레임워크 적용 테스트**

---

## 📦 프로젝트 구성

```plaintext
smuxapi-war/
├── src/                     
│   └── main/
│       ├── java/            # 예제 소스코드
│       │    └── resources/  # 설정 파일 및 정적 자원
│       └── webapp/
└── README.md
```

---

## ▶️ 설정 및 설치

### 1️⃣ **smuxapi.properties 설정**

* `/smuxapi-war/src/main/java/resources/smuxapi.properties` 파일 생성
  (예제 파일: `def.smuxapi.properties` 참고)
* 아래 항목을 환경에 맞게 설정:

  * `OPENAI_API_KEY`, `OPENAI_MODEL`, `OPENAI_ASSIST_ID`
  * `GEMINI_API_KEY`, `GEMINI_MODEL`

### 2️⃣ **프로젝트 Export (Eclipse 기준)**

1. **Project Explorer**에서 마우스 우클릭
2. **Export > WAR file** 선택
3. 생성된 WAR 파일을 \*\*WAS(Web Application Server)\*\*에 배포

---

## ✍️ 실행 절차

### 🔤 **Text Prompt 입력 테스트**

1. `mega.html` 페이지 열기 → 키오스크 화면 진입
2. 상단 입력창(`Please order~`)에 주문할 메뉴 입력

   * 예: `아이스 아메리카노와 따뜻한 레몬차`
3. 입력창 우측 **마이크 아이콘 클릭** → AI에게 프롬프트 전송
4. 결과 확인
5. 추가 프롬프트 입력 후 다시 전송

   * 예: `시원한 사과유자차 주문해줘`
6. 주문 확인 후 `"결재하기"` 입력 → 전송
7. 주문 세부내역 창 확인

---

### 🎤 **Audio Prompt 입력 테스트**

1. `mega_speech.html` 페이지 열기 → 키오스크 화면 진입
2. 프롬프트 입력창 우측 **마이크 아이콘 클릭** → 음성 입력 대기
3. 음성 명령 후 자동으로 텍스트 변환 & 프롬프트 전송

   * 예: `시원한 토피넛 마끼아또 주문하고 결제하기 눌러줘`
4. 주문 세부내역 창 확인

---

## ⚠️ 주의사항

> 이 프로젝트는 **프로덕션 용도**가 아닌, **개발 및 실험 목적**으로만 사용합니다.

---

## 📄 라이선스

* 이 샘플 프로젝트의 **Class**는 [MIT](LICENSE) 라이선스를 따릅니다.
* 포함된 **jar 패키지**는 각 프로젝트의 라이선스 정책을 따릅니다.
* 자세한 내용은 **LICENSE 파일**을 참고하세요.

---

## 🙋‍♀️ 문의 및 기여

* 💬 기여 및 피드백은 언제나 환영합니다!
* **Pull Request** 또는 **Issues**를 통해 참여해주세요.

📩 문의: **[kiunsea@gmail.com](mailto:kiunsea@gmail.com)**
# suapi-war 🧪

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

📁 **suapi-war**는 smart-ux-api 프로젝트의 배포 라이브러리를 테스트 하기 위한 샘플 프로젝트입니다.
새로운 기능을 실험하거나, 설정을 검증하거나, 협업을 위한 코드 베이스로 사용하기 위한 예제입니다.

---

## 🔧 목적

- 기능 개발 전 테스트 환경 구축
- 빌드 및 배포 과정 실습
- 프로젝트 구조 및 협업 절차 연습
- 각종 라이브러리 및 프레임워크 적용 테스트

---

## 📦 프로젝트 구성

```
suapi-war/
├── src/                     
│   └── main/
│       ├── java/            # 예제 소스코드
│       │    └── resources/  # 설정 파일 및 정적 자원
│       └── webapp/
└── README.md
```

---

## ▶️ 설정 및 설치 방법

**suapi.properties설정**
- /suapi-war/src/main/java/resources/suapi.properties 파일을 생성 (def.suapi.properties 참고)
- OPENAI_API_KEY, OPENAI_ASSIST_ID 값을 설정

**project export (Eclipse Tool 기준)**
- Project Explorer 에서 마우스 우클릭
- Export > WAR file 선택
- war파일을 was에 배포

---

## ✍️ 실행 절차

**text prompt 입력 테스트**
- mega.html 페이지 오픈후 kiosk 화면에 진입
- 최상단의 프롬프트 입력창("Please order~")에 화면상에 보이는 메뉴를 선택하는 프롬프트 입력
  ex)'아이스 아메리카노와 따뜻한 레몬차'
- 입력창의 우측의 마이크 아이콘 클릭(AI에게 프롬프트 전송)
- 결과 확인
- 추가 프롬프트 입력후 마이크 아이콘 클릭
  ex)'시원한 사과유자차 주문해줘'
- 주문 확인
- 프롬프트 입력란에 '결재하기' 입력후 전송
- 주문 세부내역창 확인

**audio prompt 입력 테스트**
- mega_speech.html 페이지 오픈후 kiosk 화면에 진입
- 프롬프트 입력창의 우측에 위치한 마이크 아이콘을 눌러 음성 입력 대기 상태로 진입
- 음성 명령을 내리고 대기하면 자동으로 텍스트 전환하여 프롬프트가 전송됨
  ex) '시원한 토피넛 마끼아또 주문하고 결제하기 눌러줘'
- 주문 세부내역창 확인

---

## ⚠️ 주의사항

> 이 프로젝트는 **프로덕션 용도**가 아닌, 개발 및 분석 용도로 사용합니다.

---

## 📄 라이선스

이 샘플 프로젝트의 Class는 [MIT](LICENSE) 자유 라이선스를 사용합니다.
함께 포함된 jar package는 개별 프로젝트의 라이선스 정책을 따르므로 유의하시기 바랍니다.
자세한 내용은 LICENSE 파일을 참조하세요.

---

## 🙋‍♀️ 문의 및 기여

기여나 피드백은 언제든지 환영합니다!  
Pull Request 또는 Issues를 통해 참여해주세요.

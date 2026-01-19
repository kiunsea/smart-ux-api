# smuxapi-demo 배포 패키지

## 📦 패키지 구성

이 ZIP 파일에는 다음이 포함되어 있습니다:

- `smuxapi-demo-{version}.jar` - 실행 가능한 Spring Boot JAR 파일
- `jre/` - Custom JRE (Java 설치 불필요)
- `smuxapi-demo.bat` - 실행 배치 파일
- `smuxapi-demo.yml` - 설정 파일 (API 키 설정)
- `README.md` - 이 파일

## 🚀 실행 방법

### 1. ZIP 파일 압축 해제

`smuxapi-demo.zip` 파일을 원하는 위치에 압축 해제합니다.

예:
```
C:\smuxapi-demo\
├── smuxapi-demo-0.6.0.jar
├── jre/
├── smuxapi-demo.bat
├── smuxapi-demo.yml
└── README.md
```

### 2. 실행

`smuxapi-demo.bat` 파일을 더블클릭하거나 명령 프롬프트에서 실행합니다:

```cmd
smuxapi-demo.bat
```

### 3. 브라우저 접속

실행하면 자동으로 브라우저가 열립니다.

**접속 주소**: `http://localhost:8080/smuxapi/`

## ⚙️ 시스템 요구사항

- **운영체제**: Windows 10/11 (64bit)
- **Java**: 설치 불필요 (번들된 JRE 포함)
- **디스크 공간**: 최소 200MB
- **메모리**: 최소 512MB RAM

## 🔧 설정

### API 키 설정

애플리케이션 실행 전에 `smuxapi-demo.yml` 파일을 수정하여 API 키를 설정해야 합니다.

JAR 파일과 같은 디렉터리에 있는 `smuxapi-demo.yml` 파일을 수정하면 됩니다.

**설정 항목:**
```yaml
OPENAI_ASSIST_ID: your assistant id
OPENAI_API_KEY: your openai api key
OPENAI_MODEL: openai ai model
GEMINI_MODEL: google gemini ai model (gemini-2.5-flash ...)
GEMINI_API_KEY: your gemini api key
```

## 📝 로그

로그 파일은 `logs/smuxapi-demo.log`에 저장됩니다.

## ❓ 문제 해결

### Java를 찾을 수 없습니다

- `jre` 폴더가 ZIP 파일과 함께 압축 해제되었는지 확인
- 시스템에 Java 17 이상이 설치되어 있는지 확인

### 포트 충돌

다른 프로그램이 8080 포트를 사용 중일 수 있습니다.

**해결 방법:**
```cmd
# 포트 사용 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료
taskkill /F /PID [PID번호]
```

또는 `application.yml` 파일에서 포트를 변경할 수 있습니다.

### 브라우저가 자동으로 열리지 않음

수동으로 브라우저에서 접속:
```
http://localhost:8080/smuxapi/
```

## 📚 더 많은 정보

프로젝트에 대한 자세한 정보는 GitHub 저장소를 참조하세요:
https://github.com/kiunsea/smart-ux-api

---

**Copyright © 2025 [jiniebox.com](https://jiniebox.com)**

# smuxapi-demo 🚀

Smart UX API의 독립 실행형 데모 애플리케이션입니다.

## 📋 프로젝트 개요

`smuxapi-demo`는 JRE와 웹 서버를 내장하여 Java 설치 없이 독립 실행 가능한 Spring Boot 애플리케이션입니다.

## 🎯 주요 특징

- ✅ **독립 실행**: Java 설치 불필요 (Custom JRE 번들)
- ✅ **자동 브라우저 실행**: 실행 시 웹 브라우저 자동 열림
- ✅ **간편한 배포**: ZIP 파일 하나로 배포
- ✅ **Spring Boot 기반**: 내장 Tomcat으로 실행

## 🏗️ 프로젝트 구조

```
smuxapi-demo/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/smartuxapi/demo/
│       │       ├── SmuxapiDemoApplication.java
│       │       ├── BrowserLauncher.java
│       │       ├── config/
│       │       ├── controller/
│       │       └── service/
│       ├── resources/
│       │   ├── application.yml
│       │   ├── log4j2.xml
│       │   └── smuxapi-demo.yml
│       └── webapp/              ← 웹 리소스
├── packaging/
│   └── distribution/
│       ├── smuxapi-demo.bat
│       └── README.md
├── build.gradle.kts
└── README.md
```

## 🔧 빌드 방법

### 사전 요구사항

- Java 17 이상 (JDK, JRE 아님)
- Gradle (프로젝트에 Gradle Wrapper 포함)

### 빌드 명령

**Windows:**
```cmd
# 저장소 루트에서 실행
cd smart-ux-api

# 배포 패키지 생성 (권장: 빌드 + 테스트 + 패키징)
.\gradlew.bat :smuxapi-demo:deploy
```

**Linux/Mac:**
```bash
# 저장소 루트에서 실행
cd smart-ux-api

# 배포 패키지 생성 (권장: 빌드 + 테스트 + 패키징)
./gradlew :smuxapi-demo:deploy
```

또는 단계별로 실행:

```bash
# 전체 프로젝트 빌드
./gradlew :smuxapi-demo:bootJar  (Linux/Mac)
.\gradlew.bat :smuxapi-demo:bootJar  (Windows)

# Custom JRE 생성
./gradlew :smuxapi-demo:createJre  (Linux/Mac)
.\gradlew.bat :smuxapi-demo:createJre  (Windows)

# 배포 패키지 생성 (ZIP)
./gradlew :smuxapi-demo:packageDist  (Linux/Mac)
.\gradlew.bat :smuxapi-demo:packageDist  (Windows)
```

### 빌드 결과

- **JAR 파일**: `smuxapi-demo/build/libs/smuxapi-demo-{version}.jar`
- **Custom JRE**: `smuxapi-demo/build/jre/`
- **배포 패키지**: `smuxapi-demo/packaging/distribution/smuxapi-demo.zip`

## 🚀 실행 방법

### 개발 모드

**Windows:**
```cmd
cd smart-ux-api
.\gradlew.bat :smuxapi-demo:bootRun
```

**Linux/Mac:**
```bash
cd smart-ux-api
./gradlew :smuxapi-demo:bootRun
```

### 배포 패키지 실행

1. `smuxapi-demo.zip` 파일 압축 해제
2. `smuxapi-demo.bat` 실행

자세한 내용은 `packaging/distribution/README.md`를 참조하세요.


## ⚙️ 설정

### application.yml

서버 포트 및 기타 설정을 변경할 수 있습니다:

```yaml
server:
  port: 8080
```

### API 키 설정

실행 디렉터리의 `smuxapi-demo.yml` 파일을 수정 (배포 패키지에 포함됨):

```yaml
OPENAI_ASSIST_ID: your assistant id
OPENAI_API_KEY: your openai api key
OPENAI_MODEL: openai ai model
GEMINI_MODEL: google gemini ai model (gemini-2.5-flash ...)
GEMINI_API_KEY: your gemini api key
```

## 📦 배포 및 테스트

### 배포 패키지 테스트

1. **배포 패키지 위치 확인**
   ```
   smuxapi-demo/packaging/distribution/smuxapi-demo.zip
   ```

2. **테스트 환경 준비**
   - ZIP 파일을 임시 디렉터리에 압축 해제
   - 예: `C:\temp\smuxapi-demo-test\`

3. **API 키 설정**
   - 압축 해제된 폴더의 `smuxapi-demo.yml` 파일을 열어 API 키 설정
   ```yaml
   OPENAI_ASSIST_ID: your_actual_assistant_id
   OPENAI_API_KEY: your_actual_openai_api_key
   OPENAI_MODEL: gpt-4
   GEMINI_MODEL: gemini-2.0-flash-exp
   GEMINI_API_KEY: your_actual_gemini_api_key
   ```

4. **실행 및 테스트**
   - `smuxapi-demo.bat` 파일 실행
   - 브라우저가 자동으로 열리면 `http://localhost:8080/smuxapi/` 접속 확인
   - 웹 애플리케이션 기능 테스트

5. **로그 확인**
   - 실행 디렉터리의 `logs/smuxapi-demo.log` 파일에서 오류 확인

### 배포 패키지 구조

```
smuxapi-demo/
├── smuxapi-demo-{version}.jar
├── jre/                        ← Custom JRE
├── smuxapi-demo.bat            ← 실행 파일
├── smuxapi-demo.yml            ← 설정 파일
└── README.md                   ← 배포 가이드
```

## 🛠️ 기술 스택

- **Java**: 17
- **Spring Boot**: 3.2.0
- **빌드 도구**: Gradle (Kotlin DSL)
- **웹 서버**: Embedded Tomcat
- **JRE 번들링**: jlink

## 📚 관련 문서

- [배포 가이드](packaging/distribution/README.md)
- [메인 라이브러리 문서](../smart-ux-api/README.md)

## 📝 라이선스

Apache License 2.0

---

**Copyright © 2025 [jiniebox.com](https://jiniebox.com)**

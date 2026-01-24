# Smart UX API 테스트 가이드

이 문서는 Smart UX API 프로젝트의 테스트 실행 방법과 결과 확인 방법을 설명합니다.

## 📋 목차

- [테스트 실행 방법](#테스트-실행-방법)
- [테스트 결과 확인](#테스트-결과-확인)
- [통합 테스트 스위트](#통합-테스트-스위트)
- [개별 테스트 실행](#개별-테스트-실행)
- [문제 해결](#문제-해결)

## 🚀 테스트 실행 방법

### 1. Gradle을 사용한 실행 (권장)

#### 모든 테스트 실행
```bash
# Windows
gradlew.bat test

# Linux/Mac
./gradlew test
```

#### 상세 로그와 함께 실행
```bash
gradlew.bat test --info
```

#### 빌드 정리 후 실행
```bash
gradlew.bat clean test
```

### 2. 스크립트를 사용한 실행

#### Windows
```bash
cd lib
run-tests.bat
run-tests.bat --info    # 상세 로그
run-tests.bat --clean   # 정리 후 실행
```

#### Linux/Mac
```bash
cd lib
chmod +x run-tests.sh
./run-tests.sh
./run-tests.sh --info    # 상세 로그
./run-tests.sh --clean   # 정리 후 실행
```

### 3. IDE에서 실행

#### IntelliJ IDEA / Eclipse
1. `AllTests.java` 파일을 열기
2. 클래스 이름 옆의 실행 버튼 클릭
3. 또는 패키지 탐색기에서 `src/test/java` 폴더를 우클릭 → "Run All Tests"

#### VS Code
1. Java Extension Pack 설치
2. `AllTests.java` 파일 열기
3. "Run Test" 링크 클릭

## 📊 테스트 결과 확인

### HTML 리포트
테스트 실행 후 자동으로 생성되는 HTML 리포트를 확인할 수 있습니다:

```
build/reports/tests/test/index.html
```

이 리포트에는 다음 정보가 포함됩니다:
- 전체 테스트 통계 (성공/실패/건너뜀)
- 각 테스트 케이스의 실행 시간
- 실패한 테스트의 상세 에러 메시지
- 테스트 실행 로그

### 콘솔 출력
테스트 실행 시 콘솔에 다음 정보가 출력됩니다:
- 테스트 실행 진행 상황
- 실패한 테스트 목록
- 테스트 결과 요약

### XML 리포트
CI/CD 통합을 위한 XML 리포트도 생성됩니다:

```
build/test-results/test/TEST-*.xml
```

## 🎯 통합 테스트 스위트

`AllTests.java`는 모든 테스트를 통합 실행하는 스위트 클래스입니다.

### 포함되는 테스트 패키지
- `com.smartuxapi` - 기본 유틸리티 테스트
- `com.smartuxapi.ai` - AI 핸들러 테스트
- `com.smartuxapi.ai.openai` - OpenAI 관련 테스트
- `com.smartuxapi.ai.gemini` - Gemini 관련 테스트
- `com.smartuxapi.util` - 유틸리티 클래스 테스트

### 실행 방법
```bash
# Gradle로 실행
gradlew.bat test --tests "com.smartuxapi.AllTests"

# 또는 IDE에서 AllTests.java 실행
```

## 🔍 개별 테스트 실행

특정 테스트 클래스나 메서드만 실행할 수 있습니다:

```bash
# 특정 테스트 클래스 실행
gradlew.bat test --tests "com.smartuxapi.util.StringUtilTest"

# 특정 테스트 메서드 실행
gradlew.bat test --tests "com.smartuxapi.util.StringUtilTest.testIsNumber"

# 패턴 매칭
gradlew.bat test --tests "*Test.test*"
```

## 🛠️ 문제 해결

### 테스트가 실행되지 않는 경우

1. **의존성 확인**
   ```bash
   gradlew.bat dependencies --configuration testRuntimeClasspath
   ```

2. **빌드 정리 후 재실행**
   ```bash
   gradlew.bat clean test
   ```

3. **Gradle 캐시 정리**
   ```bash
   gradlew.bat clean --refresh-dependencies
   ```

### 테스트 실패 시

1. **HTML 리포트 확인**
   - `build/reports/tests/test/index.html` 파일 열기
   - 실패한 테스트 클릭하여 상세 정보 확인

2. **로그 확인**
   ```bash
   gradlew.bat test --info --stacktrace
   ```

3. **특정 테스트만 실행하여 디버깅**
   ```bash
   gradlew.bat test --tests "실패한테스트클래스명" --info
   ```

### 환경 변수 설정

일부 테스트는 API 키가 필요할 수 있습니다:

```bash
# Windows
set OPENAI_API_KEY=your_key_here
set GEMINI_API_KEY=your_key_here

# Linux/Mac
export OPENAI_API_KEY=your_key_here
export GEMINI_API_KEY=your_key_here
```

### 설정 파일

테스트 실행 시 다음 설정 파일들이 사용됩니다:

#### config.json

`src/main/resources/config.json` 파일에서 디버그 모드 및 프롬프트 설정을 로드합니다.

```json
{
  "debug-mode": false,
  "debug-output-path": "./conversation_log/",
  "debug-file-prefix": "chatroom",
  "prompt": { ... }
}
```

- `debug-mode`: `true`로 설정하면 테스트 중 대화 내용이 파일로 저장됩니다.
- `debug-output-path`: 로그 파일 저장 경로

#### apikey.json (선택)

API 통합 테스트를 실행하려면 `def.apikey.json`을 복사하여 `apikey.json`으로 이름을 변경하고 실제 API 키를 입력하세요.

```bash
cp src/main/resources/def.apikey.json src/main/resources/apikey.json
# apikey.json 파일을 편집하여 실제 API 키 입력
```

> ⚠️ `apikey.json` 파일은 `.gitignore`에 포함되어 있어 커밋되지 않습니다.

## 📝 테스트 작성 가이드

새로운 테스트를 추가할 때는 다음 규칙을 따르세요:

1. **JUnit 5 사용**
   ```java
   import org.junit.jupiter.api.Test;
   import static org.junit.jupiter.api.Assertions.*;
   ```

2. **명확한 테스트 이름**
   ```java
   @Test
   @DisplayName("화면 정보 변경 감지 테스트")
   void testViewInfoChangeDetection() {
       // 테스트 코드
   }
   ```

3. **적절한 패키지 구조**
   - 테스트 클래스는 테스트 대상과 동일한 패키지 구조 유지
   - 예: `com.smartuxapi.ai.ActionQueueHandler` → `com.smartuxapi.ai.ActionQueueHandlerTest`

## 🔗 관련 문서

- [JUnit 5 사용자 가이드](https://junit.org/junit5/docs/current/user-guide/)
- [Gradle 테스트 가이드](https://docs.gradle.org/current/userguide/java_testing.html)


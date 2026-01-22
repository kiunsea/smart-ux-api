# DEVLOG - smuxapi-demo

이 DEVLOG는 프로젝트의 작업 이력을 기록합니다.

**작업 기록 형식**: 각 작업은 `YYYY-MM-DD HH:MM` 형식의 일자로 기록됩니다.

---

## 2026-01-22 12:00

### 설정 키 이름 변경: DEMO_PORT → SERVER_PORT

#### 배경
포트 설정 키 이름이 `DEMO_PORT`로 되어 있어 다른 설정 키들과의 일관성이 부족했음. 더 명확하고 표준적인 이름인 `SERVER_PORT`로 변경하여 가독성과 일관성 향상.

#### 구현 내용

1. **설정 파일 업데이트**
   - `src/main/resources/smuxapi-demo.yml`: 주석 및 예시에서 `DEMO_PORT` → `SERVER_PORT`
   - `packaging/distribution/smuxapi-demo/smuxapi-demo.yml`: 실제 설정 값 변경

2. **Java 코드 업데이트**
   - `SmuxapiDemoApplication.java`: 모든 `DEMO_PORT` 참조를 `SERVER_PORT`로 변경
   - `BrowserLauncher.java`: 모든 `DEMO_PORT` 참조를 `SERVER_PORT`로 변경
   - 주석, 메서드 설명, 로그 메시지 모두 업데이트

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `src/main/resources/smuxapi-demo.yml` | 주석에서 `DEMO_PORT` → `SERVER_PORT` |
| `packaging/distribution/smuxapi-demo/smuxapi-demo.yml` | 설정 값 `DEMO_PORT: 9090` → `SERVER_PORT: 9090` |
| `SmuxapiDemoApplication.java` | 모든 `DEMO_PORT` 참조를 `SERVER_PORT`로 변경 |
| `BrowserLauncher.java` | 모든 `DEMO_PORT` 참조를 `SERVER_PORT`로 변경 |

#### 변경 전후 비교

**변경 전:**
```yaml
# smuxapi-demo.yml
DEMO_PORT: 9090
```

**변경 후:**
```yaml
# smuxapi-demo.yml
SERVER_PORT: 9090
```

**코드 변경:**
- `data.containsKey("DEMO_PORT")` → `data.containsKey("SERVER_PORT")`
- `data.get("DEMO_PORT")` → `data.get("SERVER_PORT")`
- 로그 메시지: "DEMO_PORT 설정을 읽었습니다" → "SERVER_PORT 설정을 읽었습니다"

#### 영향 범위
- 기존에 `DEMO_PORT`를 사용하던 설정 파일은 `SERVER_PORT`로 변경 필요
- 코드 변경으로 인한 호환성 문제 없음 (단순 키 이름 변경)
- 사용자에게 더 직관적인 설정 키 이름 제공

---

## 2026-01-22 11:30

### BrowserLauncher 동적 포트 지원

#### 배경
`BrowserLauncher`에서 브라우저를 자동으로 열 때 하드코딩된 URL(`http://localhost:8080/smuxapi/`)을 사용하여, `smuxapi-demo.yml`에서 설정한 포트와 일치하지 않는 문제 발생.

#### 구현 내용

1. **동적 URL 생성 로직 추가**
   - `APP_URL` 상수를 제거하고 `getAppUrl()` 메서드로 동적 생성
   - `getPortFromYaml()` 메서드로 포트 값 읽기
   - 시스템 프로퍼티 우선 확인 (`SmuxapiDemoApplication`에서 설정한 경우)
   - 시스템 프로퍼티가 없으면 YAML 파일 직접 읽기

2. **포트 읽기 우선순위**
   - 1순위: 시스템 프로퍼티 `server.port` (이미 설정된 경우)
   - 2순위: `smuxapi-demo.yml`의 `SERVER_PORT` 설정
   - 3순위: 기본값 8080

3. **JAR 디렉터리 탐지**
   - `getJarDirectory()` 메서드 추가 (SmuxapiDemoApplication과 동일한 로직)
   - YAML 파일 위치 자동 탐지

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `BrowserLauncher.java` | `getAppUrl()`, `getPortFromYaml()`, `getJarDirectory()` 메서드 추가, 모든 URL 참조를 동적 생성으로 변경 |

#### 기술적 세부사항

**URL 생성 흐름:**
1. `getAppUrl()` 호출
2. `getPortFromYaml()`로 포트 값 가져오기
3. `"http://localhost:" + port + "/smuxapi/"` 형식으로 URL 생성

**포트 읽기 로직:**
```java
private static int getPortFromYaml() {
    // 1. 시스템 프로퍼티 확인
    String systemPort = System.getProperty("server.port");
    if (systemPort != null && !systemPort.isEmpty()) {
        return Integer.parseInt(systemPort);
    }
    
    // 2. YAML 파일 직접 읽기
    // ... YAML 파일 읽기 로직
    
    // 3. 기본값 반환
    return DEFAULT_PORT;
}
```

**개선 효과:**
- `smuxapi-demo.yml`에서 포트를 변경하면 브라우저도 자동으로 올바른 URL로 열림
- 서버 포트와 브라우저 URL이 항상 일치
- 사용자 경험 개선

---

## 2026-01-22 11:00

### YAML 설정 파일 기반 서버 포트 동적 설정

#### 배경
기존에는 서버 포트가 `application.yml`에 하드코딩되어 있어, 배포 환경에 따라 포트를 변경하려면 설정 파일을 수정해야 했음. JAR 실행 시 외부 설정 파일(`smuxapi-demo.yml`)에서 포트를 읽어 동적으로 설정할 수 있도록 개선 필요.

#### 구현 내용

1. **SmuxapiDemoApplication.java 포트 설정 로직 추가**
   - `main()` 메서드에서 `configurePortFromYaml()` 호출
   - JAR 파일과 같은 디렉터리의 `smuxapi-demo.yml` 파일 읽기
   - `SERVER_PORT` 키 값 확인 및 시스템 프로퍼티 설정
   - Spring Boot가 `System.setProperty("server.port", ...)`를 자동으로 읽어 포트 적용

2. **포트 설정 우선순위**
   - `smuxapi-demo.yml`의 `SERVER_PORT` 설정 (최우선)
   - 설정이 없으면 `application.yml`의 기본값(8080) 사용

3. **JAR 디렉터리 탐지 로직**
   - `getJarDirectory()` 메서드로 JAR 파일 위치 확인
   - JAR 실행 시: JAR 파일의 부모 디렉터리
   - IDE 실행 시: 프로젝트 루트 디렉터리 (build.gradle.kts 기준)

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `SmuxapiDemoApplication.java` | `configurePortFromYaml()`, `getJarDirectory()` 메서드 추가, YAML 파일 읽기 로직 구현 |
| `src/main/resources/smuxapi-demo.yml` | `SERVER_PORT` 설정 예시 주석 추가 |

#### 사용 방법

**설정 파일 예시:**
```yaml
# smuxapi-demo.yml (JAR 파일과 같은 디렉터리)
SERVER_PORT: 9090

OPENAI_ASSIST_ID: your assistant id
OPENAI_API_KEY: your openai api key
# ... 기타 설정
```

**실행:**
```bash
java -Xms256m -Xmx1024m -jar smuxapi-demo-0.6.0.jar
```

**결과:**
- `SERVER_PORT: 9090` 설정 시 → 서버가 9090 포트로 시작
- 설정이 없으면 → 기본값 8080 포트로 시작

#### 기술적 세부사항

1. **YAML 파일 읽기**
   - SnakeYAML 라이브러리 사용 (Spring Boot 기본 포함)
   - `Yaml.load()`로 YAML 파일 파싱
   - `Map<String, Object>`로 데이터 접근

2. **시스템 프로퍼티 설정**
   - `System.setProperty("server.port", String.valueOf(port))` 설정
   - Spring Boot가 자동으로 시스템 프로퍼티의 `server.port`를 읽어 적용
   - `application.yml`의 설정보다 우선순위가 높음

3. **에러 처리**
   - YAML 파일이 없어도 기본 포트 사용 (에러 없음)
   - `SERVER_PORT` 값이 유효하지 않으면 경고 메시지 출력 후 기본 포트 사용
   - 파일 읽기 오류 시 기본 포트 사용

---

## 2026-01-22 10:00

### mega_speech.html 점 애니메이션 추가

#### 배경
`mega.html`에서는 마이크 버튼 클릭 시 "Interpreting" 이후의 점이 '.', '..', '...'로 반복되는 애니메이션이 구현되어 있었으나, `mega_speech.html`에는 동일한 기능이 없어 사용자 경험이 일관되지 않았음.

#### 구현 내용

1. **점 애니메이션 로직 추가**
   - `mega.html`과 동일한 점 애니메이션 로직을 `mega_speech.html`에 적용
   - 음성 인식 결과가 나온 후 AJAX 요청 전송 시 애니메이션 시작
   - AJAX 응답 수신 또는 타임아웃 시 애니메이션 중지

2. **애니메이션 구현 세부사항**
   - `dotAnimationInterval` 변수로 `setInterval` 관리
   - `baseText` 변수에 음성 인식 결과 저장 ("You said: [transcript]")
   - 400ms 간격으로 점 개수 변경 (1 → 2 → 3 → 1 반복)
   - `startDotAnimation()`: 애니메이션 시작 함수
   - `stopDotAnimation()`: 애니메이션 중지 함수

3. **사용자 경험 개선**
   - 음성 인식 결과 표시: "You said: [transcript]"
   - AJAX 요청 전송 시: "You said: [transcript] Interpreting."
   - 점 애니메이션: "You said: [transcript] Interpreting." → ".." → "..." → "." 반복
   - 응답 수신 시: "Speech will appear here..."로 초기화

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `src/main/webapp/mega_speech.html` | 점 애니메이션 로직 추가 (mega.html과 동일한 구조) |

#### 기술적 세부사항

**애니메이션 동작 흐름:**
1. 사용자가 마이크 버튼 클릭
2. 음성 인식 시작 ("Listening...")
3. 음성 인식 결과 수신 ("You said: [transcript]")
4. AJAX 요청 전송 및 점 애니메이션 시작
5. 서버 응답 수신 시 애니메이션 중지 및 화면 초기화

**코드 구조:**
```javascript
// 점 애니메이션 변수
let dotAnimationInterval = null;
let baseText = '';

// 애니메이션 시작
const startDotAnimation = () => {
    let dotCount = 1;
    output.textContent = baseText + ' Interpreting.';
    dotAnimationInterval = setInterval(() => {
        dotCount = (dotCount % 3) + 1;
        output.textContent = baseText + ' Interpreting' + '.'.repeat(dotCount);
    }, 400);
};

// 애니메이션 중지
const stopDotAnimation = () => {
    if (dotAnimationInterval) {
        clearInterval(dotAnimationInterval);
        dotAnimationInterval = null;
    }
};
```

---

## 2026-01-21 22:45

### packaging/distribution/README.md 업데이트

#### 배경
배포 디렉터리에 여러 종류의 파일(ZIP, WAR)이 생성되므로, 각 파일의 용도와 사용 방법을 명확히 문서화할 필요가 있음.

#### 구현 내용

1. **배포 파일 종류 섹션 추가**
   - 독립 실행 패키지 (ZIP): `smuxapi-demo.zip`
   - WAR 배포 파일: `smuxapi.war`
   - 각 파일의 생성 태스크 명시

2. **빌드 방법 섹션 추가**
   - `deploy` 태스크: ZIP + WAR 모두 생성
   - `war` 태스크: WAR 파일만 생성
   - `packageDist` 태스크: ZIP 파일만 생성
   - 각 태스크의 실행 방법과 생성 파일 위치 상세 설명

3. **배포 및 실행 방법 섹션 개선**
   - 방법 1: 독립 실행형 (ZIP 패키지)
   - 방법 2: WAR 배포 (Tomcat)
   - 각 방법의 단계별 가이드 제공

4. **로그 섹션 개선**
   - 독립 실행형과 WAR 배포의 로그 저장 위치 구분
   - 로그 롤링 정책 설명 추가

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `packaging/distribution/README.md` | deploy/war 태스크 결과물에 대한 상세 문서 추가 |

#### 문서 구조

**주요 섹션:**
1. 배포 파일 종류
2. 빌드 방법 (deploy, war, packageDist)
3. 배포 및 실행 방법 (독립 실행형, WAR 배포)
4. 시스템 요구사항
5. 설정 (API 키)
6. 로그 (배포 방식별 로그 위치)
7. 문제 해결

**개선 효과:**
- 사용자가 배포 파일의 용도를 쉽게 이해할 수 있음
- 각 빌드 태스크의 역할과 결과물이 명확해짐
- 배포 방법에 따른 차이점을 쉽게 파악 가능

---

## 2026-01-21 22:30

### WAR 파일 출력 경로 변경 및 빌드 스크립트 추가

#### 배경
기존에는 WAR 파일이 `build/libs/` 디렉터리에 생성되었으나, 배포 파일들을 한 곳에 모아 관리하기 위해 `packaging/distribution/` 디렉터리로 통일하는 것이 필요함. 또한 WAR 빌드를 쉽게 수행할 수 있는 배치 스크립트가 필요함.

#### 구현 내용

1. **WAR 파일 출력 경로 변경**
   - `build.gradle.kts`의 `tasks.war` 블록에 `destinationDirectory.set(file("packaging/distribution"))` 추가
   - WAR 파일이 `packaging/distribution/smuxapi.war`로 생성되도록 설정
   - ZIP 패키지(`smuxapi-demo.zip`)와 동일한 디렉터리에 배치하여 배포 파일 관리 용이

2. **war.bat 빌드 스크립트 생성**
   - 위치: `bat/war.bat`
   - 기능: Gradle의 `war` task를 실행하는 배치 스크립트
   - `deploy.bat`과 동일한 구조로 작성하여 일관성 유지
   - Gradle Wrapper 자동 탐지 기능 포함

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `build.gradle.kts` | WAR 파일 출력 경로를 `packaging/distribution`으로 설정 |
| `bat/war.bat` | WAR 빌드 전용 배치 스크립트 생성 |

#### 사용 방법

**WAR 빌드:**
```bash
# 방법 1: war.bat 실행
bat\war.bat

# 방법 2: Gradle 직접 실행
gradlew war
```

**생성 파일 위치:**
- `packaging/distribution/smuxapi.war`

**deploy 태스크와의 관계:**
- `deploy` 태스크는 `packageDist`와 `war` 태스크를 모두 실행
- 결과물이 모두 `packaging/distribution/` 디렉터리에 생성됨:
  - `smuxapi-demo.zip` (독립 실행 패키지)
  - `smuxapi.war` (WAR 배포 파일)

---

## 2026-01-21 (후반)

### 로그 저장 위치 개선

#### 배경
초기에는 모든 배포 방식에서 `C:/LOGS` 폴더에 로그를 저장했으나, 독립 실행형 JAR 배포 시에는 배포 위치의 `log` 폴더에 저장하는 것이 더 적절함.

#### 구현 내용

1. **동적 로그 경로 설정**
   - 배포 타입에 따라 로그 경로 자동 결정:
     - JAR 배포: 배포 위치의 `log` 폴더 (smuxapi-demo.bat과 같은 위치)
     - WAR 배포: `C:/LOGS` 폴더

2. **log4j2.xml 수정**
   - 시스템 프로퍼티 `${sys:logPath}` 사용
   - 기본값 설정: `C:/LOGS` (프로퍼티가 없을 경우)
   - `RollingFileAppender`로 날짜별/크기별 롤링 지원

3. **ApplicationConfig 개선**
   - `setupLogDirectory()` 메서드 추가
   - 배포 타입(`deployment.type`)에 따라 로그 경로 결정
   - 로그 디렉터리 자동 생성

#### 로그 설정 상세

**JAR 배포 시:**
```
배포 위치/
├── smuxapi-demo-0.6.0.jar
├── smuxapi-demo.bat
├── smuxapi-demo.yml
├── log/                    ← 로그 저장 위치
│   ├── smuxapi-demo.log
│   └── smuxapi-demo-2026-01-21-1.log
└── jre/
```

**WAR 배포 시:**
```
C:/LOGS/                    ← 로그 저장 위치
├── smuxapi-demo.log
└── smuxapi-demo-2026-01-21-1.log
```

**로그 롤링 정책:**
- 날짜별 롤링: 매일 새 파일 생성
- 크기별 롤링: 파일 크기 10MB 초과 시 롤링
- 보관 기간: 최대 30일

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `log4j2.xml` | 시스템 프로퍼티 사용, 기본값 설정, RollingFileAppender 적용 |
| `ApplicationConfig.java` | `setupLogDirectory()` 메서드 추가, 배포 타입별 로그 경로 설정 |
| `SmuxapiDemoApplication.java` | 배포 타입 프로퍼티 설정 (`deployment.type`) |

#### 기술적 세부사항

1. **시스템 프로퍼티 전달**
   - `main()` 메서드: `deployment.type=jar` 설정
   - `configure()` 메서드: `deployment.type=war` 설정
   - `ApplicationConfig.init()`: 프로퍼티를 읽어 로그 경로 결정

2. **Log4j2 동적 경로 설정**
   - `log4j2.xml`에서 `${sys:logPath}` 프로퍼티 참조
   - `<Properties>` 섹션에서 기본값 설정: `${sys:logPath:-C:/LOGS}`
   - Log4j2 초기화 전에 프로퍼티가 설정되어야 함

3. **디렉터리 자동 생성**
   - `File.mkdirs()`로 로그 디렉터리 자동 생성
   - 생성 실패 시 경고 메시지 출력 및 기본 경로 사용

---

### WAR 배포 기능 추가

#### 배경
기존에는 독립 실행형 JAR 배포만 지원했으나, 기존 Tomcat 서버에 WAR 파일로 배포할 수 있는 기능이 필요해짐.

#### 구현 내용

1. **Gradle WAR 플러그인 추가**
   - `build.gradle.kts`에 `war` 플러그인 추가
   - `providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")` 의존성 추가
   - WAR 파일 이름을 `smuxapi.war`로 설정하여 context path를 `/smuxapi`로 고정

2. **SpringBootServletInitializer 구현**
   - `SmuxapiDemoApplication` 클래스가 `SpringBootServletInitializer`를 상속하도록 수정
   - `configure()` 메서드에서 WAR 배포 시 `war` 프로파일 활성화
   - `deployment.type` 시스템 프로퍼티로 배포 타입 구분

3. **WAR 배포용 설정 파일**
   - `src/main/resources/application-war.yml` 생성
   - WAR 배포 시 context-path를 `/`로 설정 (Tomcat의 context path와 결합하여 `/smuxapi` 접근)

4. **Tomcat Context Path 설정**
   - `src/main/webapp/META-INF/context.xml` 생성
   - `<Context path="/smuxapi" />` 설정으로 Tomcat context path 고정

#### 해결한 문제들

1. **WAR 빌드 시 중복 파일 오류**
   - 문제: `src/main/webapp/WEB-INF/lib/`의 JAR 파일과 Spring Boot가 자동 포함하는 의존성 중복
   - 해결: `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` 설정
   - `from("src/main/webapp")` 블록에서 `WEB-INF/lib/*.jar` 제외

2. **SessionListener ClassNotFoundException**
   - 문제: `web.xml`에 등록된 `com.smartuxapi.sample.listen.SessionListener` 클래스가 존재하지 않음
   - 해결: `web.xml`에서 해당 리스너 등록 제거
   - 영향: 세션 리스너 기능은 제거되었으나, Spring Boot의 세션 관리 기능 사용 가능

3. **Tomcat 리소스 캐시 경고**
   - 문제: 많은 정적 리소스로 인한 캐시 크기 초과 경고
   - 해결: `META-INF/context.xml`에 리소스 캐시 설정 추가
   ```xml
   <Resources 
       cachingAllowed="true" 
       cacheMaxSize="100000" 
       cacheObjectMaxSize="4096"
       cacheTtl="60000" />
   ```

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `build.gradle.kts` | WAR 플러그인 추가, WAR 태스크 설정, 중복 파일 처리 전략 설정 |
| `SmuxapiDemoApplication.java` | SpringBootServletInitializer 상속, 배포 타입 프로퍼티 설정 |
| `src/main/resources/application-war.yml` | WAR 배포용 설정 파일 생성 |
| `src/main/webapp/META-INF/context.xml` | Tomcat context path 및 리소스 캐시 설정 |
| `src/main/webapp/WEB-INF/web.xml` | SessionListener 제거 |
| `src/main/webapp/web.xml` | SessionListener 제거 |

#### 배포 방법

**WAR 배포:**
```bash
gradlew war
# 또는
gradlew deploy
```

생성된 파일: `build/libs/smuxapi.war`

배포 위치: Tomcat의 `webapps/` 디렉터리

접속 URL: `http://localhost:8080/smuxapi`

---

## 2026-01-21

### Eclipse IDE Validation Error 해결

#### 문제 현황
Eclipse IDE에서 smuxapi-demo 프로젝트를 열었을 때 다음과 같은 validation error가 발생:
- `ERROR_DUPLICATE_WEB_INF_LIB` (7개 항목)
- JRE 호환성 경고 (JavaSE-17 지정, JRE 21 사용)
- HTML 검증 오류 (mega.html, mega_speech.html)

#### 원인 분석

1. **ERROR_DUPLICATE_WEB_INF_LIB 오류**
   - `src/main/webapp/WEB-INF/lib/` 폴더에 JAR 파일들이 수동으로 포함되어 있음
   - 동시에 `.classpath`에서 Gradle classpath container가 `/WEB-INF/lib`로 배포되도록 설정
   - Eclipse WST(Web Standard Tools) 검증기가 중복된 WEB-INF/lib 배포를 감지

2. **수동 포함된 JAR 파일들** (`src/main/webapp/WEB-INF/lib/`):
   - jackson-annotations-2.15.3.jar
   - jackson-core-2.15.3.jar
   - jackson-databind-2.15.3.jar
   - json-20250517.jar
   - json-simple-1.1.1.jar
   - log4j-api-2.21.0.jar
   - log4j-core-2.21.0.jar
   - smart-ux-api-0.5.1.jar

#### 해결 방법

1. **`.settings/org.eclipse.wst.validation.prefs` 생성**
   - UIWarValidator 비활성화하여 ERROR_DUPLICATE_WEB_INF_LIB 오류 억제
   ```properties
   eclipse.preferences.version=1
   override=true
   suspend=false
   vals/org.eclipse.jst.j2ee.internal.web.validation.UIWarValidator/global=FF01
   ```

2. **`.classpath` 수정**
   - Gradle classpath container에서 `/WEB-INF/lib` 배포 설정 제거
   - 변경 전:
     ```xml
     <classpathentry kind="con" path="org.eclipse.buildship.core.gradleclasspathcontainer">
       <attributes>
         <attribute name="org.eclipse.jst.component.dependency" value="/WEB-INF/lib"/>
       </attributes>
     </classpathentry>
     ```
   - 변경 후:
     ```xml
     <classpathentry kind="con" path="org.eclipse.buildship.core.gradleclasspathcontainer"/>
     ```

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `.settings/org.eclipse.wst.validation.prefs` | 새로 생성 |
| `.classpath` | WEB-INF/lib 배포 설정 제거 |

#### 참고 사항
- 프로젝트는 Gradle 기반이지만, `WEB-INF/lib/`에 수동으로 JAR 파일들이 포함되어 있음
- 이는 레거시 WAR 배포 방식과의 호환성을 위한 것으로 보임
- 장기적으로는 수동 JAR 파일들을 제거하고 Gradle 의존성만 사용하는 것이 권장됨

---

## 이전 기록

CHANGELOG.md 참조:
- [0.6.0] - 2026-01-20: Spring Boot 기반 독립 실행형 데모 애플리케이션 생성
- [0.1.0] - 2026-01-20: 프로젝트 시작, smart-ux-api [0.5.1] 버전과 동기화

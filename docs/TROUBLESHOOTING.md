# Troubleshooting Guide

Smart UX API 사용 중 발생할 수 있는 문제들과 해결 방법입니다.

---

## 📋 목차

- [설치 문제](#설치-문제)
- [API 연결 문제](#api-연결-문제)
- [런타임 오류](#런타임-오류)
- [JavaScript 클라이언트 문제](#javascript-클라이언트-문제)
- [성능 문제](#성능-문제)
- [보안 문제](#보안-문제)

---

## 설치 문제

### ❌ 빌드 실패: `java.lang.UnsupportedClassVersionError`

**증상:**
```
java.lang.UnsupportedClassVersionError: ... has been compiled by a more recent version of the Java Runtime
```

**원인:** Java 버전이 17 미만입니다.

**해결 방법:**
```bash
# Java 버전 확인
java -version

# Java 17 이상으로 업그레이드
# Windows: https://adoptium.net/
# Linux: sudo apt install openjdk-17-jdk
# Mac: brew install openjdk@17
```

---

### ❌ Gradle 빌드 오류: `Could not resolve dependencies`

**증상:**
```
Could not resolve com.fasterxml.jackson.core:jackson-databind:2.15.3
```

**원인:** 네트워크 문제 또는 Maven Central 접근 불가

**해결 방법:**
```bash
# 1. 프록시 설정 (필요한 경우)
export GRADLE_OPTS="-Dhttp.proxyHost=proxy.company.com -Dhttp.proxyPort=8080"

# 2. Gradle 캐시 정리
./gradlew clean --refresh-dependencies

# 3. 저장소 미러 사용 (build.gradle.kts)
repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}
```

---

### ❌ JAR 파일이 생성되지 않음

**증상:** `build/libs/` 디렉터리가 비어있음

**원인:** 빌드가 실패했거나 다른 경로에 생성됨

**해결 방법:**
```bash
# 1. 클린 빌드
./gradlew clean build

# 2. JAR 파일 찾기
find . -name "smart-ux-api*.jar"

# 3. 빌드 로그 확인
./gradlew build --info
```

---

## API 연결 문제

### ❌ `401 Unauthorized` - API Key 오류

**증상:**
```
APIException: 401 Unauthorized - Invalid API Key
```

**원인:** API Key가 잘못되었거나 만료됨

**해결 방법:**

#### OpenAI
```bash
# 1. API Key 확인
# https://platform.openai.com/api-keys

# 2. 환경 변수 설정
export OPENAI_API_KEY="sk-..."

# 3. 코드에서 확인
System.out.println("API Key: " + apiKey.substring(0, 7) + "...");
```

#### Gemini
```bash
# 1. API Key 확인
# https://aistudio.google.com/app/apikey

# 2. 환경 변수 설정
export GEMINI_API_KEY="AIza..."
```

#### Claude (Anthropic)
```bash
# 1. API Key 확인 — sk-ant-... 형식
# https://console.anthropic.com

# 2. 환경 변수 설정
export CLAUDE_API_KEY="sk-ant-..."
```

> 💡 Claude 호출이 `400 invalid_request_error` 와 함께 "credit balance is too low" 메시지를
> 반환하면 키는 유효하나 크레딧이 부족한 상태입니다. [Anthropic Console](https://console.anthropic.com)
> 의 Plans & Billing 에서 크레딧을 충전하세요. (인증 실패는 `401 authentication_error` 로 구분됩니다.)

---

### ❌ `429 Too Many Requests` - Rate Limit 초과

**증상:**
```
APIException: 429 Too Many Requests - Rate limit exceeded
```

**원인:** API 호출 한도 초과

**해결 방법:**

#### 1. Rate Limiting 구현
```java
public class RateLimiter {
    private long lastRequestTime = 0;
    private final long MIN_INTERVAL = 1000; // 1초
    
    public synchronized void waitIfNeeded() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTime;
        
        if (elapsed < MIN_INTERVAL) {
            try {
                Thread.sleep(MIN_INTERVAL - elapsed);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        lastRequestTime = System.currentTimeMillis();
    }
}
```

#### 2. 지수 백오프 재시도
```java
int maxRetries = 3;
for (int i = 0; i < maxRetries; i++) {
    try {
        return chatting.sendMessage(prompt, viewInfo);
    } catch (APIException e) {
        if (e.getStatusCode() == 429 && i < maxRetries - 1) {
            Thread.sleep((long) Math.pow(2, i) * 1000);
        } else {
            throw e;
        }
    }
}
```

---

### ❌ `500 Internal Server Error` - API 서버 오류

**증상:**
```
APIException: 500 Internal Server Error
```

**원인:** OpenAI/Gemini 서버 일시적 장애

**해결 방법:**
```java
// 재시도 로직 추가
int retries = 3;
while (retries > 0) {
    try {
        return chatting.sendMessage(prompt, viewInfo);
    } catch (APIException e) {
        if (e.getStatusCode() == 500 && retries > 1) {
            retries--;
            Thread.sleep(2000); // 2초 대기
        } else {
            throw e;
        }
    }
}
```

---

### ❌ `Connection Timeout` - 연결 시간 초과

**증상:**
```
NetworkException: Connection timed out
```

**원인:** 네트워크 지연 또는 방화벽 차단

**해결 방법:**

#### 1. 타임아웃 늘리기
```java
ResponsesChatRoom chatRoom = new ResponsesChatRoom.Builder()
    .apiKey("sk-...")
    .model("gpt-4")
    .timeout(60000)  // 60초로 증가
    .build();
```

#### 2. 네트워크 확인
```bash
# OpenAI API 연결 테스트
curl -I https://api.openai.com/v1/chat/completions

# Gemini API 연결 테스트
curl -I https://generativelanguage.googleapis.com/v1/models
```

#### 3. 프록시 설정
```java
System.setProperty("http.proxyHost", "proxy.company.com");
System.setProperty("http.proxyPort", "8080");
System.setProperty("https.proxyHost", "proxy.company.com");
System.setProperty("https.proxyPort", "8080");
```

---

## 런타임 오류

### ❌ `NullPointerException` in ChatRoom

**증상:**
```
java.lang.NullPointerException at ResponsesChatRoom.addSystemMessage()
```

**원인:** API Key가 null이거나 초기화 실패

**해결 방법:**
```java
// API Key 검증
String apiKey = System.getenv("OPENAI_API_KEY");
if (apiKey == null || apiKey.isEmpty()) {
    throw new IllegalArgumentException("API Key is required");
}

ResponsesChatRoom chatRoom = new ResponsesChatRoom(apiKey, "gpt-4");
```

---

### ❌ `JSONException` - 응답 파싱 실패

**증상:**
```
org.json.JSONException: JSONObject["actions"] not found
```

**원인:** AI 응답 형식이 예상과 다름

**해결 방법:**

#### 1. 응답 검증
```java
try {
    String response = chatting.sendMessage(prompt, viewInfo);
    JSONObject json = new JSONObject(response);
    
    if (!json.has("actions")) {
        System.err.println("Invalid response: " + response);
        throw new IllegalStateException("Response missing 'actions' field");
    }
    
    JSONArray actions = json.getJSONArray("actions");
    // 처리 계속...
    
} catch (JSONException e) {
    System.err.println("Failed to parse response: " + e.getMessage());
    // Fallback 로직
}
```

#### 2. UIF 문서 개선
```json
{
  "instructions": "응답은 반드시 다음 JSON 형식이어야 합니다: { \"actions\": [...] }",
  "examples": [
    {
      "input": "메뉴 클릭",
      "output": {
        "actions": [
          {"elementId": "menu_btn", "action": "click"}
        ]
      }
    }
  ]
}
```

---

### ❌ `OutOfMemoryError` - 메모리 부족

**증상:**
```
java.lang.OutOfMemoryError: Java heap space
```

**원인:** 대화 이력이 너무 많이 축적됨

**해결 방법:**

#### 1. JVM 메모리 증가
```bash
# Tomcat의 경우 setenv.sh/setenv.bat
export CATALINA_OPTS="-Xms512m -Xmx2048m"
```

#### 2. 대화 이력 제한
```java
public class ConversationManager {
    private static final int MAX_HISTORY = 10;
    
    public void limitHistory(ResponsesChatting chatting) {
        List<Message> history = chatting.getConversationHistory();
        
        if (history.size() > MAX_HISTORY) {
            // 오래된 메시지 제거
            chatting.clearHistory();
            // 최근 메시지만 재추가
            for (int i = history.size() - MAX_HISTORY; i < history.size(); i++) {
                chatting.addMessage(history.get(i));
            }
        }
    }
}
```

---

## JavaScript 클라이언트 문제

### ❌ JavaScript 스크립트 로드 오류

**증상:**
```
Uncaught ReferenceError: SmartUXCollector is not defined
또는
Cannot access 'doActions' before initialization
```

**원인:** 
- JavaScript 파일이 로드되지 않음
- ES6 모듈로 로드해야 하는 스크립트를 일반 스크립트로 로드함

**해결 방법:**

#### smart-ux-collector.js 로드
```html
<!-- 일반 스크립트로 로드 (자동 실행) -->
<script src="/js/smart-ux-collector.js"></script>
```

#### smart-ux-client.js 로드
```html
<!-- ES6 모듈로 로드 (필수) -->
<script type="module">
    import { doActions } from '/js/smart-ux-client.js';
    window.doActions = doActions;  // 전역에서 사용할 수 있도록 저장
</script>
```

**참고:**
- `smart-ux-collector.js`는 IIFE(즉시 실행 함수)로 구현되어 자동 실행됩니다
- `smart-ux-client.js`는 ES6 모듈이므로 `type="module"`로 로드해야 합니다
- 클래스가 아니므로 `new SmartUXCollector()` 같은 사용법은 지원하지 않습니다

---

### ❌ UI 요소를 찾을 수 없음

**증상:**
```
Error: Element with id 'menu_americano' not found
```

**원인:** Element ID가 실제 DOM과 다름

**해결 방법:**

#### 1. Element ID 확인
```javascript
// 브라우저 콘솔에서 확인
console.log(document.getElementById('menu_americano'));
```

#### 2. Collector 디버깅
```javascript
// smart-ux-collector.js가 자동으로 수집한 정보 확인
// window.uiSnapshot에 저장되어 있음
console.log('Collected elements:', window.uiSnapshot);

// 수집된 정보가 없으면 스크립트가 로드되지 않았거나 아직 수집 전일 수 있음
if (!window.uiSnapshot) {
    console.warn('UI 정보가 아직 수집되지 않았습니다. 2초 후 다시 확인하세요.');
}
```

#### 3. 동적 로딩 대기
```javascript
// Element가 로드될 때까지 대기
function waitForElement(selector, timeout = 5000) {
    return new Promise((resolve, reject) => {
        const startTime = Date.now();
        
        const check = () => {
            const element = document.querySelector(selector);
            if (element) {
                resolve(element);
            } else if (Date.now() - startTime > timeout) {
                reject(new Error('Element not found: ' + selector));
            } else {
                setTimeout(check, 100);
            }
        };
        
        check();
    });
}

// 사용
await waitForElement('#menu_americano');
```

---

### ❌ CORS 오류

**증상:**
```
Access to fetch at 'http://localhost:8080/api/chat' has been blocked by CORS policy
```

**원인:** 서버에서 CORS 헤더가 설정되지 않음

**해결 방법:**

#### Spring Boot
```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

#### Servlet Filter
```java
@WebFilter("/*")
public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }
}
```

---

## 성능 문제

### ❌ 응답 시간이 너무 느림

**증상:** AI 응답이 10초 이상 소요

**원인:** 복잡한 UIF 문서 또는 긴 대화 이력

**해결 방법:**

#### 1. UIF 문서 최적화
```json
// Before (너무 상세함)
{
  "elements": [
    {"id": "btn1", "type": "button", "text": "...", "x": 100, "y": 200, ...},
    {"id": "btn2", "type": "button", "text": "...", "x": 150, "y": 200, ...}
    // ... 100개 이상
  ]
}

// After (핵심만)
{
  "elements": [
    {"id": "btn1", "label": "아메리카노"},
    {"id": "btn2", "label": "라떼"}
  ]
}
```

#### 2. 대화 이력 압축
```java
// 오래된 메시지 요약
if (history.size() > 10) {
    String summary = "이전 대화 요약: 사용자가 아메리카노 2잔 주문함.";
    chatting.clearHistory();
    chatting.addSystemMessage(summary);
}
```

#### 3. 모델 변경
```java
// GPT-4 대신 GPT-3.5-turbo 사용 (더 빠름)
ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-3.5-turbo");
```

---

## 보안 문제

### ❌ API Key가 클라이언트에 노출됨

**증상:** 브라우저 개발자 도구에서 API Key 확인 가능

**원인:** JavaScript에 API Key를 하드코딩

**해결 방법:**
```java
// ✅ 올바른 방법: 서버에서만 API Key 관리
// JavaScript에서는 서버 API만 호출
fetch('/api/chat', { ... })  // API Key 포함 안 함

// ❌ 잘못된 방법: JavaScript에 API Key 포함
const apiKey = "sk-...";  // 절대 금지!
```

---

### ❌ SQL Injection 위험

**증상:** 사용자 입력이 그대로 쿼리에 사용됨

**해결 방법:**
```java
// ✅ Prepared Statement 사용
String sql = "INSERT INTO chat_logs (prompt, response) VALUES (?, ?)";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, prompt);
pstmt.setString(2, response);
pstmt.executeUpdate();

// ❌ 문자열 연결 (위험!)
String sql = "INSERT INTO chat_logs VALUES ('" + prompt + "', '" + response + "')";
```

---

## 추가 지원

### 로그 활성화

#### Log4j2 설정
```xml
<!-- log4j2.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger name="com.smartuxapi" level="DEBUG"/>
        <Root level="INFO">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

---

### 디버그 모드

```java
// 디버그 정보 출력
System.setProperty("smartux.debug", "true");

ResponsesChatRoom chatRoom = new ResponsesChatRoom("sk-...", "gpt-4");
chatRoom.setDebugMode(true);  // 요청/응답 로깅
```

---

### 문제가 해결되지 않을 때

1. **GitHub Issues 검색**: [기존 이슈 확인](https://github.com/kiunsea/smart-ux-api/issues)
2. **새 이슈 등록**: [버그 신고](https://github.com/kiunsea/smart-ux-api/issues/new?template=bug_report.md)
3. **Discussions**: [질문하기](https://github.com/kiunsea/smart-ux-api/discussions)
4. **Email**: kiunsea@gmail.com

---

## 추가 리소스

- [API Reference](API.md)
- [설치 가이드](INSTALL.md)
- [코드 예제](EXAMPLES.md)
- [보안 정책](../SECURITY.md)


# Installation Guide

Smart UX API를 프로젝트에 통합하는 상세 가이드입니다.

---

## 📋 목차

- [사전 요구 사항](#사전-요구-사항)
- [Java 웹 애플리케이션에 통합](#java-웹-애플리케이션에-통합)
- [Spring Boot 프로젝트에 통합](#spring-boot-프로젝트에-통합)
- [일반 Servlet 프로젝트에 통합](#일반-servlet-프로젝트에-통합)
- [API Key 설정](#api-key-설정)
- [설치 확인](#설치-확인)

---

## 사전 요구 사항

### 필수 사항
- Java 17 이상
- Gradle 8.x 또는 Maven 3.x
- 웹 애플리케이션 서버 (Tomcat 9+, Jetty 10+)

### AI API 키
다음 중 하나 이상 필요:
- **OpenAI API Key**: [발급 받기](https://platform.openai.com/api-keys)
- **Google Gemini API Key**: [발급 받기](https://aistudio.google.com/app/apikey)

---

## Java 웹 애플리케이션에 통합

### 📁 프로젝트 구조 이해

이 저장소는 다음과 같은 구조입니다:
- **저장소 이름**: `smart-ux-api` (GitHub)
- **메인 라이브러리**: `smart-ux-api/lib/` 디렉터리
- **샘플 프로젝트**: `smuxapi-war/` 디렉터리

> 💡 **참고**: 저장소 이름과 메인 프로젝트 폴더 이름이 동일합니다. 이는 의도된 구조이며, 메인 라이브러리는 `smart-ux-api/lib/` 경로에 있습니다.

### 1. Smart UX API 빌드

```bash
# 1. 저장소 클론
git clone https://github.com/kiunsea/smart-ux-api.git
cd smart-ux-api

# 2. 메인 라이브러리 디렉터리로 이동
cd smart-ux-api/lib

# 3. 빌드 실행 (Windows)
gradlew.bat build

# 3. 빌드 실행 (Linux/Mac)
./gradlew build
```

> ✅ **빌드 완료 확인**: `smart-ux-api/lib/build/libs/smart-ux-api-0.6.0.jar` 파일이 생성되었는지 확인하세요.

### 2. JAR 파일 추가

생성된 JAR 파일을 웹 애플리케이션의 `/WEB-INF/lib/` 디렉터리에 복사합니다.

```
your-web-app/
├── WEB-INF/
│   └── lib/
│       └── smart-ux-api-0.6.0.jar  ← 여기에 복사
```

### 3. JavaScript 라이브러리 추가

`smart-ux-api/lib/src/main/js/` 디렉터리의 JavaScript 파일들을 웹 애플리케이션의 웹 루트 디렉터리에 복사합니다.

```
your-web-app/
├── js/
│   ├── smart-ux-client.js     ← 복사
│   └── smart-ux-collector.js  ← 복사
```

### 4. HTML에 스크립트 포함

```html
<!DOCTYPE html>
<html>
<head>
    <title>My Web App</title>
</head>
<body>
    <!-- 웹 애플리케이션 콘텐츠 -->
    
    <!-- Smart UX API 스크립트 (body 끝에 추가) -->
    <script src="/js/smart-ux-client.js"></script>
    <script src="/js/smart-ux-collector.js"></script>
</body>
</html>
```

---

## Spring Boot 프로젝트에 통합

### 1. Gradle 의존성 추가

`build.gradle` 또는 `build.gradle.kts`:

```kotlin
dependencies {
    implementation(files("libs/smart-ux-api-0.6.0.jar"))
    
    // 필요한 의존성들 (이미 있다면 생략)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("org.json:json:20250517")
}
```

### 2. Controller 생성

```java
@RestController
@RequestMapping("/api/chat")
public class SmartUXController {
    
    private final ResponsesChatRoom chatRoom;
    
    @Autowired
    public SmartUXController(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.model}") String model) {
        
        this.chatRoom = new ResponsesChatRoom(apiKey, model);
        
        // UIF 문서 로드
        String uifDocument = loadUIFDocument();
        chatRoom.addSystemMessage(uifDocument);
    }
    
    @PostMapping
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        ResponsesChatting chatting = chatRoom.createChatting();
        String actionQueue = chatting.sendMessage(
            request.getPrompt(), 
            request.getViewInfo()
        );
        return ResponseEntity.ok(actionQueue);
    }
    
    private String loadUIFDocument() {
        try {
            ClassPathResource resource = new ClassPathResource("uif.json");
            return new String(resource.getInputStream().readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load UIF document", e);
        }
    }
}
```

### 3. application.properties 설정

```properties
# OpenAI API 설정
openai.api.key=${OPENAI_API_KEY}
openai.model=gpt-4

# Gemini API 설정 (선택)
gemini.api.key=${GEMINI_API_KEY}
gemini.model=gemini-pro
```

---

## 일반 Servlet 프로젝트에 통합

### 1. Servlet 생성

```java
@WebServlet("/api/chat")
public class SmartUXServlet extends HttpServlet {
    
    private ResponsesChatRoom chatRoom;
    
    @Override
    public void init() throws ServletException {
        // web.xml 또는 환경 변수에서 API Key 로드
        String apiKey = getServletContext().getInitParameter("openai.api.key");
        String model = getServletContext().getInitParameter("openai.model");
        
        chatRoom = new ResponsesChatRoom(apiKey, model);
        
        // UIF 문서 로드
        try {
            String uifDocument = loadUIFFromResource();
            chatRoom.addSystemMessage(uifDocument);
        } catch (IOException e) {
            throw new ServletException("Failed to load UIF document", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // 요청 파라미터 읽기
        BufferedReader reader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        // JSON 파싱
        JSONObject json = new JSONObject(sb.toString());
        String prompt = json.getString("prompt");
        String viewInfo = json.getString("viewInfo");
        
        // AI에 전송
        ResponsesChatting chatting = chatRoom.createChatting();
        String actionQueue = chatting.sendMessage(prompt, viewInfo);
        
        // 응답
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(actionQueue);
    }
    
    private String loadUIFFromResource() throws IOException {
        InputStream is = getServletContext()
            .getResourceAsStream("/WEB-INF/classes/uif.json");
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
}
```

### 2. web.xml 설정

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    
    <display-name>Smart UX Application</display-name>
    
    <!-- API Key 설정 (환경 변수 권장) -->
    <context-param>
        <param-name>openai.api.key</param-name>
        <param-value>${OPENAI_API_KEY}</param-value>
    </context-param>
    
    <context-param>
        <param-name>openai.model</param-name>
        <param-value>gpt-4</param-value>
    </context-param>
    
</web-app>
```

---

## API Key 설정

### 환경 변수 사용 (권장)

#### Windows
```batch
set OPENAI_API_KEY=sk-...
set GEMINI_API_KEY=...
```

#### Linux/Mac
```bash
export OPENAI_API_KEY=sk-...
export GEMINI_API_KEY=...
```

### 설정 파일 사용

`src/main/resources/apikey.json`:
```json
{
  "openai": {
    "apiKey": "sk-...",
    "model": "gpt-4",
    "assistantId": "asst_..." // Assistants API 사용 시
  },
  "gemini": {
    "apiKey": "...",
    "model": "gemini-pro"
  }
}
```

⚠️ **주의**: `apikey.json` 파일은 `.gitignore`에 반드시 추가하세요!

---

## 설치 확인

### 1. 빌드 테스트

```bash
cd smart-ux-api/lib
./gradlew test
```

모든 테스트가 통과하면 정상적으로 설치된 것입니다.

### 2. 웹 애플리케이션 실행

```bash
# Tomcat 실행 (예시)
catalina.bat start   # Windows
./catalina.sh start  # Linux/Mac
```

### 3. 브라우저에서 확인

```
http://localhost:8080/your-app/
```

개발자 콘솔에서 JavaScript 오류가 없는지 확인합니다.

### 4. API 테스트

```javascript
// 브라우저 콘솔에서 테스트
const collector = new SmartUXCollector();
const viewInfo = collector.collectUIInfo();
console.log(viewInfo);
```

UI 정보가 정상적으로 출력되면 성공입니다!

---

## 문제 해결

설치 중 문제가 발생하면 [TROUBLESHOOTING.md](TROUBLESHOOTING.md)를 참조하세요.

추가 질문은 [Discussions](https://github.com/kiunsea/smart-ux-api/discussions)에서 문의해 주세요.

---

## 다음 단계

- [코드 예제 보기](EXAMPLES.md)
- [API 문서 읽기](API.md)
- [샘플 프로젝트 실행](../smuxapi-war/)


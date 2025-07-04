# Smart UX API

![Java](https://img.shields.io/badge/language-Java-orange)
![JavaScript](https://img.shields.io/badge/language-JavaScript-yellow)
![Platform](https://img.shields.io/badge/platform-Web-blue)
![Status](https://img.shields.io/badge/status-Active-brightgreen)

## 🖱️ 설치 방법

Smart UX API는 기존 또는 신규 Java 기반 웹 애플리케이션에 쉽게 통합할 수 있습니다.

### 1. 📦 JAR 파일 추가

- `lib/build/libs/smart-ux-api.jar`를 웹 애플리케이션의 `/WEB-INF/lib/` 디렉토리에 추가합니다.

### 2. 📦 JS 라이브러리 포함

- `lib/src/main/js/*.js`를 웹 애플리케이션의 `[DOC ROOT]/suapi` 디렉토리에 추가합니다.
- 웹 페이지에 다음 스크립트를 추가합니다:

```html
<script src="/suapi/smart-ux-client.js"></script>
<script src="/suapi/smart-ux-collector.js"></script>

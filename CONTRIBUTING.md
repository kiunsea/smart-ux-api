# Contributing to smart-ux-api

감사합니다! smart-ux-api 프로젝트에 기여해주셔서 환영합니다. 아래 가이드라인을 따라주시면 프로젝트 관리와 협업에 큰 도움이 됩니다.

---

## 📦 프로젝트에 기여하는 방법

1. **이슈 등록**
   - 버그, 개선/기능 요청, 질문 등은 [Issues](https://github.com/kiunsea/smart-ux-api/issues) 탭에 등록해주세요.
   - 이슈 제목과 내용을 명확하게 작성해주세요.
   - 필요시 스크린샷/로그/재현 절차 등 추가 정보를 첨부해주세요.

2. **코드 기여 (Pull Request)**
   - Fork 후 브랜치를 생성하세요.  
     예시: `feature/로그인-기능-추가`
   - 필요한 변경 사항을 작업하고 테스트를 통과시켜 주세요.
   - [Pull Request](https://github.com/kiunsea/smart-ux-api/pulls) 생성 시, 관련 이슈 번호를 연결하고 변경 요약을 작성해주세요.
   - 리뷰 후 merge됩니다. 코드 리뷰 및 피드백을 환영합니다!

---

## 🛠️ 개발 환경 설정

### 사전 요구 사항
- **JDK 17 이상** ([다운로드](https://adoptium.net/))
- **Gradle 8.10 이상** (프로젝트에 포함된 Gradle Wrapper 사용 가능)
- **Git** ([다운로드](https://git-scm.com/))
- **IDE**: IntelliJ IDEA, Eclipse, 또는 VS Code

### 📁 프로젝트 구조 이해

이 저장소는 다음과 같은 구조입니다:

```
smart-ux-api/                    ← GitHub 저장소 (현재 위치)
│
├── smart-ux-api/                ← 메인 라이브러리 프로젝트
│   └── lib/                     ← 실제 라이브러리 소스 코드
│       ├── src/main/java/      ← Java 소스 코드
│       └── build.gradle.kts    ← 빌드 설정
│
└── smuxapi-war/                 ← 샘플 애플리케이션
    └── src/main/java/          ← 샘플 서블릿 코드
```

> 💡 **참고**: 저장소 이름과 메인 프로젝트 폴더 이름이 동일합니다. 메인 라이브러리는 `smart-ux-api/lib/` 경로에 있습니다.

### 개발 환경 설정 단계

1. **저장소 Fork 및 Clone**
```bash
# Fork 후 자신의 저장소 클론
git clone https://github.com/your-username/smart-ux-api.git
cd smart-ux-api
```

2. **Gradle 프로젝트 Import**
- IntelliJ IDEA: `File > Open` → `smart-ux-api/lib/build.gradle.kts` 선택
- Eclipse: `File > Import > Gradle > Existing Gradle Project`

3. **프로젝트 빌드**
```bash
# 메인 라이브러리 디렉터리로 이동
cd smart-ux-api/lib

# 빌드 실행
./gradlew build
```

4. **테스트 실행**
```bash
# smart-ux-api/lib 디렉터리에서 실행
cd smart-ux-api/lib
./gradlew test
```

5. **개발 브랜치 생성**
```bash
git checkout -b feature/my-new-feature
```

### IDE 설정

#### IntelliJ IDEA
- Code Style: Java (Google Style 또는 기본 설정)
- File Encoding: UTF-8
- Line Separator: LF (Unix)

#### Eclipse
- Window > Preferences > Java > Code Style > Formatter
- Workspace Encoding: UTF-8

---

## 📑 코드 스타일 및 규칙

### Java 코드 스타일
- **들여쓰기**: 공백 4칸 (탭 사용 금지)
- **줄 길이**: 최대 120자
- **변수명**: camelCase 사용 (예: `chatRoom`, `actionQueue`)
- **클래스명**: PascalCase 사용 (예: `ResponsesChatRoom`)
- **상수명**: UPPER_SNAKE_CASE 사용 (예: `MAX_RETRY_COUNT`)
- **패키지명**: 소문자 (예: `com.smartuxapi.ai`)

### JavaScript 코드 스타일
- **들여쓰기**: 공백 2칸
- **변수명**: camelCase 사용
- **세미콜론**: 항상 사용
- **문자열**: 작은따옴표 사용

### 주석 작성
- **JavaDoc**: 모든 public 클래스와 메서드에 작성
- **한글 주석**: 복잡한 로직에 대한 설명
- **TODO**: 미완성 기능에 대해 `// TODO: 설명` 형식으로 작성

### Commit Message Convention

커밋 메시지는 다음 형식을 따릅니다:

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type (필수)
- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **docs**: 문서 변경
- **style**: 코드 포맷팅 (기능 변경 없음)
- **refactor**: 코드 리팩토링
- **test**: 테스트 추가/수정
- **chore**: 빌드, 설정 변경

#### Scope (선택)
- 변경된 모듈이나 컴포넌트 (예: `chatroom`, `collector`, `docs`)

#### Subject (필수)
- 50자 이내의 간결한 설명
- 명령형 현재 시제 사용
- 마침표 없음

#### Body (선택)
- 변경 이유와 내용을 상세히 설명
- 72자마다 줄 바꿈

#### Footer (선택)
- 관련 이슈 번호: `Closes #123` 또는 `Fixes #456`
- Breaking Changes: `BREAKING CHANGE: 설명`

#### 예시
```
feat(chatroom): Add Gemini API support

- GeminiChatRoom 클래스 추가
- 기존 ChatRoom 인터페이스 호환성 유지
- Gemini API 호출 로직 구현

Closes #42
```

```
fix(collector): Fix null pointer exception in UI scan

UI 요소가 없을 때 발생하는 NPE 해결

Fixes #78
```

```
docs(readme): Update installation guide

설치 가이드에 Gradle 버전 정보 추가
```

---

## 🧪 테스트

- 새로운 기능/수정에는 반드시 테스트 코드를 추가해주세요.
- 모든 테스트가 통과해야 PR 승인이 가능합니다.

---

## 🗂️ 브랜치 및 PR 관리

### 브랜치 전략

- **main**: 안정적인 릴리스 브랜치 (직접 push 금지)
- **develop**: 개발 통합 브랜치 (선택사항)
- **feature/기능명**: 새로운 기능 개발
- **fix/버그명**: 버그 수정
- **docs/문서명**: 문서 작업
- **refactor/대상**: 리팩토링

#### 브랜치 명명 규칙
```
feature/add-claude-api-support
fix/null-pointer-in-collector
docs/update-api-reference
refactor/chatroom-interface
```

### Pull Request 절차

1. **Fork 저장소에서 작업**
```bash
git checkout -b feature/my-feature
# 작업 수행
git add .
git commit -m "feat(scope): description"
git push origin feature/my-feature
```

2. **PR 생성**
- GitHub에서 `New Pull Request` 클릭
- 템플릿에 따라 내용 작성
- 관련 이슈 번호 연결 (`Closes #123`)

3. **PR 내용 작성**
- **제목**: 커밋 메시지 규칙과 동일
- **설명**: 변경 사항, 이유, 테스트 결과
- **체크리스트**: 모든 항목 확인

4. **코드 리뷰**
- 최소 1명의 maintainer 승인 필요
- 리뷰 의견에 대해 응답 및 수정
- CI 테스트 통과 확인

5. **Merge**
- Squash merge 권장 (커밋 이력 정리)
- Merge 후 브랜치 삭제

### Code Review 가이드라인

#### 리뷰어
- ✅ 코드 품질 및 스타일 확인
- ✅ 버그나 잠재적 문제 지적
- ✅ 테스트 커버리지 확인
- ✅ 문서 업데이트 확인
- 🤝 건설적이고 친절한 피드백

#### 작성자
- 🙏 피드백을 긍정적으로 수용
- 💬 명확하지 않은 부분은 질문
- ✏️ 지적된 사항 신속히 수정
- 📝 변경 이유 명확히 설명

---

## 📝 문서화

- 기능 추가/변경 시 README, API 문서, 예제 코드 등 문서 업데이트를 권장합니다.
- 문서화가 잘 되어야 다른 개발자와 사용자에게 도움이 됩니다.

---

## 💬 커뮤니티 & 질문

- 궁금한 점은 [Discussions](https://github.com/kiunsea/smart-ux-api/discussions)에서 자유롭게 질문/토론해주세요.
- 프로젝트 개발 방향, 아이디어, 개선안 등도 환영합니다.

---

## 🛡️ 라이선스 & 규정

- 프로젝트 LICENSE를 준수해주시고, 외부 코드/라이브러리 사용 시 라이선스 호환성을 확인해주세요.

---

## 🙏 기여자 분들께

모든 기여에 감사드립니다!  
더 나은 smart-ux-api를 위해 함께해요.

---

문의/건의: [Issues](https://github.com/kiunsea/smart-ux-api/issues) 또는 [Discussions](https://github.com/kiunsea/smart-ux-api/discussions)에서 남겨주세요.
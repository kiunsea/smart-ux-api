# Session Handoff — 2026-05-03 (즉시 패치 4건 진행 직전)

**작성일**: 2026-05-03
**작성자**: Claude (Opus 4.7) + 사용자
**용도**: 다음 세션이 즉시 이어받을 수 있도록 작업 컨텍스트 인계

> **다음 세션을 시작할 때**: 이 문서를 먼저 읽고, §3 "이어갈 작업" 부터 진행.

---

## 1. 직전 상태 스냅샷

### 브랜치 / 태그

| 항목 | 값 |
|---|---|
| 현재 브랜치 | `main` (clean — 변경 없음) |
| 마지막 커밋 | `1b18ba7` Merge PR #25 (CI fixture fix) |
| 최신 lib 태그 | `v0.9.6` |
| 최신 demo 버전 | `0.10.1` (태그 없음 — demo 단독) |
| 마지막 CI run | `25292283081` ✅ 6/6 success |

### 테스트 / 빌드

```
gradlew :lib:test           → 597 / 576 pass / 21 skip / 0 fail
gradlew :smuxapi-demo:test  → 13 / 13 pass
```

skip 21 의 1건은 신규 — `FullScenarioTestCaseRealLlmIT` (`@EnabledIfEnvironmentVariable` 로 env var 없으면 skip).

### 주요 인프라 (이번 세션 도입)

- 루트 `CLAUDE.md` (166줄) — '푸시' 키워드 8단계 워크플로우 자동 트리거. 세션 시작 시 자동 로드됨.
- `.gitignore` 의 `**/scenarios/` negation — `lib/src/test/resources/scenarios/**` 는 예외 (fixture).

---

## 2. 직전 사용자 지시

> "즉시 패치 가능 작업 진행"

→ 다음 4개 사전 결함을 패치하라는 의미. 단일 feature branch 에 묶어 진행하기로 결정한 시점에서 세션 종료.

---

## 3. 이어갈 작업 (4개 사전 결함 패치)

### 3.1 우선순위 1 — `easy_kiosc_uif.json` 학습 자원 부재 (lib v0.9.7 patch)

**증상**: `ChatRoomService.getChatRoom()` 이 매 새 ChatRoom 생성 시
```java
JsonNode uifJson = ConfigLoader.loadConfigFromClasspath("easy_kiosc_uif.json");
chatRoom.getChatting().sendPrompt("다음의 내용을 학습해 -> " + uifJson);
```
파일이 main resources 에 없어서 `uifJson == null`. LLM 에 "다음의 내용을 학습해 -> null" 잘못된
학습 prompt 전송. v0.9.6 IT 검증의 의미있는 매칭 실패 원인 중 하나.

**파악된 사실**:
- 동일 내용을 가진 파일은 **lib/src/test/resources/test.easy_kiosc_uif.json** 으로 존재 (UIF 스키마)
- ChatRoomService 가 호출하는 곳 2 군데 (line 71, 104) — `ConfigLoader.loadConfigFromClasspath`
- ConfigLoader 는 lib 모듈 (`lib/src/main/java/com/smartuxapi/ai/ConfigLoader.java`)
- ConfigLoader.loadConfigFromClasspath(name) 은 main classpath 에서 `name` 으로 검색

**수정안**:
1. `test.easy_kiosc_uif.json` 내용을 **demo 의 main resources** 로 복사:
   `smuxapi-demo/src/main/resources/easy_kiosc_uif.json` (단 demo 가 가져야 자연스러움 —
   학습 prompt 자체가 demo 의 책임)
2. `ChatRoomService` 에 graceful fallback 추가 — uifJson==null 이면 학습 prompt 자체 skip
3. lib v0.9.7 patch — fallback 코드는 lib 가 아니라 demo 측이므로 사실 lib 변경 불필요.
   **demo 만 v0.10.2 patch 로 처리** 하는 게 맞음.

**최종 결론**: **demo v0.10.2** — fixture 추가 + ChatRoomService graceful fallback. lib 무변경.

### 3.2 우선순위 2 — `BrowserLauncher` 비대화형 환경 종료 유발 (demo v0.10.2 같이)

**증상**: bootRun / JAR 으로 데모 실행 시 `BrowserLauncher.openBrowser()` 가 rundll32 호출.
- Desktop API 미지원 환경 (Windows headless / SSH session) 에서 rundll32 spawn 후 JVM 조기 종료
- 이번 세션에서도 PowerShell `Start-Process` 로 JAR 실행 시 5번 시도해도 stdout 비어있고 즉시 종료

**수정안**:
- `BrowserLauncher.launchWhenReady()` 호출부 (`SmuxapiDemoApplication.main` line 55) 를
  - 환경변수 `SMUXAPI_NO_BROWSER=true` 시 skip
  - 또는 `--smuxapi.no-browser=true` 인자 시 skip
  - System property `java.awt.headless=true` 자동 감지

**최종 결론**: **demo v0.10.2** 에 같이 포함 (3.1 과 한 PR).

### 3.3 우선순위 3 — GitHub Actions Node 20 deprecation

**증상**: `actions/checkout@v4`, `actions/setup-java@v4`, `actions/upload-artifact@v4` 가
Node 20 사용. 2026-09 이후 deprecated, FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true 로 임시 우회.

**수정안**: `.github/workflows/*.yml` 의 actions 버전을 `@v5` 또는 최신으로 마이그레이션 (확인 필요).
또는 임시 처방: `env: FORCE_JAVASCRIPT_ACTIONS_TO_NODE24: 'true'` 추가 (긴급 처방).

**최종 결론**: **infra patch — 별도 commit, 모듈 버전 bump 없음**.

### 3.4 우선순위 4 — `bug_report.md` markdownlint 위반

**증상**: `.github/ISSUE_TEMPLATE/bug_report.md` 의 line 9, 12 에서 MD022/blanks-around-headings
(헤딩 주변에 빈 줄 없음). CI lint job 통과는 하지만 매 push 시 경고 노이즈.

**수정안**: 헤딩 (`## 🐛 버그 설명`, `## 📋 재현 방법`) 위아래로 빈 줄 추가.

**최종 결론**: **docs patch — infra 와 같은 commit 또는 분리. 모듈 버전 bump 없음**.

---

## 4. 권장 작업 순서

### Step 1 — feature branch 생성

```bash
git checkout main && git pull origin main
git checkout -b feature/quality-pass-v0.10.2
```

### Step 2 — 4개 변경 적용 (commit 4 분리 권장)

| 순서 | 파일 / 변경 | commit 메시지 |
|---|---|---|
| (A) | `smuxapi-demo/src/main/resources/easy_kiosc_uif.json` 추가 (test 리소스 복사) + `ChatRoomService.getChatRoom()` graceful fallback (uifJson==null skip) + demo `build.gradle.kts` version `0.10.1` → `0.10.2` + `smuxapi-demo/CHANGELOG.md` `[0.10.2]` 엔트리 | `fix(demo v0.10.2): easy_kiosc_uif.json 추가 + null 학습 prompt 방어` |
| (B) | `BrowserLauncher` / `SmuxapiDemoApplication` 에 SMUXAPI_NO_BROWSER 환경변수 + `--smuxapi.no-browser` 인자 + `java.awt.headless` 자동 감지. CHANGELOG 같이 갱신 | `feat(demo): BrowserLauncher headless / no-browser 옵션` |
| (C) | `.github/workflows/*.yml` 의 actions 버전 마이그레이션 (또는 FORCE_JAVASCRIPT_ACTIONS_TO_NODE24 env 추가) | `chore(ci): GitHub Actions Node 20 deprecation 마이그레이션` |
| (D) | `.github/ISSUE_TEMPLATE/bug_report.md` MD022 위반 수정 (헤딩 주변 빈 줄) | `docs(infra): bug_report.md markdownlint MD022 해소` |

(A) + (B) 는 동일 demo v0.10.2 안에 묶이므로 사실상 단일 commit 으로 합쳐도 무방. 분리하면 회귀 추적 쉬움.

### Step 3 — 루트 CHANGELOG 갱신

`smuxapi-demo 단독 릴리스` 표 상단에 `0.10.2` 행 추가:
```markdown
| **0.10.2** | YYYY-MM-DD | easy_kiosc_uif.json 추가 + BrowserLauncher headless | [demo §0.10.2](smuxapi-demo/CHANGELOG.md#0102---YYYY-MM-DD) |
```

### Step 4 — 푸시 워크플로우 자동 실행

사용자가 "푸시" 라고 입력하면 `CLAUDE.md` 의 8단계 워크플로우 자동 적용:
- push → PR 생성 (제목/본문 자동 작성)
- merge 후 CI 모니터링 (`gh run watch`)
- 실패 시 단계 7 (오류 해소 + 재시도, 최대 3회)
- 성공 시 종합 보고

### Step 5 — 검증

CI 통과 후:
```bash
gradlew :smuxapi-demo:bootRun --args="--smuxapi.scenario.collect-enabled=true --smuxapi.no-browser=true"
# /collect → /action → /save 호출
# 새 시나리오 JSON 의 Turn 1 userPrompt 가 "다음의 내용을 학습해 -> {정상 JSON}" 인지 확인
```

(선택) Phase 3 IT 재실행하여 의미있는 매칭 (PASS=2 이상) 확인.

---

## 5. 참고 — 이번 세션에서 확인된 사실

### Spring Boot 데모 디버깅 노하우

비대화형 환경에서 Spring Boot 데모가 즉시 종료하는 경우 → BrowserLauncher 의 `rundll32` 호출 결과
JVM 종료. detach 가 안 되어 부모 종료 시 같이 죽음. PowerShell `Start-Process` 로 띄워도 동일.
**해결책은 BrowserLauncher 자체의 환경 감지** (즉 작업 3.2).

### scenarios fixture 처리

- `**/scenarios/` 는 demo 출력 디렉터리만 막아야 함
- `lib/src/test/resources/scenarios/` 의 fixture 는 의도적 commit 대상
- 현재 `.gitignore:121-124` 에 negation 처리 완료

### `gh run watch` 동작

- 개별 X 표시는 markdownlint 가 발견한 violation. job 자체가 fail 인지는 별도 확인 필요
- `gh run view <id> --json jobs --jq '.jobs[] | {name, conclusion}'` 로 정확한 job 결과 확인

### 푸시 워크플로우 트리거

- "푸시" / "푸시해줘" / "push" 등 단독 메시지 → 자동 트리거
- 다른 컨텍스트 섞이면 (예: "이거 고치고 푸시해") 의도 확인 후 진행
- `CLAUDE.md` 8단계 명세 참조

---

## 6. 핵심 파일 인덱스

| 목적 | 경로 |
|---|---|
| 푸시 워크플로우 명세 | `CLAUDE.md` (루트, 자동 로드) |
| 본 핸드오프 문서 | `lib/doc/working/session-handoff-20260503.md` (현재 파일) |
| 이전 핸드오프 | `lib/doc/working/session-handoff-20260422-v2.md`, `session-handoff-20260422.md` |
| Full Scenario Test Plan | `lib/doc/working/full-scenario-test-plan.md` |
| 루트 CHANGELOG (인덱스) | `CHANGELOG.md` |
| lib CHANGELOG | `lib/CHANGELOG.md` |
| demo CHANGELOG | `smuxapi-demo/CHANGELOG.md` |
| ChatRoomService (이번 작업 핵심) | `smuxapi-demo/src/main/java/com/smartuxapi/demo/service/ChatRoomService.java` |
| BrowserLauncher (이번 작업 핵심) | `smuxapi-demo/src/main/java/com/smartuxapi/demo/BrowserLauncher.java` |
| UIF fixture (test 측 원본) | `lib/src/test/resources/test.easy_kiosc_uif.json` |

---

## 7. 종료 시점 체크리스트

- [x] 모든 단위 테스트 통과 (597 / 576 pass / 21 skip / 0 fail)
- [x] CI 마지막 run success (`25292283081`)
- [x] main 동기화 (clean working tree)
- [x] CLAUDE.md 푸시 워크플로우 도입 + 자가 검증 완료
- [x] 핸드오프 문서 작성 (이 문서)
- [ ] 4개 사전 결함 패치 (다음 세션)

---

**다음 세션 시작 권장 명령**:
```
"다음 세션 핸드오프 lib/doc/working/session-handoff-20260503.md 읽고 이어가줘"
```
또는 단순히:
```
"즉시 패치 진행" (Claude 가 자동으로 본 핸드오프 발견 + Step 1~5 수행)
```

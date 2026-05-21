# Session Handoff — 2026-05-08

**작성일**: 2026-05-08
**작성자**: Claude (Opus 4.7) + 사용자
**용도**: 다음 세션이 즉시 이어받을 수 있도록 작업 컨텍스트 인계

> **다음 세션을 시작할 때**: 이 문서를 먼저 읽고, §3 "이어갈 작업" 부터 진행.

---

## 1. 직전 상태 스냅샷

### 브랜치 / 태그

| 항목 | 값 |
|---|---|
| 마지막 main 커밋 | `3e7354f` Merge PR #28 (demo 단독 릴리스 금지 정책) |
| 최신 lib 태그 | `v0.9.6` |
| 최신 demo 버전 | `0.10.2` (태그 없음 — 다음 lib 릴리스에 동반 출시 예정) |
| 마지막 main CI run | `25493205246` ✅ 6/6 success |

### 테스트 / 빌드 (직전 CI 기준)

```
:lib:test           → 597 / 576 pass / 21 skip / 0 fail
:smuxapi-demo:test  → 19 / 19 pass (BrowserLauncherTest 6 + collector 13)
```

skip 21 중 1건은 `FullScenarioTestCaseRealLlmIT` (`@EnabledIfEnvironmentVariable` — env var 없으면 skip).

---

## 2. 직전 세션 (2026-05-07) 완료 사항

이전 핸드오프 (`session-handoff-20260503.md`) 의 즉시 패치 4건 + 정책 변경 완료.

### PR #27 — Quality Pass v0.10.2 (merge `3379409`)

- **(A) `fix(demo v0.10.2)`** — `easy_kiosc_uif.json` 을 demo main classpath 에 추가 + `ChatRoomService.sendUifLearningPrompt()` 헬퍼 (classpath 미발견 시 학습 prompt 생략). 기존엔 `"다음의 내용을 학습해 -> null"` 잘못된 prompt 가 LLM 으로 전송되던 결함.
- **(B) `feat(demo)`** — `BrowserLauncher.launchWhenReady(args)` + `shouldSkipBrowser(args)` (env / arg / headless / Desktop 4분기). `BrowserLauncherTest` 6건.
- **(C) `chore(ci)`** — 워크플로우 3건에 `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true` env.
- **(D) `docs(infra)`** — `bug_report.md` MD022 해소.
- **`fix(ci)`** — BrowserLauncherTest 가 CI Linux headless 에서 `Desktop` 정적 초기화 실패 → `shouldSkipBrowser` 의 Desktop 호출을 `try/catch (Throwable)` 로 방어 + 테스트의 `headless` 강제 설정 제거. (CI 재시도 1/3 으로 해소)

### PR #28 — 정책 변경 (merge `3e7354f`)

- **`docs(infra)`** — smuxapi-demo 단독 릴리스 금지 정책 도입. CLAUDE.md / 루트 CHANGELOG / `devlog-20260507-policy.md` 명문화.

---

## 3. 이어갈 작업 (다음 세션 권장 — `devlog-20260507.md` §5)

> 우선순위는 사용자 지시에 따라 조정. 아래는 직전 세션이 식별한 후속 항목.

### 3.1 우선순위 1 — Phase 3 IT 재검증

**목적**: PR #27 의 (A) 패치 (학습 prompt null 결함 해소) 가 실제 매칭 정확도를 개선했는지 측정.

**방법**:
```
gradlew :smuxapi-demo:bootRun --args="--smuxapi.scenario.collect-enabled=true --smuxapi.no-browser=true"
# /collect → /action → /save 호출
# 새 시나리오 JSON 의 Turn 1 userPrompt 가 "다음의 내용을 학습해 -> {정상 JSON}" 인지 확인
```
이어서 `FullScenarioTestCaseRealLlmIT` 를 env var 부여하여 실행 → PASS=0 → PASS≥2 목표 확인.

**참고**: `--smuxapi.no-browser=true` 옵션은 PR #27 (B) 에서 추가됨 — 비대화형 환경 데모 기동 가능.

### 3.2 우선순위 2 — GitHub Actions v5 정식 마이그레이션

**현재**: `FORCE_JAVASCRIPT_ACTIONS_TO_NODE24=true` env 는 임시 처방 (PR #27 (C)).

**작업**: `actions/checkout`, `actions/setup-java`, `actions/upload-artifact` 등의 v5 정식 출시/검증 후 버전 핀 갱신 + 임시 env 제거. `.github/workflows/{ci,deploy-docs,release}.yml` 3개 파일.

**주의**: 외부 action 의 v5 호환성을 먼저 확인할 것. `dorny/test-reporter`, `codecov/codecov-action`, `peaceiris/actions-gh-pages` 등도 점검 대상.

### 3.3 우선순위 3 — demo README 옵션 문서화

`--smuxapi.no-browser` 옵션 (env `SMUXAPI_NO_BROWSER` / 인자 / `java.awt.headless`) 의 사용 예 (CI / SSH / 데몬 실행) 를 demo README 또는 `smuxapi-demo/packaging/distribution/README.md` 에 추가.

---

## 4. 중요 — 운영 정책 (2026-05-07 도입)

> **smuxapi-demo 단독 릴리스 금지.** demo 변경은 항상 lib 릴리스 사이클에 묶여 함께 출시된다. lib 변경 시 demo 영향을 반드시 검토하고, 영향 있으면 같은 PR/같은 릴리스에 묶는다. (예외: CI 패치 등 정합성 회복용 demo-only patch 는 `version` bump 없이 commit 만 허용)

- demo v0.10.2 의 GitHub Release 객체는 생성하지 않음. 다음 lib 릴리스 (`v0.9.7` 또는 그 이후) 시점에 demo v0.10.2 변경분이 lib 변경분과 함께 출시 노트로 묶여 발표될 예정.
- 정책 출처: `CLAUDE.md` § 단계 2 / § 모듈/버전 정책, `devlog-20260507-policy.md`.

---

## 5. 핵심 파일 인덱스

| 목적 | 경로 |
|---|---|
| 푸시 워크플로우 명세 + 모듈/버전 정책 | `CLAUDE.md` (루트, 자동 로드) |
| 본 핸드오프 문서 | `lib/doc/working/session-handoff-20260508.md` (현재 파일) |
| 직전 세션 작업 기록 | `lib/doc/working/devlog-20260507.md`, `devlog-20260507-policy.md` |
| 이전 핸드오프 | `lib/doc/working/session-handoff-20260503.md` |
| Full Scenario Test Plan | `lib/doc/working/full-scenario-test-plan.md` |
| 루트 CHANGELOG (릴리스 인덱스) | `CHANGELOG.md` |
| lib / demo CHANGELOG | `lib/CHANGELOG.md`, `smuxapi-demo/CHANGELOG.md` |
| ChatRoomService (PR #27 핵심) | `smuxapi-demo/src/main/java/com/smartuxapi/demo/service/ChatRoomService.java` |
| BrowserLauncher (PR #27 핵심) | `smuxapi-demo/src/main/java/com/smartuxapi/demo/BrowserLauncher.java` |
| Phase 3 IT | `lib` 의 `FullScenarioTestCaseRealLlmIT` |

---

## 6. 종료 시점 체크리스트

- [x] 핸드오프 §3 즉시 패치 4건 완료 (PR #27)
- [x] demo 단독 릴리스 금지 정책 도입 (PR #28)
- [x] main CI 마지막 run success (`25493205246`)
- [x] 정책 메모리 저장 (`policy_no_demo_only_release.md`)
- [x] 핸드오프 문서 작성 (이 문서)
- [ ] §3 후속 작업 3건 (다음 세션)

---

**다음 세션 시작 권장 명령**:
```
"세션 핸드오프 lib/doc/working/session-handoff-20260508.md 읽고 이어가줘"
```

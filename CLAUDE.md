# Smart UX API — Claude Code 운영 가이드

이 문서는 Claude Code 가 본 리포지토리에서 자동으로 따라야 할 워크플로우를 정의합니다.
세션 시작 시 자동 로드되므로 사용자가 별도로 첨부하지 않아도 됩니다.

---

## 🚀 푸시 워크플로우 (`푸시` 키워드 트리거)

**트리거**: 사용자가 단독으로 `푸시`, `푸시해줘`, `push`, `푸시 해`, 등 명백히 푸시 의도만 포함된 메시지를
보냈을 때 자동 실행합니다. 다른 작업 컨텍스트가 섞여 있으면 (예: "이거 고치고 푸시해") 의도 확인 후 진행.

### 단계 1 — Pre-flight 점검

1. `git status --short` 로 변경 파일 확인.
2. 변경이 없으면: "푸시할 변경 없음" 보고 후 종료. 사용자가 새 작업을 지시할 때까지 대기.
3. 변경이 있으면: `git diff --stat` 로 범위 요약 + 어떤 모듈 (lib / smuxapi-demo / 루트) 이 영향받는지 식별.
4. 현재 브랜치 확인: `git branch --show-current`.
   - `main` 직접 작업은 가능한 회피 — 사용자가 명시적으로 main 에서 작업하라고 했거나, 단순 docs 변경이 아니면 feature 브랜치로 전환 권유.

### 단계 2 — 버전업 결정

영향 모듈에 따라 버전 bump 단계 분류:

| 변경 종류 | 권장 bump | 예 |
|---------|---------|-----|
| Major | `X.0.0` | 공개 API 시그니처 제거/변경 (하위 비호환) |
| Minor | `0.Y.0` | 새 공개 메서드/모듈/기능 추가 (호환 유지) |
| Patch | `0.0.Z` | 버그 수정, 문서 갱신, 테스트 추가, 내부 리팩터링 |

판단 규칙:
- **lib/** 코드 변경 → `lib/build.gradle.kts` 의 `version` 을 bump. **이때 smuxapi-demo 영향을 반드시 함께 검토** (API 시그니처/동작 변경이 demo 코드/리소스/설정에 영향을 주는지). 영향 있으면 같은 PR 안에서 demo 도 함께 수정 + demo `version` 도 함께 bump.
- **smuxapi-demo/** 코드만 변경되고 lib 변경이 없는 경우 → **단독 릴리스 금지**. demo 변경은 항상 lib 릴리스 사이클에 묶여야 한다 (다음 lib 변경/릴리스 시점까지 대기, 또는 같은 PR 에 lib 변경을 함께 포함). 예외적으로 정합성 회복 (CI 패치 등) 만을 위한 demo 만의 변경은 `version` bump 없이 patch commit 으로만 처리.
- 양쪽 모두 변경 → 양쪽 모두 각자의 tier 로 bump (이게 정상 패턴).
- 루트 문서 (README, docs/, CHANGELOG 자체 등) 만 변경 → 버전 bump 생략, 단 lib/demo CHANGELOG 어디에도 기록은 남기지 않고 root CHANGELOG 가 직접 변경 사실을 갖되, 새 태그 / 새 모듈 버전은 만들지 않음.
- 의도가 모호하면 변경 분석 후 한 줄로 사용자에게 확인: 예) "lib 변경이라 patch bump 0.9.6 → 0.9.7 로 진행 — OK?". 단, 명백한 patch (오타/문서) 는 자동 결정.

bump 후 새 버전 문자열 (예: `0.9.7`) 을 기억하여 다음 단계에서 사용.

### 단계 3 — CHANGELOG 작성

다음 위치에 새 엔트리 추가 (해당하는 모듈만):

#### `lib/CHANGELOG.md` — lib 변경 시

```markdown
---
## [{newVersion}] - YYYY-MM-DD

### Added
- ...

### Changed
- ...

### Fixed
- ...

### Notes
- 하위 호환 여부 / 마이그레이션 가이드 (있을 때)
```

`Added/Changed/Fixed/Removed/Tests/Verified/Notes` 중 해당 섹션만 사용. 빈 섹션 헤더는 두지 말 것.

#### `smuxapi-demo/CHANGELOG.md` — demo 변경 시

동일 형식.

#### `CHANGELOG.md` (루트 인덱스) — lib 새 태그가 부여될 때

`## 활성 릴리스 (태그 보유)` 표 상단에 새 행 추가:

```markdown
| **v{newVersion}** | YYYY-MM-DD | 한 줄 헤드라인 | lib (또는 lib · demo) | [lib §{verNoDots}](lib/CHANGELOG.md#{verNoDots}---YYYY-MM-DD) |
```

demo 단독 릴리스 (lib 태그 없는 demo 만 bump) 인 경우 `## smuxapi-demo 단독 릴리스` 표에 추가.

### 단계 4 — 커밋

커밋 메시지 형식:

```
{type}(scope v{newVersion}): {간결한 한 줄 요약}

{본문 — 무엇이 변경되었고 왜 변경했는지. lib CHANGELOG 의 Added/Changed/Fixed
요약을 그대로 사용 가능. bullet 4~10개 권장.}

Co-Authored-By: Claude Opus 4.7 (1M context) <noreply@anthropic.com>
```

`type` 은 conventional commits:
- `feat` — 새 기능 (minor/major bump)
- `fix` — 버그 수정 (patch)
- `docs` — 문서만 변경
- `test` — 테스트만 추가
- `chore` — 빌드/설정/유틸 (배포, gradle 등)
- `refactor` — 동작 변경 없는 구조 변경

`scope` 는 모듈명: `lib`, `demo`, `lib·demo`, `docs`, `ci`, `infra` 중 하나.

명령:
1. `git add -A` (.gitignore 가 적절히 막아주므로 일반적으로 안전 — 그러나 `git status` 로 한번 더 확인)
2. `git commit -m "..."` (HEREDOC 사용해 다중 줄 메시지 작성)

### 단계 5 — 푸시

1. 현재 브랜치 확인.
2. 추적 원격이 있으면 `git push`, 없으면 `git push -u origin <branch>`.
3. 푸시 결과의 SHA 와 브랜치 명을 기억 (다음 단계 모니터링용).

### 단계 6 — CI 실행 결과 확인

1. `gh run list --branch <branch> --limit 1 --json databaseId,status,conclusion,createdAt` 로
   방금 푸시가 트리거한 워크플로우 run 을 찾는다. (timestamp 기준으로 새 run 인지 확인)
2. run 이 아직 등록 안 됐으면 짧은 polling (Monitor with `until <gh check>`).
3. run id 확보 후 `gh run watch <runId> --exit-status` 를 사용하거나
   `gh run view <runId> --json status,conclusion` 으로 polling.
4. 종료 상태:
   - `conclusion=success` → 푸시 완료 보고. 끝.
   - `conclusion=failure` / `cancelled` / `timed_out` → 단계 7 로.

### 단계 7 — 오류 해소 + 재시도 (최대 3회)

1. `gh run view <runId> --log-failed` 로 실패한 job 의 로그 추출.
2. 실패 원인 분류:
   - **컴파일 에러 / 테스트 실패** → 코드 수정 후 로컬 빌드/테스트 재현 → 재커밋 → 재푸시.
   - **린트 / 포맷** → 자동 수정 가능하면 수정 후 재커밋.
   - **네트워크 / 인프라 transient** → 사용자에게 알리고 재실행 권유 (`gh run rerun <runId>`).
   - **시크릿/권한 문제** → 사용자에게 보고 후 정지 (자동 해결 불가).
3. 수정 commit 메시지: `fix(ci): {오류 요약 한 줄}` (별도 버전 bump 안 함 — 이미 같은 푸시 흐름의 일부).
4. 재푸시 후 다시 단계 6 으로.
5. 3회 시도해도 통과 못하면 사용자에게 종합 보고 후 정지.

### 단계 8 — 보고

성공 시: `git log --oneline -1`, `gh run view --json conclusion,databaseId,url`, 변경 모듈/버전/CHANGELOG 위치를 요약.

---

## 📦 모듈 / 버전 정책 요약

- **lib 태그** = 리포지토리 태그 (`v0.x.y`). 릴리스 단위. 이 태그가 GitHub Release 출시 트리거.
- **smuxapi-demo 단독 릴리스는 운영하지 않음.** demo 변경은 다음 lib 릴리스에 함께 포함되어 출시된다. 즉 demo 만을 위한 GitHub Release / 태그 / 별도 출시 단계는 없음.
- **lib 변경 시 demo 영향 검토 의무.** lib 의 공개 API / 동작 / 리소스 / 설정 변경이 demo 에 영향을 주는지 반드시 확인하고, 영향 있으면 같은 PR 안에 demo 수정도 묶어 함께 릴리스.
- **smuxapi-demo 버전** (`0.10.x` 등) = 모듈 내부에서만 추적되는 식별자. 별도 태그 없음. lib 태그와 함께 동반 출시되는 시점에 의미 부여.
- 루트 `CHANGELOG.md` = lib 태그 (= 릴리스) 인덱스. lib 태그 행에 동반된 demo 변경을 `lib · demo` 형식으로 함께 기록.
- 모듈 CHANGELOG = 상세 항목 (Added/Changed/Fixed).
- **(과거 history)** 본 정책 도입 이전에 demo 단독 릴리스가 운영된 시기가 있어 루트 CHANGELOG 에 그 history 가 보존되어 있음 (이전 정책 사용 흔적). 신규 작업은 본 정책을 따른다.

## 🧪 테스트 / 빌드 명령 (참조)

```
gradlew :lib:test                                        # 단위 테스트
gradlew :lib:build                                       # JAR 빌드
gradlew :smuxapi-demo:bootJar                            # demo Spring Boot JAR
gradlew :smuxapi-demo:bootRun                            # 데모 기동 (port 9090)
gradlew :lib:deploy                                      # doribox/libs 로 JAR 복사
gradlew :smuxapi-demo:test                               # demo 단위 테스트
```

`smuxapi-demo\bat\start.bat` — 더블클릭으로 데모 실행 (gradlew bootRun 호출).

## 🛡 안전 수칙

- `git push --force` / `--no-verify` / `git reset --hard` 등 파괴적 명령은 사용자가 명시적으로 요청한 경우만.
- `main` 브랜치 force push 는 절대 금지.
- 본 워크플로우 도중 발생한 모든 commit 은 Co-Authored-By trailer 포함.
- API 키 / 시크릿이 변경 diff 에 포함되었다면 즉시 정지 후 사용자에게 경고.

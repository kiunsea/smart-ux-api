# Vision API Guide (v0.7.0+)

smart-ux-api v0.7.0 은 이미지에서 텍스트를 추출하여 LLM 에 전달하는 Vision 모듈을 제공합니다.
alt 속성이 없는 이미지 버튼, 텍스트가 포함된 스크린샷 등 AI 가 DOM 만으로 판단하기 어려운
UI 요소를 보조 컨텍스트로 덧대는 용도입니다.

---

## 1. 핵심 API

| API | 설명 |
|-----|------|
| `VisionServiceFactory.createOpenAI(apiKey)` | OpenAI GPT-4 Vision 기본 모델로 서비스 생성 |
| `VisionServiceFactory.createOpenAI(apiKey, model)` | 모델 지정 (예: "gpt-4o") |
| `VisionServiceFactory.createOpenAIFromEnv()` | 환경변수/시스템프로퍼티에서 키 로드 |
| `VisionService.extractText(url)` | 이미지 → 텍스트 문자열 |
| `VisionService.extractTextFromBase64(b64)` | Base64 → 텍스트 |
| `VisionService.scanImage(url)` | 이미지 → `ImageScanInfo` (메타데이터 포함) |
| `VisionService.isEnabled()` | API 키 설정 여부 |
| `ActionQueueHandler.addImageScanInfo(info)` | AI 프롬프트에 스캔 결과 주입 (imageUrl 기준 dedupe) |
| `ActionQueueHandler.addImageScanInfoList(list)` | 여러 결과를 한 번에 주입 |
| `ActionQueueHandler.clearImageScanInfo()` | 전체 초기화 |

---

## 2. 기본 사용 흐름

```java
// 1. VisionService 생성
VisionService vision = VisionServiceFactory.createOpenAI(apiKey);

if (!vision.isEnabled()) {
    log.warn("Vision 비활성 — API 키 미설정");
    return;
}

// 2. 이미지 스캔
try {
    ImageScanInfo info = vision.scanImage("https://example.com/button.png");
    log.info("추출: {} (confidence={})", info.getExtractedText(), info.getConfidence());
} catch (VisionException e) {
    log.error("Vision 실패", e);
}

// 3. ActionQueueHandler 에 주입
ActionQueueHandler aq = new ActionQueueHandler();
aq.setCurrentViewInfo(htmlJson);

for (String imgUrl : imageUrls) {
    try {
        aq.addImageScanInfo(vision.scanImage(imgUrl));
    } catch (VisionException e) {
        log.warn("스캔 실패 (건너뜀): {}", imgUrl);
    }
}

// 4. 기존 플로우 그대로 — 프롬프트에 imageScanInfo 배열이 자동 포함됨
chatRoom.setActionQueueHandler(aq);
JSONObject resp = chatRoom.getChatting().sendPrompt("로그인 버튼 클릭");
```

---

## 3. ImageScanInfo 스키마 (고정)

설계 스케치 §6.3 에 의해 키가 고정됩니다. 변경 금지 — T2-b Tool Use 응답 포맷과 정합 유지.

```json
{
  "imageUrl":      "https://example.com/button.png",
  "extractedText": "Login",
  "confidence":    0.95,
  "timestamp":     "2026-04-22T01:23:45Z",
  "modelUsed":     "gpt-4o-mini"
}
```

`curViewInfo` JSON 의 `imageScanInfo` 키 아래 배열로 AI 에 전달됩니다.

---

## 4. Dedupe 동작

같은 `imageUrl` 로 스캔 정보를 다시 `addImageScanInfo` 하면 **최신값으로 교체**됩니다.
- 캐시가 무효화된 후 재스캔할 때 안전하게 중복 호출 가능
- 향후 Tool Use 경로(T2-b) 에서 LLM 이 같은 이미지를 여러 번 스캔해도 히스토리가 부풀지 않음

---

## 5. 예외 처리 원칙

- `VisionException` 은 **checked** 로 던져진다 (`Exception` 상속). `RuntimeException` 아님
- 호출자는 배치 스캔 중 일부 실패를 catch 하고 no-scan 진행 여부를 결정할 수 있음
- 빈 텍스트가 추출된 경우는 예외가 아닌 `""` 반환 + `confidence=0.0` 으로 `scanImage` 가 표현

---

## 6. 비용/성능 주의사항

- OpenAI Vision 은 **이미지당 유료** — 배치 스캔 전 개수 확인 권장
- 타임아웃 10 초 (연결/읽기) — 큰 이미지는 실패 가능
- 현재 버전은 **캐싱 없음** — 같은 이미지 재호출 시 매번 API 호출됨 (향후 보강 검토)

---

## 7. 설계 스케치와의 정합 지점

본 모듈은 T2-b Tool Use (v0.8.0) 와 다음을 전제로 구현되었습니다:

- `ImageScanInfo` 키 5종 고정
- `VisionService.scanImage(String)` 단일 시그니처 (오버로드 금지)
- `VisionService` 구현체 상태 비유지
- `VisionException` checked 예외
- `ActionQueueHandler.addImageScanInfo` dedupe 기본 적용

Tool Use 본체가 도입되면 `scanImage` 를 Tool 로 등록하는 `VisionTools` 헬퍼가 추가됩니다.
수동 주입 경로와 Tool 경로는 동일한 `ImageScanInfo` 포맷으로 수렴하도록 설계되어 공존 가능합니다.

---

## 8. 관련 문서

- 설계 배경: `doc/tasks/20260421_tool_use_design_sketch.md`
- 초기 계획: `doc/tasks/20260107_vision_api_integration.md`
- Prompt Caching: `doc/caching-guide.md`

---

## 9. 변경 이력

| 버전 | 변경 |
|------|------|
| 0.7.0 | 초기 도입 — OpenAI Vision + ActionQueueHandler 통합 (dedupe) |

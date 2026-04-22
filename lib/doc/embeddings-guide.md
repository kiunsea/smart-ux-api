# Embeddings Guide (v0.9.0+)

smart-ux-api v0.9.0 은 **Provider 중립 임베딩 API** 를 제공합니다.
텍스트를 벡터로 변환하여 UI Object Map 의미 검색, 사용자 의도 매칭 등에 활용할 수 있습니다.

---

## 1. 핵심 API

| API | 설명 |
|-----|------|
| `EmbeddingServiceFactory.createOpenAI(apiKey[, model])` | OpenAI 임베딩 서비스 (기본 `text-embedding-3-small`) |
| `EmbeddingServiceFactory.createGemini(apiKey[, model])` | Gemini 임베딩 서비스 (기본 `text-embedding-004`) |
| `EmbeddingService.embed(text)` | 단일 텍스트 → `float[]` |
| `EmbeddingService.embedBatch(texts)` | 여러 텍스트 → `EmbeddingResult` |
| `Embeddings.cosineSimilarity(a, b)` | 코사인 유사도 |
| `Embeddings.argmax(query, candidates)` | 가장 유사한 후보 인덱스 |
| `Embeddings.topK(query, candidates, k)` | 상위 k 후보 인덱스 (내림차순) |
| `Embeddings.normalize(v)` | L2 정규화 |

---

## 2. 벡터 차원 (provider/model 의존)

| Provider | Model | Dimension |
|----------|-------|-----------|
| OpenAI | `text-embedding-3-small` (기본) | 1536 |
| OpenAI | `text-embedding-3-large` | 3072 |
| OpenAI | `text-embedding-ada-002` (legacy) | 1536 |
| Gemini | `text-embedding-004` (기본) | 768 |
| Gemini | `embedding-001` (legacy) | 768 |

> **차원 혼합 금지** — 한 프로젝트에서는 한 provider 의 벡터만 사용.

---

## 3. 기본 사용 — UI Object Map 의미 검색

```java
import com.smartuxapi.ai.embedding.*;

EmbeddingService em = EmbeddingServiceFactory.createOpenAI(apiKey);

// 1. UI 라벨 배치 임베딩 (한 번만 수행, 캐시)
List<String> uiLabels = Arrays.asList("취소", "확인", "주문하기", "뒤로가기");
EmbeddingResult uiMap = em.embedBatch(uiLabels);

// 2. 사용자 입력으로부터 가장 가까운 라벨 찾기
float[] queryVec = em.embed("돌아가고 싶어");
int idx = Embeddings.argmax(queryVec, uiMap);
System.out.println("매칭된 UI: " + uiLabels.get(idx));   // "뒤로가기"
```

---

## 4. 상위 k 후보 검색

```java
float[] query = em.embed("취소하고 나가고 싶어");
int[] top3 = Embeddings.topK(query, uiMap, 3);
for (int i : top3) {
    float score = Embeddings.cosineSimilarity(query, uiMap.get(i));
    System.out.printf("%s (score=%.3f)%n", uiLabels.get(i), score);
}
```

---

## 5. 환경 변수 팩토리

```java
// OPENAI_API_KEY 환경변수 or openai.api.key 시스템 프로퍼티
EmbeddingService em = EmbeddingServiceFactory.createOpenAIFromEnv();
if (!em.isEnabled()) {
    // fallback 로직
}
```

---

## 6. Provider 매핑

### OpenAI
- 엔드포인트: `POST https://api.openai.com/v1/embeddings`
- 요청: `{ model, input: [...], encoding_format: "float" }`
- 응답: `{ data: [{ embedding: [...] }], usage: { prompt_tokens } }`
- 배치 가능 — 한 번의 호출로 여러 텍스트 처리 (권장)

### Gemini
- 엔드포인트: `POST /v1beta/models/{model}:batchEmbedContents?key={apiKey}`
- 요청: `{ requests: [{ model: "models/...", content: { parts: [{ text }] } }] }`
- 응답: `{ embeddings: [{ values: [...] }] }`
- `text-embedding-004` 는 무료 tier 포함 (rate limit 있음)
- **유의**: Gemini batchEmbedContents 응답은 토큰 사용량을 포함하지 않음 — `EmbeddingResult.getPromptTokens()` 은 항상 0

---

## 7. 제약 / 주의

- **차원 mismatch**: OpenAI 1536 과 Gemini 768 벡터를 섞어 `cosineSimilarity` 호출하면 `IllegalArgumentException`. 프로젝트 내에서 **한 provider 선택**.
- **0-벡터**: `cosineSimilarity(zero, v)` 는 `0.0` 반환 (NaN 방지). 실제 API 는 0-벡터를 거의 반환하지 않음.
- **빈 입력**: `embed("")` 또는 `embedBatch([])` 는 `EmbeddingException` (네트워크 호출 전 fail-fast).
- **입력 크기**: OpenAI 는 텍스트당 최대 약 8192 토큰. 초과하면 API 가 거부. 사전 분할 필요.
- **토큰 비용**:
  - OpenAI `text-embedding-3-small`: $0.02 / 1M 입력 토큰
  - OpenAI `text-embedding-3-large`: $0.13 / 1M
  - Gemini `text-embedding-004`: 무료 tier + paid plan 확인 필요

---

## 8. T3-b 예고 (v0.9.1)

- Cross-provider Fallback + Cost Telemetry 도입 예정.
- Embedding 호출에도 fallback 적용 가능 (설계 스케치 §6).

---

## 9. 관련 문서

- `caching-guide.md` — Prompt Caching (v0.7.0)
- `vision-guide.md` — Vision API (v0.7.0) + Gemini 지원 (v0.8.1)
- `structured-output-guide.md` — Structured Output (v0.8.0)
- `tool-use-guide.md` — Tool Use (v0.8.0)
- `doc/tasks/20260422_embeddings_design_sketch.md` — 설계 스케치 v1.0 (로컬)

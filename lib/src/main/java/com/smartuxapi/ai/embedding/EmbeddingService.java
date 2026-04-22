package com.smartuxapi.ai.embedding;

import java.util.List;

/**
 * 텍스트 임베딩 서비스 인터페이스 — provider 중립.
 *
 * <p>구현체는 상태 비유지(stateless) 여야 한다. Vision / Tool Use 와 동일한 정책.
 *
 * <p>벡터 차원은 provider / model 의존:
 * <ul>
 *   <li>OpenAI {@code text-embedding-3-small} — 1536</li>
 *   <li>OpenAI {@code text-embedding-3-large} — 3072</li>
 *   <li>Gemini {@code text-embedding-004} — 768</li>
 * </ul>
 *
 * <p>여러 provider 를 혼합하여 저장/검색하면 차원 mismatch 가 발생하므로, 한 프로젝트에서
 * 는 한 provider 의 벡터만 사용한다.
 *
 * @since 0.9.0
 */
public interface EmbeddingService {

    /**
     * 단일 텍스트 임베딩 — {@link #embedBatch(List)} 의 편의 래퍼.
     *
     * @param text 입력 텍스트 (null/empty 불가)
     * @return 벡터 (차원 = {@link #getDimension()})
     * @throws EmbeddingException API 호출 실패 등
     */
    float[] embed(String text) throws EmbeddingException;

    /**
     * 배치 임베딩 — 한 번의 API 호출로 여러 텍스트 처리.
     *
     * @param texts 입력 텍스트 리스트 (null/empty 불가)
     * @return 전체 결과 (배열 + 모델 + 토큰)
     * @throws EmbeddingException API 호출 실패 등
     */
    EmbeddingResult embedBatch(List<String> texts) throws EmbeddingException;

    /**
     * 서비스 활성화 여부 — API 키가 유효하게 설정되어 있으면 true.
     */
    boolean isEnabled();

    /**
     * 구성된 모델의 벡터 차원 수 (네트워크 호출 없이 메타데이터 반환).
     */
    int getDimension();

    /**
     * 사용 중인 모델 식별자.
     */
    String getModel();
}

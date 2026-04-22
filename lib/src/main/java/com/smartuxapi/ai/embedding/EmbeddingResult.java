package com.smartuxapi.ai.embedding;

/**
 * 배치 임베딩 호출 결과.
 *
 * <p>벡터는 {@code float[][]} 로 {@code [batch_size][dimension]} 레이아웃.
 * 원본 배열은 생성자에서 방어 복사하지 않는다 — 호출자가 수정하면 내부 상태에 영향.
 * 읽기 전용 사용을 전제로 한다.
 *
 * @since 0.9.0
 */
public final class EmbeddingResult {

    private final float[][] vectors;
    private final int dimension;
    private final String model;
    private final int promptTokens;

    /**
     * @param vectors [batch_size][dimension] — 비어있으면 안 됨
     * @param model 사용된 모델 식별자 (provider 별 naming)
     * @param promptTokens 입력 토큰 수 (미제공 시 0)
     */
    public EmbeddingResult(float[][] vectors, String model, int promptTokens) {
        if (vectors == null || vectors.length == 0) {
            throw new IllegalArgumentException("vectors must not be empty");
        }
        this.vectors = vectors;
        this.dimension = vectors[0].length;
        this.model = model == null ? "" : model;
        this.promptTokens = Math.max(0, promptTokens);
    }

    /** i 번째 벡터 (참조 반환). */
    public float[] get(int i) {
        return vectors[i];
    }

    /** 내부 배열 참조. */
    public float[][] getVectors() {
        return vectors;
    }

    public int size() { return vectors.length; }
    public int getDimension() { return dimension; }
    public String getModel() { return model; }
    public int getPromptTokens() { return promptTokens; }

    @Override
    public String toString() {
        return "EmbeddingResult{size=" + size() + ", dim=" + dimension
                + ", model=" + model + ", promptTokens=" + promptTokens + "}";
    }
}

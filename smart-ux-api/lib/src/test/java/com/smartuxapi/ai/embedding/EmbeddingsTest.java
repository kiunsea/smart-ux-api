package com.smartuxapi.ai.embedding;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Embeddings 유틸 단위 테스트")
class EmbeddingsTest {

    private static final float EPS = 1e-5f;

    @Test
    @DisplayName("cosineSimilarity — 동일 벡터는 1.0, 반대 벡터는 -1.0")
    void testCosine() {
        float[] a = {1f, 0f, 0f};
        float[] b = {1f, 0f, 0f};
        float[] c = {-1f, 0f, 0f};
        float[] d = {0f, 1f, 0f};
        assertEquals(1.0f, Embeddings.cosineSimilarity(a, b), EPS);
        assertEquals(-1.0f, Embeddings.cosineSimilarity(a, c), EPS);
        assertEquals(0.0f, Embeddings.cosineSimilarity(a, d), EPS);
    }

    @Test
    @DisplayName("cosineSimilarity — 0-벡터는 0.0 반환 (NaN 방지)")
    void testZeroVector() {
        float[] zero = {0f, 0f};
        float[] v = {1f, 2f};
        assertEquals(0.0f, Embeddings.cosineSimilarity(zero, v));
        assertEquals(0.0f, Embeddings.cosineSimilarity(v, zero));
    }

    @Test
    @DisplayName("cosineSimilarity — 차원 불일치는 IllegalArgumentException")
    void testDimMismatch() {
        assertThrows(IllegalArgumentException.class,
                () -> Embeddings.cosineSimilarity(new float[]{1f}, new float[]{1f, 2f}));
        assertThrows(IllegalArgumentException.class,
                () -> Embeddings.cosineSimilarity(null, new float[]{1f}));
        assertThrows(IllegalArgumentException.class,
                () -> Embeddings.cosineSimilarity(new float[0], new float[0]));
    }

    @Test
    @DisplayName("argmax — 가장 유사한 후보 인덱스 반환")
    void testArgmax() {
        float[][] candidates = {
                {1f, 0f},      // 가장 비슷
                {0f, 1f},
                {-1f, 0f},
        };
        EmbeddingResult er = new EmbeddingResult(candidates, "m", 0);
        float[] query = {0.9f, 0.1f};
        assertEquals(0, Embeddings.argmax(query, er));
    }

    @Test
    @DisplayName("topK — 내림차순 정렬, k>size 면 size 반환")
    void testTopK() {
        float[][] candidates = {
                {1f, 0f},
                {0.9f, 0.1f},
                {0f, 1f},
                {-1f, 0f}
        };
        EmbeddingResult er = new EmbeddingResult(candidates, "m", 0);
        float[] query = {1f, 0f};

        int[] top2 = Embeddings.topK(query, er, 2);
        assertEquals(2, top2.length);
        assertEquals(0, top2[0]);  // best
        assertEquals(1, top2[1]);  // second best

        int[] top10 = Embeddings.topK(query, er, 10);
        assertEquals(4, top10.length);
        assertEquals(0, top10[0]);
        assertEquals(3, top10[3], "가장 먼 후보가 마지막");
    }

    @Test
    @DisplayName("argmax/topK — 빈 후보는 IllegalArgumentException, k<=0 도 에러")
    void testInvalidArgs() {
        float[] query = {1f};
        assertThrows(IllegalArgumentException.class,
                () -> Embeddings.argmax(query, null));
        EmbeddingResult er = new EmbeddingResult(new float[][]{{1f}}, "m", 0);
        assertThrows(IllegalArgumentException.class,
                () -> Embeddings.topK(query, er, 0));
        assertThrows(IllegalArgumentException.class,
                () -> Embeddings.topK(query, er, -1));
    }

    @Test
    @DisplayName("normalize — L2 norm = 1, 0-벡터는 그대로")
    void testNormalize() {
        float[] v = {3f, 4f};
        float[] n = Embeddings.normalize(v);
        // |v| = 5 → normalized = [0.6, 0.8]
        assertEquals(0.6f, n[0], EPS);
        assertEquals(0.8f, n[1], EPS);
        // 원본은 그대로
        assertEquals(3f, v[0]);

        float[] zero = {0f, 0f};
        float[] nz = Embeddings.normalize(zero);
        assertArrayEquals(new float[]{0f, 0f}, nz);
    }
}

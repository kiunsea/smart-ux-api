package com.smartuxapi.ai.embedding;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 벡터 유사도 유틸리티.
 *
 * <p>모든 메서드는 입력 벡터가 같은 차원임을 가정 — 다르면 {@link IllegalArgumentException}.
 *
 * @since 0.9.0
 */
public final class Embeddings {

    private Embeddings() {}

    /**
     * 코사인 유사도 — 범위 [-1, 1]. 두 벡터 중 하나라도 0-벡터면 0.0 반환 (NaN 방지).
     */
    public static float cosineSimilarity(float[] a, float[] b) {
        checkDim(a, b);
        double dot = 0.0, na = 0.0, nb = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += (double) a[i] * b[i];
            na += (double) a[i] * a[i];
            nb += (double) b[i] * b[i];
        }
        if (na == 0.0 || nb == 0.0) return 0.0f;
        return (float) (dot / (Math.sqrt(na) * Math.sqrt(nb)));
    }

    /**
     * query 와 가장 유사한 candidate 의 인덱스.
     *
     * @throws IllegalArgumentException candidates 가 비어있거나 차원 불일치
     */
    public static int argmax(float[] query, EmbeddingResult candidates) {
        if (candidates == null || candidates.size() == 0) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        int bestIdx = 0;
        float bestScore = -Float.MAX_VALUE;
        for (int i = 0; i < candidates.size(); i++) {
            float s = cosineSimilarity(query, candidates.get(i));
            if (s > bestScore) {
                bestScore = s;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    /**
     * 상위 k 개 후보 인덱스 (점수 내림차순). k 가 candidates 크기보다 크면 전체 반환.
     */
    public static int[] topK(float[] query, EmbeddingResult candidates, int k) {
        if (candidates == null || candidates.size() == 0) {
            throw new IllegalArgumentException("candidates must not be empty");
        }
        if (k <= 0) throw new IllegalArgumentException("k must be positive");
        int effectiveK = Math.min(k, candidates.size());

        // min-heap of size K — keeps K largest; entry = [-1 for unused, ...]
        PriorityQueue<int[]> heap = new PriorityQueue<>(
                effectiveK,
                Comparator.comparingDouble(entry -> Float.intBitsToFloat(entry[1])));

        for (int i = 0; i < candidates.size(); i++) {
            float s = cosineSimilarity(query, candidates.get(i));
            int[] entry = new int[]{i, Float.floatToRawIntBits(s)};
            if (heap.size() < effectiveK) {
                heap.offer(entry);
            } else {
                float smallest = Float.intBitsToFloat(heap.peek()[1]);
                if (s > smallest) {
                    heap.poll();
                    heap.offer(entry);
                }
            }
        }

        // heap → 내림차순 배열
        int[] result = new int[heap.size()];
        float[] scores = new float[heap.size()];
        int cursor = heap.size() - 1;
        while (!heap.isEmpty()) {
            int[] entry = heap.poll();
            result[cursor] = entry[0];
            scores[cursor] = Float.intBitsToFloat(entry[1]);
            cursor--;
        }
        // 점수가 동률일 때 heap 에서 꺼내는 순서가 보장되지 않으므로 정렬로 확정
        // (result 와 scores 를 zip 정렬)
        Integer[] idx = new Integer[result.length];
        for (int i = 0; i < idx.length; i++) idx[i] = i;
        java.util.Arrays.sort(idx, (x, y) -> Float.compare(scores[y], scores[x]));
        int[] sorted = new int[result.length];
        for (int i = 0; i < idx.length; i++) sorted[i] = result[idx[i]];
        return sorted;
    }

    /**
     * L2 normalize — in-place 가 아닌 새 배열 반환. 이미 정규화된 벡터끼리는 코사인 = dot.
     */
    public static float[] normalize(float[] v) {
        if (v == null) throw new IllegalArgumentException("v must not be null");
        double n = 0.0;
        for (float x : v) n += (double) x * x;
        if (n == 0.0) return v.clone();
        float norm = (float) Math.sqrt(n);
        float[] out = new float[v.length];
        for (int i = 0; i < v.length; i++) out[i] = v[i] / norm;
        return out;
    }

    private static void checkDim(float[] a, float[] b) {
        if (a == null || b == null) throw new IllegalArgumentException("vector must not be null");
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "dimension mismatch: " + a.length + " vs " + b.length);
        }
        if (a.length == 0) throw new IllegalArgumentException("vector must not be empty");
    }
}

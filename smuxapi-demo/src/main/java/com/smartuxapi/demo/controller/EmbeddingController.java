package com.smartuxapi.demo.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartuxapi.ai.embedding.EmbeddingResult;
import com.smartuxapi.ai.embedding.EmbeddingService;
import com.smartuxapi.ai.embedding.EmbeddingServiceFactory;
import com.smartuxapi.ai.embedding.Embeddings;
import com.smartuxapi.util.PropertiesUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Embedding 의미 검색 데모 컨트롤러 (smart-ux-api v0.9.0 T3-a).
 *
 * <p>POST /demo/embedding/search
 * <pre>{
 *   "provider": "openai" | "gemini",   // 기본 "openai"
 *   "query": "돌아가고 싶어",
 *   "candidates": ["취소", "확인", "주문하기", "뒤로가기"],
 *   "top_k": 3                           // 선택, 기본 3
 * }</pre>
 *
 * <p>반환:
 * <pre>{
 *   "query": "...",
 *   "provider": "openai",
 *   "model": "text-embedding-3-small",
 *   "dimension": 1536,
 *   "prompt_tokens": 42,
 *   "matches": [
 *     { "index": 3, "label": "뒤로가기", "score": 0.82 },
 *     { "index": 0, "label": "취소",   "score": 0.74 },
 *     { "index": 1, "label": "확인",   "score": 0.31 }
 *   ]
 * }</pre>
 *
 * <p>실제 API 호출은 query + candidates 의 (N+1) 텍스트에 대한 배치 embedding 1회.
 * 이후 cosine 계산은 로컬.
 */
@RestController
@RequestMapping("/demo/embedding")
public class EmbeddingController {

    private static final Logger log = LogManager.getLogger(EmbeddingController.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_TOP_K = 3;
    private static final int MAX_CANDIDATES = 100;

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JSONObject> handleSearch(@RequestBody String body, HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("UTF-8");

        JsonNode root;
        try {
            root = MAPPER.readTree(body == null ? "{}" : body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("Invalid JSON: " + e.getMessage()));
        }

        String provider = root.path("provider").asText("openai");
        String query = root.path("query").asText("");
        int topK = root.path("top_k").asInt(DEFAULT_TOP_K);

        if (query.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("query is required"));
        }

        JsonNode candidatesNode = root.path("candidates");
        if (!candidatesNode.isArray() || candidatesNode.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err("candidates must be a non-empty array"));
        }
        if (candidatesNode.size() > MAX_CANDIDATES) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(err("candidates size exceeds " + MAX_CANDIDATES));
        }

        List<String> candidates = new ArrayList<>(candidatesNode.size());
        for (JsonNode n : candidatesNode) {
            String s = n.asText("");
            if (s.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(err("candidates contains empty string"));
            }
            candidates.add(s);
        }

        EmbeddingService em = pickService(provider);
        if (em == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(err("unknown provider or API key missing: " + provider));
        }

        try {
            // query + candidates 를 한 번에 배치 임베딩 (API 호출 1회)
            List<String> batch = new ArrayList<>(candidates.size() + 1);
            batch.add(query);
            batch.addAll(candidates);

            EmbeddingResult er = em.embedBatch(batch);
            float[] queryVec = er.get(0);

            // candidates 용 별도 EmbeddingResult 뷰 생성
            float[][] candVecs = new float[candidates.size()][];
            for (int i = 0; i < candidates.size(); i++) {
                candVecs[i] = er.get(i + 1);
            }
            EmbeddingResult candRes = new EmbeddingResult(candVecs, er.getModel(), 0);

            int effectiveK = Math.min(topK, candidates.size());
            int[] indices = Embeddings.topK(queryVec, candRes, effectiveK);

            JSONArray matches = new JSONArray();
            for (int idx : indices) {
                JSONObject m = new JSONObject();
                m.put("index", idx);
                m.put("label", candidates.get(idx));
                m.put("score", (double) Embeddings.cosineSimilarity(queryVec, candRes.get(idx)));
                matches.add(m);
            }

            JSONObject res = new JSONObject();
            res.put("query", query);
            res.put("provider", provider);
            res.put("model", er.getModel());
            res.put("dimension", er.getDimension());
            res.put("prompt_tokens", er.getPromptTokens());
            res.put("matches", matches);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("embedding search 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(err(e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    private EmbeddingService pickService(String provider) {
        if ("openai".equalsIgnoreCase(provider)) {
            String key = PropertiesUtil.get("OPENAI_API_KEY");
            if (key == null || key.isBlank()) return null;
            return EmbeddingServiceFactory.createOpenAI(key);
        }
        if ("gemini".equalsIgnoreCase(provider)) {
            String key = PropertiesUtil.get("GEMINI_API_KEY");
            if (key == null || key.isBlank()) return null;
            return EmbeddingServiceFactory.createGemini(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private JSONObject err(String msg) {
        JSONObject o = new JSONObject();
        o.put("status", "error");
        o.put("message", msg);
        return o;
    }
}

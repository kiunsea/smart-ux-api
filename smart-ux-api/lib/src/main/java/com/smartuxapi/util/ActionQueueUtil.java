package com.smartuxapi.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ActionQueueUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 문자열에서 JSON을 추출하고 JsonNode로 반환
     */
    public static JsonNode extractActionQueue(String message) {
        List<Function<String, String>> extractors = Arrays.asList(
                ActionQueueUtil::extractJsonContent,
                ActionQueueUtil::extractJson,
                ActionQueueUtil::extractJsonBlock,
                ActionQueueUtil::extractJsonByBracketCounting,
                ActionQueueUtil::extractAndParseJson
        );

        for (Function<String, String> extractor : extractors) {
            String jsonStr = extractor.apply(message);
            JsonNode node = parseJson(jsonStr);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    /**
     * 문자열을 JsonNode로 파싱
     */
    private static JsonNode parseJson(String jsonStr) {
        if (jsonStr == null) return null;
        try {
            JsonNode jsonNode = objectMapper.readTree(normalize(jsonStr));
            System.out.println("\nJSON 파싱 성공: 유효한 JSON입니다.");
            return jsonNode;
        } catch (IOException e) {
            System.err.println("\n파싱 오류: 유효하지 않은 JSON - " + e.getMessage());
        }
        return null;
    }

    /**
     * 문자열 내 ```json\n...\n``` 블록 추출
     */
    private static String extractJsonContent(String text) {
        String start = "```json\n";
        String end = "\n```\n";

        int startIndex = text.indexOf(start);
        if (startIndex == -1) return null;

        startIndex += start.length();
        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) {
            System.err.println("경고: 시작 마커는 찾았으나 끝 마커는 없음");
            return null;
        }

        return normalize(text.substring(startIndex, endIndex));
    }

    /**
     * 문자열 내 ```json ... ``` 블록 추출
     */
    private static String extractJson(String text) {
        String start = "```json";
        String end = "```";

        int startIndex = text.indexOf(start);
        if (startIndex == -1) return null;
        startIndex += start.length();

        int endIndex = text.indexOf(end, startIndex);
        if (endIndex == -1) return null;

        return normalize(text.substring(startIndex, endIndex).trim());
    }

    /**
     * 괄호 균형 기반 JSON 추출
     */
    private static String extractJsonBlock(String text) {
        int startIndex = -1;
        int braceCount = 0;
        char startChar = 0;
        char endChar = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '{' || c == '[') {
                if (startIndex == -1) {
                    startIndex = i;
                    startChar = c;
                    endChar = (c == '{') ? '}' : ']';
                    braceCount = 1;
                } else if (c == startChar) {
                    braceCount++;
                }
            } else if (c == endChar) {
                braceCount--;
                if (braceCount == 0 && startIndex != -1) {
                    return normalize(text.substring(startIndex, i + 1));
                }
            }
        }
        return null;
    }

    /**
     * 문자열 내 첫 JSON 블록을 괄호 카운팅으로 추출
     */
    private static String extractJsonByBracketCounting(String text) {
        int startIndex = -1;
        char startChar = ' ';
        char endChar = ' ';

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{' || c == '[') {
                startIndex = i;
                startChar = c;
                endChar = (c == '{') ? '}' : ']';
                break;
            }
        }

        if (startIndex == -1) return null;

        int count = 0;
        boolean inQuote = false;

        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '"' && (i == 0 || text.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }

            if (!inQuote) {
                if (c == startChar) count++;
                else if (c == endChar) count--;
            }

            if (count == 0) {
                return normalize(text.substring(startIndex, i + 1));
            }
        }
        return null;
    }

    /**
     * 파싱 시도 방식으로 JSON 추출
     */
    private static String extractAndParseJson(String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{' || c == '[') {
                String candidate = text.substring(i);
                for (int j = candidate.length(); j > 0; j--) {
                    String sub = candidate.substring(0, j);
                    try {
                        objectMapper.readTree(normalize(sub));
                        return normalize(sub);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return null;
    }

    /**
     * 특수 공백 문자 제거 및 trim
     */
    private static String normalize(String text) {
        return text == null ? null : text.replace('\u00A0', ' ').trim();
    }
}

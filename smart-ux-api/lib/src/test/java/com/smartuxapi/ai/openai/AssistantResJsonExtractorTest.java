package com.smartuxapi.ai.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AssistantResJsonExtractorTest {
    public static void main(String[] args) {
        String jsonString = """
                {
                    "id": "resp_688774bd12f08192a6c27b3cb59560710a13fbb018c758a9",
                    "object": "response",
                    "created_at": 1753707709,
                    "status": "completed",
                    "background": false,
                    "error": null,
                    "incomplete_details": null,
                    "instructions": null,
                    "max_output_tokens": null,
                    "max_tool_calls": null,
                    "model": "gpt-4.1-2025-04-14",
                    "output": [
                        {
                            "id": "msg_688774bd9ec88192a0e0600a1299f1910a13fbb018c758a9",
                            "type": "message",
                            "status": "completed",
                            "content": [
                                {
                                    "type": "output_text",
                                    "annotations": [],
                                    "logprobs": [],
                                    "text": "Hello! 😊 How can I help you today?"
                                }
                            ],
                            "role": "assistant"
                        }
                    ],
                    "parallel_tool_calls": true,
                    "previous_response_id": null,
                    "prompt_cache_key": null,
                    "reasoning": {
                        "effort": null,
                        "summary": null
                    },
                    "safety_identifier": null,
                    "service_tier": "default",
                    "store": true,
                    "temperature": 1.0,
                    "text": {
                        "format": {
                            "type": "text"
                        }
                    },
                    "tool_choice": "auto",
                    "tools": [],
                    "top_logprobs": 0,
                    "top_p": 1.0,
                    "truncation": "disabled",
                    "usage": {
                        "input_tokens": 10,
                        "input_tokens_details": {
                            "cached_tokens": 0
                        },
                        "output_tokens": 11,
                        "output_tokens_details": {
                            "reasoning_tokens": 0
                        },
                        "total_tokens": 21
                    },
                    "user": null,
                    "metadata": {}
                }
                """; // Java 15+ 텍스트 블록

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonString);

            // 1. "output" 배열 값 추출
            JsonNode outputArray = rootNode.get("output");

            if (outputArray != null && outputArray.isArray()) {
                // 2. 해당 array에서 role이 "assistant"인 JSON 인스턴스 찾기
                for (JsonNode outputItem : outputArray) {
                    JsonNode roleNode = outputItem.get("role");
                    if (roleNode != null && "assistant".equals(roleNode.asText())) {
                        // 3. 해당 instance의 "content" array 값 추출
                        JsonNode contentArray = outputItem.get("content");
                        if (contentArray != null && contentArray.isArray() && contentArray.size() > 0) {
                            // 4. 첫 번째 JSON 인스턴스의 "text" 값 추출
                            JsonNode firstContentItem = contentArray.get(0);
                            JsonNode textNode = firstContentItem.get("text");

                            if (textNode != null) {
                                System.out.println("추출된 텍스트: " + textNode.asText());
                                return; // 원하는 값을 찾았으므로 종료
                            }
                        }
                    }
                }
                System.out.println("Assistant 역할의 content 또는 text를 찾을 수 없습니다.");
            } else {
                System.out.println("output 배열을 찾을 수 없거나 배열이 아닙니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("JSON 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

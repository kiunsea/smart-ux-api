package com.smartuxapi.ai.openai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Assistant Response JSON 추출 테스트")
public class AssistantResJsonExtractorTest {
    
    @Test
    @DisplayName("Assistant 응답에서 텍스트 추출 테스트")
    public void testExtractTextFromAssistantResponse() {
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
            assertNotNull(outputArray, "output 배열이 있어야 합니다");
            assertTrue(outputArray.isArray(), "output은 배열이어야 합니다");

            // 2. 해당 array에서 role이 "assistant"인 JSON 인스턴스 찾기
            boolean found = false;
            for (JsonNode outputItem : outputArray) {
                JsonNode roleNode = outputItem.get("role");
                if (roleNode != null && "assistant".equals(roleNode.asText())) {
                    // 3. 해당 instance의 "content" array 값 추출
                    JsonNode contentArray = outputItem.get("content");
                    assertNotNull(contentArray, "content 배열이 있어야 합니다");
                    assertTrue(contentArray.isArray() && contentArray.size() > 0, "content는 비어있지 않은 배열이어야 합니다");
                    
                    // 4. 첫 번째 JSON 인스턴스의 "text" 값 추출
                    JsonNode firstContentItem = contentArray.get(0);
                    JsonNode textNode = firstContentItem.get("text");

                    assertNotNull(textNode, "text 노드가 있어야 합니다");
                    String extractedText = textNode.asText();
                    assertNotNull(extractedText, "추출된 텍스트가 null이 아니어야 합니다");
                    assertFalse(extractedText.isEmpty(), "추출된 텍스트가 비어있지 않아야 합니다");
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Assistant 역할의 응답을 찾아야 합니다");

        } catch (Exception e) {
            fail("JSON 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}

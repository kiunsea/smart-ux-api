package com.smartuxapi.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigLoader 테스트")
public class ConfigLoaderTest {

    @Test
    @DisplayName("classpath에서 config.json 로드 성공")
    void loadDefaultConfig() {
        JsonNode config = ConfigLoader.loadConfigFromClasspath();

        assertNotNull(config, "config.json이 classpath에서 로드되어야 합니다");
        assertTrue(config.has("prompt"), "config.json에 prompt 필드가 있어야 합니다");
    }

    @Test
    @DisplayName("null 파일명 전달 시 기본값(config.json) 사용")
    void loadWithNullFileName() {
        JsonNode config = ConfigLoader.loadConfigFromClasspath(null);

        assertNotNull(config, "null 파일명 시 기본 config.json이 로드되어야 합니다");
        assertTrue(config.has("prompt"), "config.json에 prompt 필드가 있어야 합니다");
    }

    @Test
    @DisplayName("존재하지 않는 파일 로드 시 null 반환")
    void loadNonExistentFile() {
        JsonNode config = ConfigLoader.loadConfigFromClasspath("nonexistent_file_12345.json");

        assertNull(config, "존재하지 않는 파일은 null을 반환해야 합니다");
    }

    @Test
    @DisplayName("잘못된 JSON 형식 파일 로드 시 null 반환")
    void loadInvalidJsonFile() {
        JsonNode config = ConfigLoader.loadConfigFromClasspath("invalid.json");

        assertNull(config, "잘못된 JSON 파일은 null을 반환해야 합니다");
    }

    @Test
    @DisplayName("로드된 설정의 prompt 구조 검증")
    void verifyPromptStructure() {
        JsonNode config = ConfigLoader.loadConfigFromClasspath();

        assertNotNull(config, "config.json이 로드되어야 합니다");

        JsonNode prompt = config.get("prompt");
        assertNotNull(prompt, "prompt 필드가 존재해야 합니다");
        assertTrue(prompt.has("cur_view_info"), "prompt에 cur_view_info가 있어야 합니다");
        assertTrue(prompt.has("action_queue"), "prompt에 action_queue가 있어야 합니다");
        assertTrue(prompt.get("cur_view_info").isArray(), "cur_view_info는 배열이어야 합니다");
        assertTrue(prompt.get("action_queue").isArray(), "action_queue는 배열이어야 합니다");
    }
}

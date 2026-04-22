package com.smartuxapi.ai.debug;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DebugConfig 테스트")
public class DebugConfigTest {

    @Test
    @DisplayName("싱글톤 인스턴스 반환 확인")
    void singletonInstance() {
        DebugConfig config1 = DebugConfig.getInstance();
        DebugConfig config2 = DebugConfig.getInstance();

        assertNotNull(config1, "인스턴스가 null이 아니어야 합니다");
        assertSame(config1, config2, "같은 인스턴스를 반환해야 합니다");
    }

    @Test
    @DisplayName("setDebugMode 설정 테스트")
    void setDebugMode() {
        DebugConfig config = DebugConfig.getInstance();
        boolean original = config.isDebugMode();

        try {
            config.setDebugMode(true);
            assertTrue(config.isDebugMode(), "debugMode가 true여야 합니다");

            config.setDebugMode(false);
            assertFalse(config.isDebugMode(), "debugMode가 false여야 합니다");
        } finally {
            config.setDebugMode(original);
        }
    }

    @Test
    @DisplayName("setOutputPath 설정 시 구분자 자동 추가")
    void setOutputPathAppendsSeparator() {
        DebugConfig config = DebugConfig.getInstance();
        String original = config.getOutputPath();

        try {
            config.setOutputPath("/tmp/test_output");
            String path = config.getOutputPath();
            assertTrue(
                path.endsWith("/") || path.endsWith("\\") || path.endsWith(java.io.File.separator),
                "출력 경로 끝에 구분자가 자동으로 추가되어야 합니다"
            );
        } finally {
            config.setOutputPath(original);
        }
    }

    @Test
    @DisplayName("이미 구분자가 있는 경로 설정")
    void setOutputPathWithExistingSeparator() {
        DebugConfig config = DebugConfig.getInstance();
        String original = config.getOutputPath();

        try {
            config.setOutputPath("/tmp/test_output/");
            assertEquals("/tmp/test_output/", config.getOutputPath(),
                "이미 구분자가 있으면 추가하지 않아야 합니다");
        } finally {
            config.setOutputPath(original);
        }
    }

    @Test
    @DisplayName("setFilePrefix 설정 테스트")
    void setFilePrefix() {
        DebugConfig config = DebugConfig.getInstance();
        String original = config.getFilePrefix();

        try {
            config.setFilePrefix("test_prefix");
            assertEquals("test_prefix", config.getFilePrefix(),
                "filePrefix가 설정값과 같아야 합니다");
        } finally {
            config.setFilePrefix(original);
        }
    }

    @Test
    @DisplayName("절대 경로 반환 테스트")
    void getAbsoluteOutputPath() {
        DebugConfig config = DebugConfig.getInstance();
        String absolutePath = config.getAbsoluteOutputPath();

        assertNotNull(absolutePath, "절대 경로가 null이 아니어야 합니다");
        assertFalse(absolutePath.isEmpty(), "절대 경로가 빈 문자열이 아니어야 합니다");
    }

    @Test
    @DisplayName("초기화 상태 확인")
    void isInitialized() {
        DebugConfig config = DebugConfig.getInstance();
        // config.json이 classpath에 있으므로 초기화되어야 함
        assertTrue(config.isInitialized(), "config.json이 존재하므로 초기화되어야 합니다");
    }
}

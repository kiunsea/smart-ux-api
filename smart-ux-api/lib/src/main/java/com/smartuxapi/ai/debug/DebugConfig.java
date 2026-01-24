package com.smartuxapi.ai.debug;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartuxapi.ai.ConfigLoader;

/**
 * 디버그 모드 설정 관리 클래스
 * config.json 파일에서 설정을 로드합니다.
 */
public class DebugConfig {

    private static final Logger logger = LogManager.getLogger(DebugConfig.class);

    private static DebugConfig instance;

    private boolean debugMode = false;
    private String outputPath = "./conversation_log/";
    private String filePrefix = "chatroom";

    private boolean initialized = false;

    private DebugConfig() {
        loadConfig();
    }

    /**
     * 싱글톤 인스턴스 반환
     */
    public static synchronized DebugConfig getInstance() {
        if (instance == null) {
            instance = new DebugConfig();
        }
        return instance;
    }

    /**
     * 설정 다시 로드 (테스트용)
     */
    public void reload() {
        loadConfig();
    }

    /**
     * config.json 파일에서 설정을 로드합니다.
     * ConfigLoader를 사용하여 JAR 디렉터리 우선, classpath 순으로 로드합니다.
     */
    private void loadConfig() {
        try {
            JsonNode root = ConfigLoader.loadConfigFromClasspath();

            if (root != null) {
                parseConfigFromJson(root);
                initialized = true;
            } else {
                logger.warn("config.json not found, using defaults");
            }
        } catch (Exception e) {
            logger.warn("Failed to load config from config.json, using defaults: {}", e.getMessage());
        }

        logConfiguration();
    }

    /**
     * JsonNode에서 설정을 파싱합니다.
     */
    private void parseConfigFromJson(JsonNode root) {
        // debug-mode
        if (root.has("debug-mode")) {
            debugMode = root.get("debug-mode").asBoolean(false);
        }

        // debug-output-path
        if (root.has("debug-output-path")) {
            String pathValue = root.get("debug-output-path").asText("./conversation_log/").trim();
            if (!pathValue.isEmpty()) {
                outputPath = pathValue;
                // 경로 끝에 구분자가 없으면 추가
                if (!outputPath.endsWith("/") && !outputPath.endsWith("\\")) {
                    outputPath += File.separator;
                }
            }
        }

        // debug-file-prefix
        if (root.has("debug-file-prefix")) {
            String prefixValue = root.get("debug-file-prefix").asText("chatroom").trim();
            if (!prefixValue.isEmpty()) {
                filePrefix = prefixValue;
            }
        }
    }

    /**
     * JAR 디렉터리 또는 프로젝트 루트 디렉터리를 반환합니다.
     */
    private String getJarDirectory() {
        try {
            URL location = DebugConfig.class.getProtectionDomain().getCodeSource().getLocation();
            URI uri = location.toURI();
            File file = new File(uri);

            if (file.isFile()) {
                // JAR 파일인 경우 부모 디렉터리 반환
                return file.getParent();
            } else {
                // IDE 실행 시 프로젝트 루트 찾기
                String path = file.getAbsolutePath();

                // build/classes/java/main, build/classes, target/classes 등 제거
                String[] patterns = {
                    File.separator + "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main",
                    File.separator + "build" + File.separator + "classes",
                    File.separator + "target" + File.separator + "classes",
                    File.separator + "out" + File.separator + "production" + File.separator + "classes"
                };

                for (String pattern : patterns) {
                    int index = path.indexOf(pattern);
                    if (index > 0) {
                        path = path.substring(0, index);
                        break;
                    }
                }

                return path;
            }
        } catch (Exception e) {
            logger.warn("Failed to determine JAR directory, using current directory: {}", e.getMessage());
            return System.getProperty("user.dir");
        }
    }

    /**
     * 현재 설정을 로그에 출력합니다.
     */
    private void logConfiguration() {
        if (debugMode) {
            logger.info("=".repeat(80));
            logger.info("DebugConfig initialized (Debug Mode ENABLED)");
            logger.info("  debug-mode: {}", debugMode);
            logger.info("  debug-output-path: {}", outputPath);
            logger.info("  debug-output-path (absolute): {}", getAbsoluteOutputPath());
            logger.info("  debug-file-prefix: {}", filePrefix);
            logger.info("=".repeat(80));
        } else {
            logger.debug("DebugConfig initialized (Debug Mode disabled)");
        }
    }

    /**
     * 디버그 모드 활성화 여부 반환
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * 디버그 모드 설정 (프로그래밍 방식)
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (debugMode) {
            logger.info("Debug mode enabled programmatically");
        }
    }

    /**
     * 출력 경로 반환 (설정 값 그대로)
     */
    public String getOutputPath() {
        return outputPath;
    }

    /**
     * 출력 경로 설정
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
        if (!this.outputPath.endsWith("/") && !this.outputPath.endsWith("\\")) {
            this.outputPath += File.separator;
        }
    }

    /**
     * 절대 경로로 변환된 출력 경로 반환
     */
    public String getAbsoluteOutputPath() {
        if (new File(outputPath).isAbsolute()) {
            return outputPath;
        }

        String jarDir = getJarDirectory();
        String relativePath = outputPath.replaceFirst("^\\./", "").replaceFirst("^/", "");
        return jarDir + File.separator + relativePath;
    }

    /**
     * 파일명 접두사 반환
     */
    public String getFilePrefix() {
        return filePrefix;
    }

    /**
     * 파일명 접두사 설정
     */
    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    /**
     * 설정이 정상적으로 초기화되었는지 확인
     */
    public boolean isInitialized() {
        return initialized;
    }
}

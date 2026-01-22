package com.smartuxapi.demo;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * 브라우저 자동 실행 유틸리티
 * 서버가 준비될 때까지 대기 후 브라우저를 자동으로 실행합니다.
 * 
 * @author KIUNSEA
 */
public class BrowserLauncher {

    private static final Logger logger = LogManager.getLogger(BrowserLauncher.class);
    private static final int DEFAULT_PORT = 8080;
    private static final String CONTEXT_PATH = "/smuxapi/";
    private static final int MAX_WAIT_SECONDS = 30;
    private static final int CHECK_INTERVAL_MS = 1000;
    
    /**
     * 애플리케이션 URL을 동적으로 생성합니다.
     * smuxapi-demo.yml의 SERVER_PORT 설정을 참조합니다.
     */
    private static String getAppUrl() {
        int port = getPortFromYaml();
        return "http://localhost:" + port + CONTEXT_PATH;
    }

    /**
     * 서버가 준비될 때까지 대기한 후 브라우저를 실행합니다.
     * 별도 스레드에서 실행되어 메인 애플리케이션 시작을 차단하지 않습니다.
     */
    public static void launchWhenReady() {
        Thread browserThread = new Thread(
            () -> {
                try {
                    logger.info("서버 준비 대기 중...");

                    if (waitForServerReady()) {
                        logger.info("서버 준비 완료, 브라우저 실행 중...");
                        openBrowser();
                    } else {
                        logger.warn("서버 준비 시간 초과. 브라우저를 수동으로 열어주세요: {}", getAppUrl());
                    }
                } catch (Exception e) {
                    logger.error("브라우저 실행 중 오류 발생", e);
                }
            },
            "BrowserLauncher");

        browserThread.setDaemon(true);
        browserThread.start();
    }

    /**
     * 브라우저를 즉시 실행합니다 (서버 대기 없음)
     */
    public static void launch() {
        try {
            openBrowser();
        } catch (Exception e) {
            logger.error("브라우저 실행 실패", e);
        }
    }

    /**
     * 서버가 준비될 때까지 대기합니다.
     *
     * @return 서버 준비 완료 시 true, 타임아웃 시 false
     */
    private static boolean waitForServerReady() {
        int attempts = 0;
        int maxAttempts = MAX_WAIT_SECONDS * 1000 / CHECK_INTERVAL_MS;

        while (attempts < maxAttempts) {
            if (isServerReady()) {
                return true;
            }

            try {
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            attempts++;
        }

        return false;
    }

    /**
     * 서버가 응답하는지 확인합니다.
     *
     * @return 서버가 응답하면 true, 아니면 false
     */
    private static boolean isServerReady() {
        try {
            URL url = new URL(getAppUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200 || responseCode == 302;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 기본 브라우저로 애플리케이션 URL을 엽니다.
     */
    private static void openBrowser() {
        try {
            String appUrl = getAppUrl();
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(appUrl));
                logger.info("브라우저 실행 완료: {}", appUrl);
            } else {
                logger.warn("Desktop.browse가 지원되지 않습니다. URL을 수동으로 열어주세요: {}", appUrl);

                // Windows에서 cmd를 통해 브라우저 열기 시도
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + appUrl);
                    logger.info("rundll32로 브라우저 실행 완료");
                }
            }
        } catch (Exception e) {
            logger.error("브라우저 실행 실패: {}", e.getMessage());
        }
    }
    
    /**
     * smuxapi-demo.yml 파일에서 SERVER_PORT 값을 읽어서 포트를 반환합니다.
     * 파일이 없거나 설정이 없으면 기본값(8080)을 반환합니다.
     */
    private static int getPortFromYaml() {
        // 먼저 시스템 프로퍼티에서 포트 확인 (SmuxapiDemoApplication에서 설정한 경우)
        String systemPort = System.getProperty("server.port");
        if (systemPort != null && !systemPort.isEmpty()) {
            try {
                return Integer.parseInt(systemPort);
            } catch (NumberFormatException e) {
                // 무시하고 YAML 파일에서 읽기 시도
            }
        }
        
        // YAML 파일에서 직접 읽기
        try {
            String jarDir = getJarDirectory();
            String yamlPath = jarDir + File.separator + "smuxapi-demo.yml";
            
            File yamlFile = new File(yamlPath);
            
            if (yamlFile.exists() && yamlFile.isFile()) {
                InputStream is = null;
                try {
                    is = new FileInputStream(yamlFile);
                    Yaml yaml = new Yaml();
                    Map<String, Object> data = yaml.load(is);
                    
                    if (data != null && data.containsKey("SERVER_PORT")) {
                        Object portObj = data.get("SERVER_PORT");
                        if (portObj != null) {
                            String portValue = portObj.toString().trim();
                            if (!portValue.isEmpty()) {
                                try {
                                    return Integer.parseInt(portValue);
                                } catch (NumberFormatException e) {
                                    logger.warn("SERVER_PORT 값이 유효한 숫자가 아닙니다: {}", portValue);
                                }
                            }
                        }
                    }
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            // 무시
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("smuxapi-demo.yml 파일 읽기 중 오류 발생 (기본 포트 사용): {}", e.getMessage());
        }
        
        // 기본값 반환
        return DEFAULT_PORT;
    }
    
    /**
     * JAR 파일이 실행되는 디렉터리 경로를 반환합니다.
     */
    private static String getJarDirectory() {
        try {
            // JAR 파일의 경로에서 디렉터리 추출
            URL location = BrowserLauncher.class.getProtectionDomain().getCodeSource().getLocation();
            URI uri = location.toURI();
            File codeSourceFile = new File(uri);
            
            if (codeSourceFile.isFile() && codeSourceFile.getName().endsWith(".jar")) {
                // JAR 파일인 경우
                return codeSourceFile.getParent();
            } else {
                // 개발 환경인 경우 (IDE에서 실행)
                // 프로젝트 루트 디렉터리 찾기
                File currentDir = codeSourceFile;
                while (currentDir != null && currentDir.exists()) {
                    File buildGradle = new File(currentDir, "build.gradle.kts");
                    if (buildGradle.exists()) {
                        return currentDir.getAbsolutePath();
                    }
                    currentDir = currentDir.getParentFile();
                }
                // 찾지 못하면 현재 작업 디렉터리 사용
                return System.getProperty("user.dir");
            }
        } catch (Exception e) {
            // 오류 발생 시 현재 작업 디렉터리 사용
            return System.getProperty("user.dir");
        }
    }
}

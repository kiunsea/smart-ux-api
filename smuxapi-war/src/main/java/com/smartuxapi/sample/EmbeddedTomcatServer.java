package com.smartuxapi.sample;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Embedded Tomcat 서버를 사용하여 WAR 애플리케이션을 실행하는 메인 클래스
 * 
 * 사용법:
 *   java -cp <classpath> com.smartuxapi.sample.EmbeddedTomcatServer [--port=8080] [--context-path=/]
 * 
 * 또는 Gradle로 실행:
 *   ./gradlew :smuxapi-war:run
 */
public class EmbeddedTomcatServer {
    
    private static final Logger log = LogManager.getLogger(EmbeddedTomcatServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CONTEXT_PATH = "/";
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        String contextPath = DEFAULT_CONTEXT_PATH;
        
        // 명령줄 인수 파싱
        for (String arg : args) {
            if (arg.startsWith("--port=")) {
                port = Integer.parseInt(arg.substring(7));
            } else if (arg.startsWith("--context-path=")) {
                contextPath = arg.substring(15);
            }
        }
        
        EmbeddedTomcatServer server = new EmbeddedTomcatServer();
        try {
            server.start(port, contextPath);
            log.info("========================================");
            log.info("🚀 Embedded Tomcat Server Started!");
            log.info("📍 Port: {}", port);
            log.info("📍 Context Path: {}", contextPath);
            log.info("🌐 URL: http://localhost:{}{}", port, contextPath);
            log.info("========================================");
            log.info("Press Ctrl+C to stop the server");
            
            // 서버가 종료될 때까지 대기
            Thread.currentThread().join();
        } catch (Exception e) {
            log.error("Failed to start server", e);
            System.exit(1);
        }
    }
    
    public void start(int port, String contextPath) throws Exception {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setHostname("localhost");
        
        // 임시 디렉토리 생성
        String tempDir = System.getProperty("java.io.tmpdir");
        Path baseDir = Paths.get(tempDir, "embedded-tomcat-" + port);
        Files.createDirectories(baseDir);
        tomcat.setBaseDir(baseDir.toString());
        
        // 웹앱 디렉토리 찾기
        String webappDir = findWebappDirectory();
        if (webappDir == null) {
            throw new IllegalStateException("웹앱 디렉토리를 찾을 수 없습니다. src/main/webapp 또는 build/libs를 확인하세요.");
        }
        
        log.info("웹앱 디렉토리: {}", webappDir);
        
        // Context 추가
        Context context = tomcat.addWebapp(contextPath, new File(webappDir).getAbsolutePath());
        
        // UTF-8 인코딩 설정
        context.setRequestCharacterEncoding("UTF-8");
        context.setResponseCharacterEncoding("UTF-8");
        
        // JSP 컴파일러 설정
        tomcat.getConnector().setURIEncoding("UTF-8");
        
        // 서버 시작
        tomcat.start();
        
        // 종료 시 정리 작업
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("서버를 종료하는 중...");
                tomcat.stop();
                tomcat.destroy();
                log.info("서버가 종료되었습니다.");
            } catch (LifecycleException e) {
                log.error("서버 종료 중 오류 발생", e);
            }
        }));
    }
    
    /**
     * 웹앱 디렉토리를 찾습니다.
     * 1. 개발 환경: src/main/webapp
     * 2. 빌드 환경: build/exploded/smuxapi-war 또는 WAR 파일에서 추출
     */
    private String findWebappDirectory() {
        // 1. 개발 환경 확인 (src/main/webapp)
        Path devWebapp = Paths.get("src/main/webapp");
        if (Files.exists(devWebapp) && Files.isDirectory(devWebapp)) {
            return devWebapp.toAbsolutePath().toString();
        }
        
        // 2. 상대 경로로 확인 (프로젝트 루트에서 실행하는 경우)
        Path relativeWebapp = Paths.get("smuxapi-war/src/main/webapp");
        if (Files.exists(relativeWebapp) && Files.isDirectory(relativeWebapp)) {
            return relativeWebapp.toAbsolutePath().toString();
        }
        
        // 3. 절대 경로로 확인
        String userDir = System.getProperty("user.dir");
        Path absoluteWebapp = Paths.get(userDir, "src/main/webapp");
        if (Files.exists(absoluteWebapp) && Files.isDirectory(absoluteWebapp)) {
            return absoluteWebapp.toAbsolutePath().toString();
        }
        
        // 4. 상위 디렉토리에서 확인
        Path parentWebapp = Paths.get(userDir).getParent().resolve("smuxapi-war/src/main/webapp");
        if (Files.exists(parentWebapp) && Files.isDirectory(parentWebapp)) {
            return parentWebapp.toAbsolutePath().toString();
        }
        
        return null;
    }
}


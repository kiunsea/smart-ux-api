package com.smartuxapi.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Smart UX API Demo 애플리케이션 메인 클래스
 * 
 * @author KIUNSEA
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "com.smartuxapi.demo")
public class SmuxapiDemoApplication extends SpringBootServletInitializer {

    /**
     * WAR 배포를 위한 설정
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        // WAR 배포 타입 표시
        System.setProperty("deployment.type", "war");
        
        // WAR 배포 시 'war' 프로파일 활성화하여 context-path를 /로 설정
        return application.sources(SmuxapiDemoApplication.class)
                .profiles("war");
    }

    /**
     * 독립 실행형 JAR 실행을 위한 main 메서드
     */
    public static void main(String[] args) {
        // JAR 배포 타입 표시
        System.setProperty("deployment.type", "jar");
        
        // smuxapi-demo.yml에서 SERVER_PORT 설정 확인 및 적용
        configurePortFromYaml();
        
        // Spring Boot 애플리케이션 시작
        SpringApplication.run(SmuxapiDemoApplication.class, args);
        
        // 브라우저 자동 실행 (독립 실행형 JAR에서만)
        BrowserLauncher.launchWhenReady();
    }
    
    /**
     * smuxapi-demo.yml 파일에서 SERVER_PORT 값을 읽어서 서버 포트를 설정합니다.
     */
    private static void configurePortFromYaml() {
        try {
            String jarDir = getJarDirectory();
            String yamlPath = jarDir + File.separator + "smuxapi-demo.yml";
            
            File yamlFile = new File(yamlPath);
            
            if (yamlFile.exists() && yamlFile.isFile()) {
                // YAML 파일 읽기
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
                                    int port = Integer.parseInt(portValue);
                                    // Spring Boot가 시스템 프로퍼티의 server.port를 자동으로 읽습니다
                                    System.setProperty("server.port", String.valueOf(port));
                                    System.out.println("✅ smuxapi-demo.yml에서 SERVER_PORT 설정을 읽었습니다: " + port);
                                } catch (NumberFormatException e) {
                                    System.err.println("⚠️ 경고: SERVER_PORT 값이 유효한 숫자가 아닙니다: " + portValue);
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
            // 오류 발생 시 기본 포트(8080) 사용
            System.err.println("⚠️ 경고: smuxapi-demo.yml 파일 읽기 중 오류 발생: " + e.getMessage());
        }
    }
    
    /**
     * JAR 파일이 실행되는 디렉터리 경로를 반환합니다.
     */
    private static String getJarDirectory() {
        try {
            // JAR 파일의 경로에서 디렉터리 추출
            URL location = SmuxapiDemoApplication.class.getProtectionDomain().getCodeSource().getLocation();
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

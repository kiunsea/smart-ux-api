package com.smartuxapi.demo.config;

import java.io.File;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.smartuxapi.util.PropertiesUtil;

import jakarta.annotation.PostConstruct;

/**
 * 애플리케이션 설정 클래스
 * 
 * @author KIUNSEA
 */
@Configuration
public class ApplicationConfig implements WebMvcConfigurer {
    
    @PostConstruct
    public void init() {
        // PropertiesUtil 경로 설정
        // 실행 디렉터리의 smuxapi-demo.yml 파일을 사용
        String jarDir = getJarDirectory();
        String yamlPath = jarDir + File.separator + "smuxapi-demo.yml";
        
        File yamlFile = new File(yamlPath);
        
        if (yamlFile.exists()) {
            // YAML 파일이 있으면 사용
            PropertiesUtil.USER_PROPERTIES_PATH = yamlPath;
        } else {
            // YAML 파일이 없으면 classpath의 리소스 사용 시도
            try {
                String resourcePath = getClass().getClassLoader().getResource("smuxapi-demo.yml").getPath();
                // Windows 경로 처리
                if (resourcePath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                    resourcePath = resourcePath.substring(1);
                }
                PropertiesUtil.USER_PROPERTIES_PATH = resourcePath;
            } catch (Exception e) {
                // 리소스를 찾을 수 없으면 기본 경로 사용
                PropertiesUtil.USER_PROPERTIES_PATH = "smuxapi-demo.yml";
            }
        }
    }
    
    /**
     * JAR 파일이 실행되는 디렉터리 경로를 반환합니다.
     */
    private String getJarDirectory() {
        try {
            // JAR 파일의 경로에서 디렉터리 추출
            java.net.URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            java.net.URI uri = location.toURI();
            File codeSourceFile = new File(uri);
            
            if (codeSourceFile.isFile() && codeSourceFile.getName().endsWith(".jar")) {
                // JAR 파일인 경우
                return codeSourceFile.getParent();
            } else {
                // 개발 환경인 경우 (IDE에서 실행) - build/classes 또는 target/classes
                // 프로젝트 루트 디렉터리 찾기
                File currentDir = codeSourceFile;
                while (currentDir != null && currentDir.exists()) {
                    File pomFile = new File(currentDir, "pom.xml");
                    File buildGradle = new File(currentDir, "build.gradle.kts");
                    if (pomFile.exists() || buildGradle.exists()) {
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
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 핸들러 추가
        registry.addResourceHandler("/**")
            .addResourceLocations("classpath:/static/");
    }
}

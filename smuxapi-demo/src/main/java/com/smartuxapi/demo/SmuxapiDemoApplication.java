package com.smartuxapi.demo;

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
        // WAR 배포 시 'war' 프로파일 활성화하여 context-path를 /로 설정
        return application.sources(SmuxapiDemoApplication.class)
                .profiles("war");
    }

    /**
     * 독립 실행형 JAR 실행을 위한 main 메서드
     */
    public static void main(String[] args) {
        // Spring Boot 애플리케이션 시작
        SpringApplication.run(SmuxapiDemoApplication.class, args);
        
        // 브라우저 자동 실행 (독립 실행형 JAR에서만)
        BrowserLauncher.launchWhenReady();
    }
}

package com.smartuxapi.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * Smart UX API Demo 애플리케이션 메인 클래스
 * 
 * @author KIUNSEA
 */
@SpringBootApplication
@ServletComponentScan(basePackages = "com.smartuxapi.demo")
public class SmuxapiDemoApplication {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션 시작
        SpringApplication.run(SmuxapiDemoApplication.class, args);
        
        // 브라우저 자동 실행
        BrowserLauncher.launchWhenReady();
    }
}

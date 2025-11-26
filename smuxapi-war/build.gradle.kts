plugins {
    `war`
    java
}

group = "com.smartuxapi"
version = "0.5.2"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // smart-ux-api lib 모듈 의존성
    implementation(project(":lib"))
    
    // Jakarta Servlet API
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    
    // Embedded Tomcat (톰캣 서버 없이 실행하기 위해)
    implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.20")
    implementation("org.apache.tomcat.embed:tomcat-embed-jasper:10.1.20")
    implementation("org.apache.tomcat.embed:tomcat-embed-websocket:10.1.20")
    
    // Jackson (JSON 처리)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    
    // JSON Simple
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.json:json:20250517")
    
    // Log4j2
    implementation("org.apache.logging.log4j:log4j-api:2.21.0")
    implementation("org.apache.logging.log4j:log4j-core:2.21.0")
    
    // 테스트
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.war {
    archiveBaseName.set("smuxapi-war")
    archiveVersion.set(project.version.toString())
    
    // WEB-INF/lib의 JAR 파일들을 제외 (Gradle 의존성으로 대체)
    exclude("WEB-INF/lib/*.jar")
    
    // webapp 디렉토리 설정
    webAppDirectory.set(file("src/main/webapp"))
}

tasks.test {
    useJUnitPlatform()
}

// 실행 태스크 (Embedded Tomcat으로 실행)
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Runs the application with embedded Tomcat server"
    
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.smartuxapi.sample.EmbeddedTomcatServer")
    
    // 기본 포트 및 컨텍스트 경로 설정
    args = listOf("--port=8080", "--context-path=/")
    
    // 표준 입력/출력 연결
    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err
}


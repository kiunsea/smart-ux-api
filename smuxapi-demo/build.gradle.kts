plugins {
    java
    war
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.smartuxapi"
version = "0.6.0"

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
    
    // Jakarta Servlet API (컴파일 시에만 필요, 런타임에는 Spring Boot가 제공)
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    
    // Spring Boot Web Starter
    implementation("org.springframework.boot:spring-boot-starter-web") {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
    
    // WAR 배포를 위한 Tomcat 제공 (외부 Tomcat에서 실행 시 필요)
    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
    
    // Log4j2
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
    implementation("org.apache.logging.log4j:log4j-api:2.21.0")
    implementation("org.apache.logging.log4j:log4j-core:2.21.0")
    
    // Jackson (JSON 처리)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.3")
    
    // JSON Simple
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.json:json:20250517")
    
    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

// webapp 리소스를 resources/static으로 복사
tasks.register<Copy>("copyWebapp") {
    from("src/main/webapp") {
        exclude("WEB-INF/**")
        exclude("META-INF/**")
    }
    into("src/main/resources/static")
    includeEmptyDirs = false
}

// 빌드 전에 webapp 리소스 복사
tasks.processResources {
    dependsOn("copyWebapp")
}

// Spring Boot JAR 설정: plain JAR 생성 안 함
tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
    archiveBaseName.set("smuxapi-demo")
    archiveVersion.set(project.version.toString())
    mainClass.set("com.smartuxapi.demo.SmuxapiDemoApplication")
}

// WAR 파일 설정
tasks.war {
    enabled = true
    // WAR 파일 이름을 smuxapi.war로 설정하여 context path를 /smuxapi로 고정
    archiveBaseName.set("smuxapi")
    archiveVersion.set("")  // 버전 번호 제거하여 smuxapi.war로 생성
    
    // WAR 파일 출력 경로를 packaging/distribution으로 설정
    destinationDirectory.set(file("packaging/distribution"))
    
    // 중복 파일 처리 전략 설정
    // Spring Boot가 의존성을 자동으로 포함하므로, src/main/webapp/WEB-INF/lib/의 JAR와 중복 시 제외
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    // src/main/webapp의 내용을 포함하되, WEB-INF/lib/의 JAR 파일들은 제외
    // Spring Boot WAR 플러그인이 자동으로 의존성을 포함하므로 수동 JAR는 불필요
    from("src/main/webapp") {
        exclude("WEB-INF/lib/*.jar")
        exclude("WEB-INF/lib/*-sources.jar")
    }
    
    doLast {
        val warFile = archiveFile.get().asFile
        println("✅ WAR 파일 생성 완료!")
        println("📦 파일 위치: ${warFile.absolutePath}")
        println("📊 파일 크기: ${String.format("%.2f", warFile.length() / 1024.0 / 1024.0)} MB")
        println("")
        println("WAR 배포 방법:")
        println("  1. WAR 파일을 Tomcat의 webapps 디렉터리에 복사")
        println("  2. Tomcat 서버 시작")
        println("  3. 브라우저에서 http://localhost:8080/smuxapi 접속")
        println("")
        println("⚠️  참고:")
        println("  - WAR 파일 이름: smuxapi.war")
        println("  - Tomcat context path: /smuxapi (META-INF/context.xml 설정)")
        println("  - Spring Boot context-path: / (application-war.yml 설정)")
        println("  - 최종 접속 URL: http://localhost:8080/smuxapi")
    }
}

tasks.test {
    useJUnitPlatform()
}

// Custom JRE 생성 태스크 (jlink 사용)
tasks.register("createJre") {
    val jreDir = layout.buildDirectory.dir("jre").get().asFile
    
    outputs.dir(jreDir)
    
    doFirst {
        // 기존 JRE 강제 삭제
        if (jreDir.exists()) {
            println("기존 JRE 디렉토리 삭제 중...")
            jreDir.deleteRecursively()
        }
    }
    
    doLast {
        // jlink로 custom JRE 생성
        exec {
            commandLine(
                "jlink",
                "--add-modules", "java.base,java.compiler,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,java.xml.crypto,jdk.crypto.ec,jdk.httpserver,jdk.jdwp.agent,jdk.jfr,jdk.management,jdk.management.agent,jdk.naming.dns,jdk.net,jdk.security.auth,jdk.unsupported,jdk.zipfs",
                "--strip-debug",
                "--no-man-pages",
                "--no-header-files",
                "--compress=2",
                "--output", jreDir.absolutePath
            )
        }
        println("✓ Custom JRE 생성 완료: ${jreDir.absolutePath}")
        println("  포함된 모듈: Spring Boot 및 Tomcat 구동에 필요한 모든 모듈")
    }
}

// 배포 패키지 생성 태스크: JAR + 배치 스크립트 + Custom JRE
tasks.register<Zip>("packageDist") {
    dependsOn(tasks.bootJar, tasks.named("createJre"))
    
    archiveFileName.set("smuxapi-demo.zip")
    destinationDirectory.set(file("packaging/distribution"))
    
    from(tasks.bootJar) {
        into("/")
    }
    
    from("packaging/distribution") {
        include("smuxapi-demo.bat", "README.md")
        into("/")
    }
    
    // 설정 파일 포함 (YAML 형식)
    from("src/main/resources") {
        include("smuxapi-demo.yml")
        into("/")
    }
    
    from(layout.buildDirectory.dir("jre")) {
        into("jre")
    }
    
    doLast {
        println("✅ 배포 패키지 생성 완료!")
        println("📦 파일 위치: ${archiveFile.get().asFile}")
        println("")
        println("📌 이 패키지는 Java 설치 없이 실행 가능합니다.")
        println("")
        println("배포 방법:")
        println("1. ZIP 파일을 원하는 위치에 압축 해제")
        println("2. smuxapi-demo.bat 실행")
    }
}

// 배포 태스크: 빌드, 배포 패키지 생성 (JAR + WAR)
tasks.register("deploy") {
    group = "distribution"
    description = "빌드하고 배포 패키지를 생성합니다 (JAR + WAR)."
    
    dependsOn("packageDist", "war")
    
    doFirst {
        println("=".repeat(60))
        println("🚀 배포 프로세스 시작")
        println("=".repeat(60))
    }
    
    doLast {
        val packageDistTask = tasks.named<Zip>("packageDist").get()
        val distFile = packageDistTask.archiveFile.get().asFile
        val warTask = tasks.named<War>("war").get()
        val warFile = warTask.archiveFile.get().asFile
        
        println("")
        println("=".repeat(60))
        println("✅ 배포 완료!")
        println("=".repeat(60))
        println("")
        println("📦 독립 실행 패키지 (ZIP):")
        println("   위치: ${distFile.absolutePath}")
        println("   크기: ${String.format("%.2f", distFile.length() / 1024.0 / 1024.0)} MB")
        println("")
        println("📦 WAR 배포 파일:")
        println("   위치: ${warFile.absolutePath}")
        println("   크기: ${String.format("%.2f", warFile.length() / 1024.0 / 1024.0)} MB")
        println("")
        println("배포 파일 구성:")
        println("  ZIP 패키지:")
        println("    - smuxapi-demo-${project.version}.jar (Spring Boot 실행 JAR)")
        println("    - smuxapi-demo.bat (실행 스크립트)")
        println("    - README.md (사용 가이드)")
        println("    - jre/ (번들된 Java Runtime Environment)")
        println("")
        println("  WAR 파일:")
        println("    - smuxapi.war (Tomcat 배포용)")
        println("")
        println("사용 방법:")
        println("  독립 실행 (ZIP):")
        println("    1. ZIP 파일을 원하는 위치에 압축 해제")
        println("    2. smuxapi-demo.bat 실행")
        println("    3. 브라우저에서 http://localhost:8080/smuxapi/ 접속")
        println("")
        println("  WAR 배포 (Tomcat):")
        println("    1. WAR 파일을 Tomcat의 webapps 디렉터리에 복사")
        println("    2. Tomcat 서버 시작")
        println("    3. 브라우저에서 http://localhost:8080/smuxapi 접속")
        println("=".repeat(60))
    }
}

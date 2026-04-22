/*
 * Smart UX API 루트 빌드 — 공통 설정을 모든 서브 프로젝트에 적용한다.
 *
 * 서브 프로젝트별 세부 설정은 각자의 build.gradle.kts 에서 관리한다:
 *   lib/build.gradle.kts          — java-library, jacoco, publishing
 *   smuxapi-demo/build.gradle.kts — war, Spring Boot, project(":lib") 의존
 */

allprojects {
    repositories {
        mavenCentral()
    }
}

// 루트 자체는 JAR을 생성하지 않음
tasks.register("printProjects") {
    group = "help"
    description = "참여 서브 프로젝트 목록을 출력한다."
    doLast {
        println("Root: ${rootProject.name}")
        subprojects.forEach { println("  └─ :${it.name}  (version=${it.version})") }
    }
}

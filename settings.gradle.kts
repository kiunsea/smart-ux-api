/*
 * Smart UX API 모노레포 — multi-project Gradle build.
 *
 *   lib          : AI 기반 UI 자동화 라이브러리 (java-library)
 *   smuxapi-demo : lib를 참조하는 Spring Boot WAR 데모 애플리케이션
 *
 * smuxapi-demo의 build.gradle.kts는 `implementation(project(":lib"))` 로 lib에 의존한다.
 */

plugins {
    // JDK 자동 다운로드 (foojay resolver)
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "smart-ux-api"

include("lib")
include("smuxapi-demo")

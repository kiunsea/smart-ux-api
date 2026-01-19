/*
 * smuxapi-demo 프로젝트를 Eclipse에서 독립적으로 열 때 사용하는 settings.gradle.kts
 * 멀티 프로젝트 구조를 지원하기 위해 lib 프로젝트를 포함합니다.
 */

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "smuxapi-demo"

// lib 프로젝트 포함 (상대 경로로 설정)
include("lib")
project(":lib").projectDir = file("../smart-ux-api/lib")

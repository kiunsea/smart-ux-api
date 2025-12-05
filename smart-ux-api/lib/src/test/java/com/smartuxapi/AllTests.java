package com.smartuxapi;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Smart UX API 통합 테스트 스위트
 * 
 * 모든 테스트 케이스를 통합 실행하고 결과를 확인할 수 있습니다.
 * 
 * 실행 방법:
 * 1. IDE에서 이 클래스를 실행
 * 2. Gradle: ./gradlew test
 * 3. Gradle (상세): ./gradlew test --info
 * 
 * 테스트 결과 확인:
 * - HTML 리포트: build/reports/tests/test/index.html
 * - XML 리포트: build/test-results/test/TEST-*.xml
 */
@Suite
@SuiteDisplayName("Smart UX API 통합 테스트 스위트")
@SelectPackages({
    "com.smartuxapi",
    "com.smartuxapi.ai",
    "com.smartuxapi.ai.openai",
    "com.smartuxapi.ai.gemini",
    "com.smartuxapi.util"
})
public class AllTests {
    // 이 클래스는 테스트 스위트를 정의하는 마커 클래스입니다.
    // 실제 테스트 메서드는 포함하지 않습니다.
}


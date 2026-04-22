package com.smartuxapi;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StrSubstitutor 기본 테스트")
public class StrSubstitutorBasicTest {
	
	@Test
	@DisplayName("기본 문자열 치환 테스트")
	public void testBasicSubstitution() {
		// 1. 변수와 값 매핑을 위한 Map 생성
		Map<String, String> valueMap = new HashMap<>();
		valueMap.put("name", "Alice");
		valueMap.put("product", "Laptop");
		valueMap.put("price", "1200");

		// 2. 템플릿 문자열 정의 (플레이스홀더는 기본적으로 ${...} 형태)
		String templateString = "안녕하세요, ${name}님! 당신은 ${product}를 ${price}원에 구매하셨습니다.";

		// 3. StrSubstitutor 인스턴스 생성
		StrSubstitutor sub = new StrSubstitutor(valueMap);

		// 4. 문자열 치환 수행
		String result = sub.replace(templateString);

		// 검증
		assertNotNull(result);
		assertTrue(result.contains("Alice"));
		assertTrue(result.contains("Laptop"));
		assertTrue(result.contains("1200"));
		assertEquals("안녕하세요, Alice님! 당신은 Laptop를 1200원에 구매하셨습니다.", result);
	}

	@Test
	@DisplayName("없는 변수 처리 테스트")
	public void testMissingVariable() {
		Map<String, String> valueMap = new HashMap<>();
		valueMap.put("name", "Alice");
		
		StrSubstitutor sub = new StrSubstitutor(valueMap);
		
		// 플레이스홀더가 없는 경우
		String templateWithMissing = "사용자 ID: ${userId}, 지역: ${region}";
		String resultWithMissing = sub.replace(templateWithMissing);
		
		// 없는 변수는 치환되지 않고 그대로 남아야 함
		assertNotNull(resultWithMissing);
		assertTrue(resultWithMissing.contains("${userId}"));
		assertTrue(resultWithMissing.contains("${region}"));
	}
}

package com.smartuxapi;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.core.lookup.StrSubstitutor;

public class StrSubstitutorBasicTest {
	public static void main(String[] args) {
		// 1. 변수와 값 매핑을 위한 Map 생성
		Map<String, String> valueMap = new HashMap<>();
		valueMap.put("name", "Alice");
		valueMap.put("product", "Laptop");
		valueMap.put("price", "1200");

		// 2. 템플릿 문자열 정의 (플레이스홀더는 기본적으로 ${...} 형태)
		String templateString = "안녕하세요, ${name}님! 당신은 ${product}를 ${price}원에 구매하셨습니다.";

		// 3. StrSubstitutor 인스턴스 생성
		// MapStrLookup.mapLookup(valueMap)은 Map을 기반으로 값을 찾는 룩업입니다.
		StrSubstitutor sub = new StrSubstitutor(valueMap);
		// 또는 간단하게 StrSubstitutor sub = new StrSubstitutor(valueMap);

		// 4. 문자열 치환 수행
		String result = sub.replace(templateString);

		System.out.println("원본 템플릿: " + templateString);
		System.out.println("치환 결과: " + result);
		// 출력: 치환 결과: 안녕하세요, Alice님! 당신은 Laptop를 1200원에 구매하셨습니다.

		// 플레이스홀더가 없는 경우
		System.out.println("\n--- 없는 변수 및 기본값 ---");
		String templateWithMissing = "사용자 ID: ${userId}, 지역: ${region}";
		String resultWithMissing = sub.replace(templateWithMissing);
		System.out.println("치환 결과 (없는 변수): " + resultWithMissing);
		// 출력: 치환 결과 (없는 변수): 사용자 ID: ${userId}, 지역: ${region}
		// 기본적으로 없는 변수는 치환되지 않고 그대로 남습니다.
	}
}

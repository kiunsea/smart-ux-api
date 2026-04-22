package com.smartuxapi.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtil 테스트")
public class StringUtilTest {
	
	@Test
	@DisplayName("isNumber 테스트 - 숫자 문자열 검증")
	public void testIsNumber() {
		assertTrue(StringUtil.isNumber("123"));
		assertTrue(StringUtil.isNumber("123.45"));
		assertTrue(StringUtil.isNumber("0"));
		assertFalse(StringUtil.isNumber("abc"));
		assertFalse(StringUtil.isNumber("12abc"));
		assertFalse(StringUtil.isNumber(null));
	}
	
	@Test
	@DisplayName("getFirst 테스트 - 첫 번째 단어 추출")
	public void testGetFirst() {
		assertEquals("Hello", StringUtil.getFirst("Hello World"));
		assertEquals("test", StringUtil.getFirst("test"));
		assertEquals("first", StringUtil.getFirst("first second third"));
	}
	
	@Test
	@DisplayName("removeAllWhiteSpace 테스트 - 모든 공백 제거")
	public void testRemoveAllWhiteSpace() {
		assertEquals("HelloWorld", StringUtil.removeAllWhiteSpace("Hello World"));
		assertEquals("test", StringUtil.removeAllWhiteSpace("test"));
		assertEquals("abc", StringUtil.removeAllWhiteSpace("a b c"));
	}
	
	@Test
	@DisplayName("replaceEscapeSequence 테스트 - 이스케이프 시퀀스 변환")
	public void testReplaceEscapeSequence() {
		assertEquals("test\\nline", StringUtil.replaceEscapeSequence("test\nline"));
		assertEquals("test\\ttab", StringUtil.replaceEscapeSequence("test\ttab"));
		assertEquals("test\\rreturn", StringUtil.replaceEscapeSequence("test\rreturn"));
		assertNull(StringUtil.replaceEscapeSequence(null));
	}
	
	@Test
	@DisplayName("replaceAll 테스트 - 문자열 치환")
	public void testReplaceAll() {
		String result = StringUtil.replaceAll("Hello World", "World", "Java", false);
		assertEquals("Hello Java", result);
		
		String result2 = StringUtil.replaceAll("test$value", "\\$", "dollar", false);
		assertTrue(result2.contains("dollar"));
		
		assertNull(StringUtil.replaceAll(null, "test", "replace", false));
	}
}


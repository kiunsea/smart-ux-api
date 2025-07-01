package com.smartuxapi.util;

import java.util.StringTokenizer;


/**
 * @author KIUNSEA
 *
 */
public class StringUtil {

    /**
     * String 클래스의 replaceAll 개량 함수<br/>
     * replacement 인자에 역슬래쉬(\)와 달러($)문자를 인식할 수 있도록 한다.<br/>
     * regex 인자에 달러($)를 입력하려는 경우엔 "\\$"이다.<br/>
     * 주의) 단일 특수문자 입력은 불가 -> (ex> "\")
     * @param sourceDoc 입력문서
     * @param regex 대상이 될 문자열 정규표현식
     * @param replacement 대체할 문자열
     * @param useDoubleQuotation '"' 문자도 고려할지 여부
     * @return 결과물
     */
	public static String replaceAll(String sourceDoc, String regex, String replacement, boolean useDoubleQuotation) {

		if (sourceDoc == null) {
			return sourceDoc;
		}
		
		StringBuffer newReplacement = new StringBuffer();
		char[] replaceChar = replacement.toCharArray();
		for (int i = 0; i < replaceChar.length; i++) {
			
			if (replaceChar[i] == '\\' || replaceChar[i] == '$') {
				newReplacement.append('\\');
			} else if(replaceChar[i] == '"' && useDoubleQuotation) {
				newReplacement.append('\\');
				newReplacement.append('\\');
			}
			
			newReplacement.append(replaceChar[i]);
			
		}

		String rtnValue = sourceDoc.replaceAll(regex, newReplacement.toString());
		return rtnValue;
	}
	
	/**
	 * source 내의 모든 escape sequence를 문자열로 변환한다.
	 * @param source
	 * @return 결과물
	 */
	public static String replaceEscapeSequence(String source) {
		
		if (source == null) {
			return source;
		}
		
		StringBuffer newSource = new StringBuffer();
		char[] sourceChar = source.toCharArray();
		int length = sourceChar.length;
		for (int i = 0; i < length; i++) {
			
			if (sourceChar[i] == '\r') { //Carridge Return
				newSource.append('\\');
				newSource.append('r');
				continue;
			} else if (sourceChar[i] == '\n') { //New Line
				newSource.append('\\');
				newSource.append('n');
				continue;
			} else if (sourceChar[i] == '\t') { //Tab
				newSource.append('\\');
				newSource.append('t');
				continue;
			} else if (sourceChar[i] == '\b') { //Back Space
				newSource.append('\\');
				newSource.append('b');
				continue;
			} else if (sourceChar[i] == '\f') { //Form Feed
				newSource.append('\\');
				newSource.append('f');
				continue;
			} else if (sourceChar[i] == '\\') { //Back Slash
				newSource.append('\\');
				newSource.append('\\');
					continue;
			} else if (sourceChar[i] == '\"') { //Double Quotation
				newSource.append('\\');
				newSource.append('\"');
					continue;
			}
			
			newSource.append(sourceChar[i]);
		}

		return newSource.toString();
	}
	
	/**
	 * 수치형 문자열인지 검사
	 * @param num
	 * @return 수치형 문자열이면 true
	 */
	public static boolean isNumber(String num) {
		return num == null ? false : num.matches("[0-9.]+");
	}
	
	/**
	 * 공백이 아닌 첫번째 단어를 추출한다.
	 * @param str
	 * @return 추출된 단어
	 */
	public static String getFirst(String str) {
		
		String[] strArr = str.trim().split("\\p{Space}");
		
		return strArr[0];
	}
	
	/**
	 * 모든 공백 문자를 제거한다
	 * @param str
	 * @return
	 */
	public static String removeAllWhiteSpace(String str) {
		
		StringTokenizer st = new StringTokenizer(str.trim());
		StringBuilder sb = new StringBuilder();
		while (st.hasMoreTokens()) {
			sb.append(st.nextToken());
		}

		return sb.toString();
	}
	
	private StringUtil() {
		;
	}
}
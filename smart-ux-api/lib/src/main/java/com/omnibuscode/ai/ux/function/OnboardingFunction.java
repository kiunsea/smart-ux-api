package com.omnibuscode.ai.ux.function;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.omnibuscode.ai.UserFunction;

/**
 * Assistant 에 User Onboarding Function 을 등록한다.<br/>
 * 이 클래스를 상속받은 클래스에서는 getOnboardingJson()과 execFunction() 함수를 구현하여<br/>
 * getOnboardingJson()으로는 사용자 온보딩 정의가 Assistant 에 등록하게 하고<br/>
 * execFunction()로는 AI가 함수 호출시 가이드를 위한 준비 작업으로 onboarding 내용을 작성하게 해야 한다.
 */
public abstract class OnboardingFunction implements UserFunction {

	public static String FILE_NAME = "onboarding.txt"; //assist 에 등록하는 file 이름
	
	@Override
	public JSONObject getFunctionJson() {
		
		JSONObject onboardingJson = this.getOnboardingJson();
		if (onboardingJson != null) {
			//TODO assistant/tools/file search 에 AI 가 function call 시 args 를 작성 할 수 있도록 참조 문서를 생성
			//onboarding.txt file을 조회하여 이미 등록된 file이 있다면 file 을 가져다가 새로 추가해야 한다.
		}
		
		//TODO 파일 생성후 assistant 에 function 등록을 위해 json 내용을 만들어 반환해야 한다.
		//반환된 json 내용은 Assistant 클래스에서 api 로 반영하게 된다.
		
		return null;
	}
	
	/**
	 * Onboarding 내용을 json object 로 반환한다.
	 * @return 온보딩을 위한 Actions Processing
	 */
	public abstract JSONObject getOnboardingJson();

}

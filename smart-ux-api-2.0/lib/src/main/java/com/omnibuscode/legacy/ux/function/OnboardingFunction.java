package com.omnibuscode.legacy.ux.function;

import org.json.simple.JSONObject;

import com.omnibuscode.legacy.ProcessFunction;

/**
 * ProcessFunction 의 기능과 더불어 ai로부터 전달받은 user process scenario 를 action 목록으로 세션에 저장하고 온보딩 실행을 위한 driver.js 실행 파일도 생성한다.<br/>
 * 이 클래스를 상속받은 클래스에서는 getOnboardingJson()과 execFunction() 함수를 구현하여<br/>
 * getOnboardingJson()으로는 user onboarding definition 이 Assistant 의 file search 에 등록하게 하고<br/>
 * execFunction()로는 AI가 함수 호출시 가이드를 위한 준비 작업을 해야 한다.
 */
public abstract class OnboardingFunction implements ProcessFunction {

	public static String FILE_NAME = "onboarding.json"; //assist 의 file search 에 등록하는 file 이름
	
	@Override
	public JSONObject getFunctionJson() {
		
		JSONObject onboardingJson = this.getOnboardingJson();
		if (onboardingJson != null) {
			//TODO assistant/tools/file search 에 AI 가 function call 시 args 를 작성 할 수 있도록 참조 문서를 생성
			//onboarding.json file을 조회하여 이미 등록된 file이 있다면 file 을 가져다가 내용을 확인하고 반영해야 한다.
		}
		
		//TODO 파일 생성후 assistant 에 function 등록을 위해 json 내용을 만들어 반환해야 한다.
		//반환된 json 내용은 Assistant 클래스에서 api 로 반영하게 된다.
		//function 의 args 는 navigate 와 onboard 동작들이 배열로 만들어지도록 정의해야 한다.
		//onboarding.json에 기록되는 action 은 driver.js 를 위해 navigate 와 message 만 가능하다.
		
		return null;
	}
	
	/**
	 * process 내용을 json object 로 반환한다.
	 * @return Actions Processing
	 */
	public abstract JSONObject getOnboardingJson();

}

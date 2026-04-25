package com.smartuxapi.scenario;

import com.smartuxapi.ai.ChatRoom;

/**
 * 시나리오 재현 시 ChatRoom 을 만들어 반환하는 함수형 인터페이스.
 *
 * <p>호출자가 OpenAI / Gemini / Mock 등 어떤 ChatRoom 을 사용할지 결정. 각 시나리오마다 새 ChatRoom 을
 * 만들어 격리하는 것이 권장 (대화 히스토리 오염 방지).
 *
 * @since lib 0.9.5
 */
@FunctionalInterface
public interface ChatRoomFactory {
    /**
     * @param scenario 재현할 시나리오 (aiModel 필드를 보고 분기 가능)
     * @return 새로 생성된 ChatRoom (caller 가 close() 호출 책임)
     */
    ChatRoom create(ScenarioData scenario) throws Exception;
}

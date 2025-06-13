package com.omnibuscode.ai.openai.connection;

/**
 * Chat Completions API에 연결한다.
 * ref : https://platform.openai.com/docs/api-reference/chat
 * 2025.06.13 Chat Completions API 는 현재 OpenAI의 API 서버가 대화 이력을 자동으로 관리해주지 않는다.
 *            대화 이력을 기억하면서 API를 계속 사용하려면 연결하는 서버에서 messages 배열의 대화 목록을 계속해서 누적하며 API 서버에 전송해줘야 한다.
 *            따라서 현재 클래스는 당분간 구현 보류로 남긴다.
 */
public class ChatCompletionsConnection {

}

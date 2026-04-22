package com.smartuxapi.ai.embedding;

/**
 * Embedding API 호출 실패를 나타내는 checked exception.
 *
 * <p>Tool Use 의 handler 에서 catch 하여 {@code ToolResult.error} 로 변환하는 용도로도 사용 가능.
 *
 * @since 0.9.0
 */
public class EmbeddingException extends Exception {

    private static final long serialVersionUID = 1L;

    public EmbeddingException(String message) {
        super(message);
    }

    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }
}

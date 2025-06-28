package io.throttle.core.exception;

/**
 * Throttle 관련 기본 예외 클래스
 */
public abstract class ThrottleException extends RuntimeException {
    public ThrottleException(String message) {
        super(message);
    }

    public ThrottleException(String message, Throwable cause) {
        super(message, cause);
    }
}
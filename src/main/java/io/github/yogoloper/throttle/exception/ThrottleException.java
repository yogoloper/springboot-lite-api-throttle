package io.github.yogoloper.throttle.exception;

public class ThrottleException extends RuntimeException {
    public ThrottleException(String message) {
        super(message);
    }

    public ThrottleException(String message, Throwable cause) {
        super(message, cause);
    }
}
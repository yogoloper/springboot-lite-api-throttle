package io.github.yogoloper.throttle.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.github.yogoloper.throttle.model.ErrorResponse;

@RestControllerAdvice
public class ThrottleExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse.RateLimitResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse.RateLimitResponse.builder()
                .error("Too Many Requests")
                .type("rate_limit")
                .limit(ex.getLimit())
                .remaining(ex.getRemaining())
                .retryAfterSeconds(ex.getRetryAfterSeconds())
                .timestamp(ex.getTimestamp())
                .build());
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ErrorResponse.QuotaResponse> handleQuotaExceeded(QuotaExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(ErrorResponse.QuotaResponse.builder()
                .error("Quota Exceeded")
                .type("quota_limit")
                .limit(ex.getLimit())
                .remaining(ex.getRemaining())
                .period(ex.getPeriod())
                .resetTime(ex.getResetTime())
                .build());
    }
}
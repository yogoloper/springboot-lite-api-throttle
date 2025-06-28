package io.throttle.spring.starter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.throttle.core.model.ErrorResponse;
import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.exception.RateLimitExceededException;

@RestControllerAdvice
public class ThrottleExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<io.throttle.core.model.ErrorResponse.RateLimitResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new io.throttle.core.model.ErrorResponse.RateLimitResponse(
                "Rate limit exceeded",
                "RATE_LIMIT",
                ex.getRateLimit().getLimit(),
                ex.getRemaining(),
                ex.getRetryAfterSeconds(),
                java.time.Instant.now()
            ));
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<io.throttle.core.model.ErrorResponse.QuotaResponse> handleQuotaExceeded(QuotaExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new io.throttle.core.model.ErrorResponse.QuotaResponse(
                "Quota exceeded",
                "QUOTA",
                ex.getLimit(),
                ex.getRemaining(),
                ex.getPeriod(),
                ex.getResetTime()
            ));
    }
}
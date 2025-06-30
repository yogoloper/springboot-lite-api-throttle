package io.throttle.spring.starter;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.throttle.core.model.ErrorResponse;
import io.throttle.core.exception.QuotaExceededException;
import io.throttle.core.exception.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
public class ThrottleExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ThrottleExceptionHandler.class);

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<io.throttle.core.model.ErrorResponse.RateLimitResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        log.info("[ThrottleExceptionHandler] handleRateLimitExceeded called");
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        
        // Retry-After 헤더 추가
        responseBuilder.header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        
        // Rate Limit 헤더 추가
        responseBuilder.header("X-RateLimit-Limit", String.valueOf(ex.getRateLimit().getLimit()));
        responseBuilder.header("X-RateLimit-Remaining", String.valueOf(ex.getRemaining()));
        responseBuilder.header("X-RateLimit-Reset", String.valueOf(Instant.now().plusSeconds(ex.getRetryAfterSeconds()).getEpochSecond()));
        
        return responseBuilder.body(new io.throttle.core.model.ErrorResponse.RateLimitResponse(
            "Too Many Requests",
            "rate_limit",
            ex.getRateLimit().getLimit(),
            ex.getRemaining(),
            ex.getRetryAfterSeconds(),
            Instant.now()
        ));
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<io.throttle.core.model.ErrorResponse.QuotaResponse> handleQuotaExceeded(QuotaExceededException ex) {
        log.info("[ThrottleExceptionHandler] handleQuotaExceeded called");
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS);
        
        // Quota 관련 헤더 추가
        String period = ex.getPeriod() != null ? ex.getPeriod().toLowerCase() : null;
        if ("daily".equals(period)) {
            responseBuilder.header("X-Quota-Daily-Limit", String.valueOf(ex.getLimit()));
            responseBuilder.header("X-Quota-Daily-Remaining", String.valueOf(ex.getRemaining()));
            responseBuilder.header("X-Quota-Daily-Reset", String.valueOf(ex.getResetTime().getEpochSecond()));
        } else if ("monthly".equals(period)) {
            responseBuilder.header("X-Quota-Monthly-Limit", String.valueOf(ex.getLimit()));
            responseBuilder.header("X-Quota-Monthly-Remaining", String.valueOf(ex.getRemaining()));
            responseBuilder.header("X-Quota-Monthly-Reset", String.valueOf(ex.getResetTime().getEpochSecond()));
        }
        
        // Retry-After 헤더 추가 (다음 리셋까지의 시간)
        long retryAfterSeconds = ex.getResetTime().getEpochSecond() - Instant.now().getEpochSecond();
        if (retryAfterSeconds > 0) {
            responseBuilder.header("Retry-After", String.valueOf(retryAfterSeconds));
        }
        
        return responseBuilder.body(new io.throttle.core.model.ErrorResponse.QuotaResponse(
            "Quota Exceeded",
            "quota_limit",
            ex.getLimit(),
            ex.getRemaining(),
            period,
            ex.getResetTime()
        ));
    }
}
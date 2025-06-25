package io.github.yogoloper.throttle.model;

import java.time.Instant;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * API 쓰로틀링 에러 응답 모델
 */
@Getter
@SuperBuilder
public class ErrorResponse {
    private final String error;
    private final String type;

    /**
     * RateLimit 초과시 응답
     */
    @Getter
    @SuperBuilder
    public static class RateLimitResponse extends ErrorResponse {
        private final int limit;
        private final int remaining;
        private final long retryAfterSeconds;
        private final Instant timestamp;
    }

    /**
     * Quota 초과시 응답
     */
    @Getter
    @SuperBuilder
    public static class QuotaResponse extends ErrorResponse {
        private final int limit;
        private final int remaining;
        private final String period;
        private final Instant resetTime;
    }
}
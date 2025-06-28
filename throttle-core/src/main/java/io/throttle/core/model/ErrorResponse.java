package io.throttle.core.model;

import java.time.Instant;

/**
 * API 쓰로틀링 에러 응답 모델
 */
public class ErrorResponse {
    private final String error;
    private final String type;

    public ErrorResponse(String error, String type) {
        this.error = error;
        this.type = type;
    }

    public String getError() {
        return error;
    }

    public String getType() {
        return type;
    }

    /**
     * RateLimit 초과시 응답
     */
    public static class RateLimitResponse extends ErrorResponse {
        private final int limit;
        private final int remaining;
        private final long retryAfterSeconds;
        private final Instant timestamp;

        public RateLimitResponse(String error, String type, int limit, int remaining, long retryAfterSeconds, Instant timestamp) {
            super(error, type);
            this.limit = limit;
            this.remaining = remaining;
            this.retryAfterSeconds = retryAfterSeconds;
            this.timestamp = timestamp;
        }

        public int getLimit() {
            return limit;
        }

        public int getRemaining() {
            return remaining;
        }

        public long getRetryAfterSeconds() {
            return retryAfterSeconds;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Quota 초과시 응답
     */
    public static class QuotaResponse extends ErrorResponse {
        private final int limit;
        private final int remaining;
        private final String period;
        private final Instant resetTime;

        public QuotaResponse(String error, String type, int limit, int remaining, String period, Instant resetTime) {
            super(error, type);
            this.limit = limit;
            this.remaining = remaining;
            this.period = period;
            this.resetTime = resetTime;
        }

        public int getLimit() {
            return limit;
        }

        public int getRemaining() {
            return remaining;
        }

        public String getPeriod() {
            return period;
        }

        public Instant getResetTime() {
            return resetTime;
        }
    }
}
package io.throttle.core.exception;

import io.throttle.core.model.RateLimit;

/**
 * Rate Limit 초과 시 발생하는 예외
 */
public class RateLimitExceededException extends ThrottleException {
    private final RateLimit rateLimit;
    private final int remaining;
    private final long retryAfterSeconds;

    public RateLimitExceededException(String message, RateLimit rateLimit, int remaining, long retryAfterSeconds) {
        super(message);
        this.rateLimit = rateLimit;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
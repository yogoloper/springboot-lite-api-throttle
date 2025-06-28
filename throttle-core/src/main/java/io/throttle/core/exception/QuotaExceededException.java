package io.throttle.core.exception;

import io.throttle.core.model.Quota;

/**
 * Quota 초과 시 발생하는 예외
 */
public class QuotaExceededException extends ThrottleException {
    private final String period;  // "daily", "monthly"
    private final int limit;
    private final int remaining;
    private final java.time.Instant resetTime;

    public QuotaExceededException(String message, String period, int limit, int remaining, java.time.Instant resetTime) {
        super(message);
        this.period = period;
        this.limit = limit;
        this.remaining = remaining;
        this.resetTime = resetTime;
    }

    public String getPeriod() {
        return period;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public java.time.Instant getResetTime() {
        return resetTime;
    }
}
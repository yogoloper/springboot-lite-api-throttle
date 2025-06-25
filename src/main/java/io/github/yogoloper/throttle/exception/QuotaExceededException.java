package io.github.yogoloper.throttle.exception;

import java.time.Instant;

public class QuotaExceededException extends ThrottleException {
    private final String period;  // "daily", "monthly"
    private final int limit;
    private final int remaining;
    private final Instant resetTime;

    public QuotaExceededException(String message, String period, int limit, int remaining, Instant resetTime) {
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

    public Instant getResetTime() {
        return resetTime;
    }
}
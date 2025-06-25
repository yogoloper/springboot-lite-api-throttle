package io.github.yogoloper.throttle.exception;

import java.time.Instant;

public class QuotaExceededException extends ThrottleException {
    private final String period;  // "daily", "monthly"
    private final long limit;
    private final long remaining;
    private final Instant resetTime;

    public QuotaExceededException(String message, String period, long limit, long remaining, Instant resetTime) {
        super(message);
        this.period = period;
        this.limit = limit;
        this.remaining = remaining;
        this.resetTime = resetTime;
    }

    // Getters
    public String getPeriod() { return period; }
    public long getLimit() { return limit; }
    public long getRemaining() { return remaining; }
    public Instant getResetTime() { return resetTime; }
}
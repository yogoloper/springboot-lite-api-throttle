package io.github.yogoloper.throttle.exception;

import java.time.Instant;

public class RateLimitExceededException extends ThrottleException {
    private final long retryAfterSeconds;
    private final int limit;
    private final int remaining;
    private final Instant timestamp;

    public RateLimitExceededException(String message, long retryAfterSeconds, int limit, int remaining, Instant timestamp) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
        this.remaining = remaining;
        this.timestamp = timestamp;
    }

    public long getRetryAfterSeconds() { 
        return retryAfterSeconds;
    }

    public int getLimit() { 
        return limit; 
    }

    public int getRemaining() { 
        return remaining; 
    }

    public Instant getTimestamp() { 
        return timestamp; 
    }
}
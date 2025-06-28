package io.throttle.core.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Rate Limit 정책을 정의하는 클래스
 */
public class RateLimit {
    private final int limit;            // 요청 제한 수
    private final Duration duration;    // 시간 단위
    private final String key;           // 키 (IP, JWT 등)

    public RateLimit(int limit, Duration duration, String key) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        
        this.limit = limit;
        this.duration = duration;
        this.key = key;
    }

    // Getters
    public int getLimit() {
        return limit;
    }

    public Duration getDuration() {
        return duration;
    }

    public String getKey() {
        return key;
    }
}

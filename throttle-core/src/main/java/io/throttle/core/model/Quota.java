package io.throttle.core.model;

import java.time.Instant;

/**
 * API 할당량 정책을 정의하는 클래스
 */
public class Quota {
    private final int limit;        // 허용되는 최대 요청 수
    private final Period period;    // 기간 (daily, monthly)
    private final Instant resetTime; // 리셋 시간
    private final String key;        // 고유 키 (예: "user:123:/api/endpoint")

    public enum Period {
        DAILY,
        MONTHLY
    }

    public Quota(int limit, Period period, Instant resetTime, String key) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        if (resetTime == null) {
            throw new IllegalArgumentException("Reset time must not be null");
        }
        this.limit = limit;
        this.period = period;
        this.resetTime = resetTime;
        this.key = key;
    }

    // Getters
    public int getLimit() {
        return limit;
    }

    public Period getPeriod() {
        return period;
    }

    public Instant getResetTime() {
        return resetTime;
    }

    public String getKey() {
        return key;
    }
}
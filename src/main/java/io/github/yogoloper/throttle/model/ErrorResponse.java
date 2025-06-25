package io.github.yogoloper.throttle.model;

import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

/** 
 * API 쓰로틀링 에러 응답 모델
 */
@Getter
@Builder
public class ErrorResponse {
  /**
     * 공통 필드
     */
    private final String error;
    private final String type;
    
    protected ErrorResponse(String error, String type) {
        this.error = error;
        this.type = type;
    }
  
    @Builder
    public static class RateLimitResponse extends ErrorResponse {
        private final int limit;
        private final int remaining;
        private final long retryAfterSeconds;
        private final Instant timestamp;

        @Builder
        public RateLimitResponse(String error, String type, int limit, int remaining, long retryAfterSeconds, Instant timestamp) {
          super(error, type);
            this.limit = limit;
            this.remaining = remaining;
            this.retryAfterSeconds = retryAfterSeconds;
            this.timestamp = timestamp;
        }
    }

    @Builder
    public static class QuotaResponse extends ErrorResponse {
      private final long limit;
      private final long remaining;
      private final String period;
        private final Instant resetTime;

        @Builder
        public QuotaResponse(String error, String type, String period, long limit, long remaining, Instant resetTime) {
          super(error, type);
            this.period = period;
            this.limit = limit;
            this.remaining = remaining;
            this.resetTime = resetTime;
        }
    }
}
